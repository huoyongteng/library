package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import lombok.NonNull;
import lombok.Value;

@Value
/**
 * 值对象, 书在哪个图书分馆
 */
class Hold {

    @NonNull BookId bookId;
    @NonNull LibraryBranchId libraryBranchId;

}
