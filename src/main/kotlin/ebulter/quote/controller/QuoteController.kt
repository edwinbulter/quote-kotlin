package ebulter.quote.controller

import ebulter.quote.model.Quote
import ebulter.quote.service.QuoteService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.util.*

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
class QuoteController(val quoteService: QuoteService) {

    @GetMapping("/quotes")
    fun getAllQuotes(): List<Quote> {
        return quoteService.getAllQuotes()
    }

    @GetMapping("/quote/{id}")
    fun getQuoteById(@PathVariable id: Long): ResponseEntity<Quote> {
        val quote: Optional<Quote> = quoteService.getQuoteById(id)
        return if (quote.isPresent) {
            ResponseEntity.ok(quote.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/quote/{id}/like")
    fun likeQuote(@PathVariable id: Long): Int {
        return quoteService.likeQuote(id)
    }

    @GetMapping("/quote/liked")
    fun getLikedQuotes(): List<Quote> {
        return quoteService.getLikedQuotes()
    }

    @GetMapping("/quote")
    fun getRandomQuote(): ResponseEntity<Quote> {
        return getRandomQuote(emptyList())
    }

    @PostMapping("/quote")
    fun getRandomQuote(@RequestBody excludedIds: List<Long>): ResponseEntity<Quote> {
        val randomQuote = quoteService.getRandomQuote(excludedIds)
        return if (randomQuote.isPresent) {
            ResponseEntity.ok(randomQuote.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/quote/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamQuotes(): Flux<String> {
        return quoteService.streamQuotes()
    }
}