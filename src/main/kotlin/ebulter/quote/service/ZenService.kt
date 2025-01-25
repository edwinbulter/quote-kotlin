package ebulter.quote.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ZenService {
    @Value("\${zenquotes.url}")
    lateinit var zenQuotesUrl: String

    fun fetchQuotesFromZen(): List<ZenQuote> {
        val webClient = WebClient.create()

        val response = webClient.get()
            .uri(zenQuotesUrl)
            .retrieve()
            .bodyToMono(Array<ZenQuote>::class.java)
            .block()
        return response?.toList() ?: emptyList()
    }

    // Nested class to map Zen API response
    data class ZenQuote(
        val q: String = "",
        val a: String = ""
    )

}