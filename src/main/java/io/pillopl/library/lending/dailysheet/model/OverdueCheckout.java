package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.Value;

@Value
//过期不还图书模型
public class OverdueCheckout {
    private final BookId checkedOutBook;
    private final PatronId patron;
    private final LibraryBranchId library;

    /**
     * 转成事件了..
     * @return
     */
    OverdueCheckoutRegistered toEvent() {
        return OverdueCheckoutRegistered.now(this.patron, this.checkedOutBook, this.library);
    }
}
