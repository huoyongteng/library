package io.pillopl.library.lending.book.model;


import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import lombok.NonNull;
import lombok.Value;

@Value
public class BookInformation {
//值对象
    @NonNull
    BookId bookId;

    @NonNull
    BookType bookType;
}
