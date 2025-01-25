package ebulter.quote.repository

import ebulter.quote.model.Quote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuoteRepository : JpaRepository<Quote, Long> {

    fun findByLikesGreaterThanOrderByLikesDescIdAsc(minLikes: Int): List<Quote>

    fun findByIdNotIn(ids: List<Long>): List<Quote>
}