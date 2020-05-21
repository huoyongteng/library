package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.vavr.control.Option;

//存书仓储接口
public interface BookRepository {

    Option<Book> findBy(BookId bookId);

    void save(Book book);
}
