package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;


//接口,有啥必要吗?  好像是为了搞分别 AvailableBook BookOnHold CheckedOutBook等不同状态的书
public interface Book {

    default BookId bookId() {
        return getBookInformation().getBookId();
    }

    default BookType type() {
        return getBookInformation().getBookType();
    }

    BookInformation getBookInformation();

    Version getVersion();

}

