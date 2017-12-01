package ch.uzh.ifi.seal.data_access

import ch.uzh.ifi.seal.domain_classes.DailyFormInfo
import ch.uzh.ifi.seal.domain_classes.User
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

/**
 *
 */
interface DailyFormInfoRepository : CrudRepository <DailyFormInfo, Int> {

    fun findByOwner(user: User): List<DailyFormInfo>
    fun findByOwnerId(id: Int): List<DailyFormInfo>

    fun findByOwnerAndDate(user: User, date: LocalDate): DailyFormInfo?
}