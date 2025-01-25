package ebulter.quote.service

import ebulter.quote.model.Quote
import ebulter.quote.repository.QuoteRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*

@Service
class QuoteService(private val quoteRepository: QuoteRepository,
                   private val quoteEventPublisher: QuoteEventPublisher,
                   private val zenService: ZenService) {

    fun getAllQuotes(): List<Quote> {
        return quoteRepository.findAll()
    }

    fun getQuoteById(id: Long): Optional<Quote> {
        return quoteRepository.findById(id)
    }
    
    fun likeQuote(id: Long): Int {
        val quoteOptional = quoteRepository.findById(id)
        if (quoteOptional.isEmpty) {
            return 0
        }
        val quote = quoteOptional.get()
        val updatedQuote = quote.copy(likes = quote.likes + 1)
        quoteRepository.save(updatedQuote)

        val message = "${quote.quoteText} - ${quote.author}"
        quoteEventPublisher.publishEvent(message) // Broadcast to all SSE subscribers

        return updatedQuote.likes
    }

    fun getLikedQuotes(): List<Quote> {
        return quoteRepository.findByLikesGreaterThanOrderByLikesDescIdAsc(0)
    }

    fun getRandomQuote(excludedIds: List<Long>): Optional<Quote> {
        // Fetch a random quote excluding the given IDs
        var availableQuotes = if (excludedIds.isNotEmpty()) {
            quoteRepository.findByIdNotIn(excludedIds)
        } else {
            quoteRepository.findAll()
        }
        if (availableQuotes.isEmpty()) {
            // Fetch new quotes from the Zen API if no quotes remain
            val zenQuotes = zenService.fetchQuotesFromZen()

            // Filter Zen quotes by removing duplicates (quoteText + author)
            val existingQuotes = quoteRepository.findAll()
            val filteredZenQuotes = zenQuotes.filter { zenQuote ->
                existingQuotes.none { it.quoteText == zenQuote.q && it.author == zenQuote.a }
            }

            // Insert the new quotes into the database
            filteredZenQuotes.forEach { quoteRepository.save(Quote(quoteText = it.q, author = it.a, likes = 0)) }

            // Fetch the updated list of quotes after adding new ones
            availableQuotes = quoteRepository.findByIdNotIn(excludedIds)
        }
        return if (availableQuotes.isNotEmpty()) {
            Optional.of(availableQuotes.random())
        } else {
            Optional.empty()
        }
    }

    fun streamQuotes(): Flux<String> {
        return quoteEventPublisher.getEvents()
    }

}