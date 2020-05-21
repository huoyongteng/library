package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.model.*;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.API;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;

import java.time.Instant;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@AllArgsConstructor
//从Book视角看到的借阅人相关事件的处理器
public class PatronEventsHandler {

    private final BookRepository bookRepository;
    private final DomainEvents domainEvents;


    @EventListener
    void handle(BookPlacedOnHold bookPlacedOnHold) {
        //图书被申请onhold事件事件
        bookRepository.findBy(new BookId(bookPlacedOnHold.getBookId()))
                .map(book -> handleBookPlacedOnHold(book, bookPlacedOnHold))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookCheckedOut bookCheckedOut) {
        //图书被借出事件
        bookRepository.findBy(new BookId(bookCheckedOut.getBookId()))
                .map(book -> handleBookCheckedOut(book, bookCheckedOut))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookHoldExpired holdExpired) {
        //图书被申请人onhold过期
        bookRepository.findBy(new BookId(holdExpired.getBookId()))
                .map(book -> handleBookHoldExpired(book, holdExpired))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookHoldCanceled holdCanceled) {
        //图书被onhold取消
        bookRepository.findBy(new BookId(holdCanceled.getBookId()))
                .map(book -> handleBookHoldCanceled(book, holdCanceled))
                .map(this::saveBook);
    }

    @EventListener
    void handle(BookReturned bookReturned) {
        //图书被返回
        bookRepository.findBy(new BookId(bookReturned.getBookId()))
                .map(book -> handleBookReturned(book, bookReturned))
                .map(this::saveBook);
    }


    private Book handleBookPlacedOnHold(Book book, BookPlacedOnHold bookPlacedOnHold) {
        return API.Match(book).of(
                //如果是可用图书被onhold,直接操作,并记录事件
                Case($(instanceOf(AvailableBook.class)), availableBook -> availableBook.handle(bookPlacedOnHold)),
                //已经被onhold,并且被不同人hold,发布冲突事件,返回onHold
                Case($(instanceOf(BookOnHold.class)), bookOnHold -> raiseDuplicateHoldFoundEvent(bookOnHold, bookPlacedOnHold)),
                //其他情况
                Case($(), () -> book)
        );
    }


    //Q1:有违迪米特法则, 目前客户端需要知道BOOK是那种类型,再调用相应行为
    // 可以考虑状态模式解决这个问题


    private BookOnHold raiseDuplicateHoldFoundEvent(BookOnHold onHold, BookPlacedOnHold bookPlacedOnHold) {
        if (onHold.by(new PatronId(bookPlacedOnHold.getPatronId()))) {
            return onHold;
        }
        domainEvents.publish(
                new BookDuplicateHoldFound(
                        Instant.now(),
                        onHold.getByPatron().getPatronId(),
                        bookPlacedOnHold.getPatronId(),
                        bookPlacedOnHold.getLibraryBranchId(),
                        bookPlacedOnHold.getBookId()));
        return onHold;
    }


    private Book handleBookHoldExpired(Book book, BookHoldExpired holdExpired) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdExpired)),
                Case($(), () -> book)
        );
    }

    private Book handleBookHoldCanceled(Book book, BookHoldCanceled holdCanceled) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdCanceled)),
                Case($(), () -> book)
        );
    }

    private Book handleBookCheckedOut(Book book, BookCheckedOut bookCheckedOut) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(bookCheckedOut)),
                Case($(), () -> book)
        );
    }

    private Book handleBookReturned(Book book, BookReturned bookReturned) {
        return API.Match(book).of(
                Case($(instanceOf(CheckedOutBook.class)), checkedOut -> checkedOut.handle(bookReturned)),
                Case($(), () -> book)
        );
    }

    private Book saveBook(Book book) {
        bookRepository.save(book);
        return book;
    }

}
