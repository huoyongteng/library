package io.pillopl.library.catalogue;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.commons.events.DomainEvents;
import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
//目录(聚合)
//在聚合里持久化
public class Catalogue {

    private final CatalogueDatabase database;
    private final DomainEvents domainEvents;

    //增加书目(只有书名,没有实体)
    public Try<Result> addBook(String author, String title, String isbn) {
        return Try.of(() -> {
            Book book = new Book(isbn, author, title);
            database.saveNew(book);
            return Success;
        });
    }

    //增加实体书,
    public Try<Result> addBookInstance(String isbn, BookType bookType) {
        return Try.of(() -> database
                .findBy(new ISBN(isbn))
                .map(book -> BookInstance.instanceOf(book, bookType))
                .map(this::saveAndPublishEvent)
                .map(savedInstance -> Success)
                .getOrElse(Rejection));
    }

    //持久行为
    private BookInstance saveAndPublishEvent(BookInstance bookInstance) {
        database.saveNew(bookInstance);
        //发布图书上架信息
        domainEvents.publish(new BookInstanceAddedToCatalogue(bookInstance));
        return bookInstance;
    }


}

