package ebulter.quote.service

import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Component
class QuoteEventPublisher {

    private val sink: Sinks.Many<String> = Sinks.many().multicast().onBackpressureBuffer()

    // Provide a Flux for clients to consume the stream
    fun getEvents(): Flux<String> {
        return sink.asFlux()
    }

    // Trigger an event with the given message
    fun publishEvent(event: String) {
        sink.tryEmitNext(event)
    }
}