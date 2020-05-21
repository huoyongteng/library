package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.NonNull;
import lombok.Value;

@Value
/**
 * 过期的checkout视图
 */
public class CheckoutsToOverdueSheet {

    @NonNull
    List<OverdueCheckout> checkouts;

    public Stream<OverdueCheckoutRegistered> toStreamOfEvents() {
        return checkouts.toStream()
                .map(OverdueCheckout::toEvent);
    }

    public int count() {
        return checkouts.size();
    }

}
