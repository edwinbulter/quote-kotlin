package ebulter.quote.service

import ebulter.quote.model.Quote
import ebulter.quote.repository.QuoteRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import java.util.*

@SpringBootTest
class QuoteServiceTest {

    private val quoteRepository: QuoteRepository = mockk()

    private val quoteEventPublisher: QuoteEventPublisher = mockk()

    private val zenService: ZenService = mockk()

    private val quoteService: QuoteService = QuoteService(quoteRepository, quoteEventPublisher, zenService)


    @Test
    fun `test getAllQuotes returns all quotes from repository`() {
        val quotes = listOf(Quote(id = 1, quoteText = "Test Quote", author = "Author", likes = 0))
        every { quoteRepository.findAll() } returns quotes

        val result = quoteService.getAllQuotes()

        assertEquals(quotes, result)
        verify { quoteRepository.findAll() }
    }

    @Test
    fun `test getQuoteById returns correct quote by ID`() {
        val quote = Quote(id = 1, quoteText = "Test Quote", author = "Author", likes = 0)
        every { quoteRepository.findById(1) } returns Optional.of(quote)

        val result = quoteService.getQuoteById(1)

        assertTrue(result.isPresent)
        assertEquals(quote, result.get())
        verify { quoteRepository.findById(1) }
    }

    @Test
    fun `test likeQuote increases likes and publishes event`() {
        val quote = Quote(id = 1, quoteText = "Test Quote", author = "Author", likes = 0)
        val updatedQuote = quote.copy(likes = 1)
        every { quoteRepository.findById(1) } returns Optional.of(quote)
        every { quoteRepository.save(updatedQuote) } returns updatedQuote
        every { quoteEventPublisher.publishEvent(any()) } just Runs

        val result = quoteService.likeQuote(1)

        assertEquals(1, result)
        // Verify if called:
        verify { quoteRepository.findById(1) }
        verify { quoteRepository.save(updatedQuote) }
        verify { quoteEventPublisher.publishEvent("${quote.quoteText} - ${quote.author}") }
    }

    @Test
    fun `test likeQuote returns 0 if quote not found`() {
        every { quoteRepository.findById(2) } returns Optional.empty()

        val result = quoteService.likeQuote(2)

        assertEquals(0, result)
        verify { quoteRepository.findById(2) }
    }

    @Test
    fun `test getLikedQuotes returns quotes with likes greater than zero`() {
        val quotes = listOf(Quote(id = 1, quoteText = "Test Quote", author = "Author", likes = 5))
        every { quoteRepository.findByLikesGreaterThanOrderByLikesDescIdAsc(0) } returns quotes

        val result = quoteService.getLikedQuotes()

        assertEquals(quotes, result)
        verify { quoteRepository.findByLikesGreaterThanOrderByLikesDescIdAsc(0) }
    }

    @Test
    fun `test getRandomQuote returns a random available quote`() {
        val quote = Quote(id = 1, quoteText = "Test Quote", author = "Author", likes = 0)
        every { quoteRepository.findAll() } returns listOf(quote)

        val result = quoteService.getRandomQuote(emptyList())

        assertTrue(result.isPresent)
        assertEquals(quote, result.get())
        verify { quoteRepository.findAll() }
    }

    @Test
    fun `test getRandomQuote excludes specified ID`() {
        val quote1 = Quote(id = 1, quoteText = "Quote 1", author = "Author 1", likes = 0)
        val quote2 = Quote(id = 2, quoteText = "Quote 2", author = "Author 2", likes = 0)
        every { quoteRepository.findByIdNotIn(listOf(1)) } returns listOf(quote2)

        val result = quoteService.getRandomQuote(listOf(1))

        assertTrue(result.isPresent)
        assertEquals(quote2, result.get())
        verify { quoteRepository.findByIdNotIn(listOf(1)) }
    }

    @Test
    fun `test getRandomQuote excludes specified ID and no quotes remain`() {
        val quote1 = Quote(id = 1, quoteText = "Quote 1", author = "Author 1", likes = 0)
        val quote2 = Quote(id = 2, quoteText = "Quote 2", author = "Author 2", likes = 0)
        val zenQuote = ZenService.ZenQuote(q = quote2.quoteText, a = quote2.author)
        // the next call is done twice, the first time it should return an emptyList the second time a list with quote2
        every { quoteRepository.findByIdNotIn(listOf(1)) } returns emptyList() andThen listOf(quote2)
        every { zenService.fetchQuotesFromZen() } returns listOf(zenQuote)
        every { quoteRepository.findAll() } returns listOf(quote1)
        every {
            quoteRepository.save(
                Quote(
                    id = 0,
                    quoteText = zenQuote.q,
                    author = zenQuote.a,
                    likes = 0
                )
            )
        } returns quote2

        val result = quoteService.getRandomQuote(listOf(1))

        assertTrue(result.isPresent)
        assertEquals(quote2, result.get())
        verify { quoteRepository.findByIdNotIn(listOf(1)) }
        verify { zenService.fetchQuotesFromZen() }
    }

    @Test
    fun `test streamQuotes returns published events`() {
        every { quoteEventPublisher.getEvents() } returns Flux.just("Event1", "Event2")

        val result = quoteService.streamQuotes().collectList().block()

        assertNotNull(result)
        assertEquals(listOf("Event1", "Event2"), result)
        verify { quoteEventPublisher.getEvents() }
    }
}