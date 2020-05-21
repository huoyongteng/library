package io.pillopl.library.lending.book.model;

import io.pillopl.library.commons.events.DomainEvent;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;


/**
 * 重复持有被发现了!!!!  类似订单被重复支付了.
 */
@Value
public class BookDuplicateHoldFound implements DomainEvent {
    @NonNull UUID eventId = UUID.randomUUID();
    @NonNull Instant when;
    @NonNull UUID firstPatronId;
    @NonNull UUID secondPatronId;
    @NonNull UUID libraryBranchId;
    @NonNull UUID bookId;

    @Override
    public UUID getAggregateId() {
        return bookId;
    }
}