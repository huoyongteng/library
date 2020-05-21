package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.application.checkout.CheckingOutBookOnHold;
import io.pillopl.library.lending.patron.application.checkout.RegisteringOverdueCheckout;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.ExpiringHolds;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.pillopl.library.lending.patron.application.hold.PlacingOnHold;
import io.pillopl.library.lending.patron.model.PatronFactory;
import io.pillopl.library.lending.patron.model.Patrons;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * spring 初始化Bean
 */
@Configuration
@EnableJdbcRepositories
public class PatronConfiguration {

    @Bean
    CheckingOutBookOnHold checkingOutBookOnHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
        return new CheckingOutBookOnHold(findBookOnHold, patronRepository);
    }

    /**
     * 发现逾期不还借阅人
     *
     * @param dailySheet
     * @param patronRepository
     * @return
     */
    @Bean
    RegisteringOverdueCheckout registeringOverdueCheckout(DailySheet dailySheet, Patrons patronRepository) {
        return new RegisteringOverdueCheckout(dailySheet, patronRepository);
    }

    @Bean
    CancelingHold cancelingHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
        return new CancelingHold(findBookOnHold, patronRepository);
    }

    @Bean
    ExpiringHolds expiringHolds(DailySheet dailySheet, Patrons patronRepository) {
        return new ExpiringHolds(dailySheet, patronRepository);
    }

    @Bean
    HandleDuplicateHold handleDuplicateHold(CancelingHold cancelingHold) {
        return new HandleDuplicateHold(cancelingHold);
    }

    @Bean
    PlacingOnHold placingOnHold(FindAvailableBook findAvailableBook, Patrons patronRepository) {
        return new PlacingOnHold(findAvailableBook, patronRepository);
    }

    /**
     * 这是大佬啊啊, 数据库存好事件处理结果,再分发spring-event
     *
     * @param patronEntityRepository
     * @param domainEvents
     * @return
     */
    @Bean
    Patrons patronRepository(PatronEntityRepository patronEntityRepository,
                             DomainEvents domainEvents) {
        return new PatronsDatabaseRepository(
                patronEntityRepository,
                new DomainModelMapper(new PatronFactory()),
                domainEvents);
    }
}
