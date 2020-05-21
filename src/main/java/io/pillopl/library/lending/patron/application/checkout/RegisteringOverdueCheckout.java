package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.commons.commands.BatchResult;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

/***
 * 这也是checkout的Application,感觉没必要分开.
 * 放到一起算了
 * 这种业务可以用定时触发
 */
@AllArgsConstructor
public class RegisteringOverdueCheckout {
    /**
     * 查询模型
     */
    private final DailySheet find;
    /**
     * 为什么叫patron repo
     */
    private final Patrons patronRepository;

    public Try<BatchResult> registerOverdueCheckouts() {
        return Try.of(() ->
                find.queryForCheckoutsToOverdue()
                        .toStreamOfEvents()
                        .map(this::publish)
                        .find(Try::isFailure)
                        .map(tryErrs -> BatchResult.SomeFailed)
                        .getOrElse(BatchResult.FullSuccess));
    }

    /**
     * @param event
     * @return
     */
    private Try<Void> publish(OverdueCheckoutRegistered event) {
        /**
         *
         */
        return Try.run(() -> patronRepository.publish(event));
    }

}
