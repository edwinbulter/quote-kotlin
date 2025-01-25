package ebulter.quote

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class QuoteApplication

fun main(args: Array<String>) {
    SpringApplication.run(QuoteApplication::class.java, *args)
}