package ebulter.quote

import ebulter.quote.model.Quote
import ebulter.quote.repository.QuoteRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class LoadDatabase {
    @Bean
    open fun initDatabase(repository: QuoteRepository) = CommandLineRunner {
        repository.save(Quote(quoteText = "The only limit to our realization of tomorrow is our doubts of today.", author ="Author 1", likes =0))
        repository.save(Quote(quoteText = "In the middle of every difficulty lies opportunity.", author ="Author 2", likes =0))
    }
}