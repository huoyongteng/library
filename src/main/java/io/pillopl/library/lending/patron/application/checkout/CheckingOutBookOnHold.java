package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
/**
 * Application:借出书来
 */
public class CheckingOutBookOnHold {

    private final FindBookOnHold findBookOnHold;
    private final Patrons patronRepository;

    /**
     * 命令模式, 在应用层, 发起借阅人checkout命令
     *
     * @param command
     * @return
     */
    public Try<Result> checkOut(@NonNull CheckOutBookCommand command) {
        return Try.of(() -> {
            BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
            Patron patron = find(command.getPatronId());
            Either<BookCheckingOutFailed, BookCheckedOut> result = patron.checkOut(bookOnHold, command.getCheckoutDuration());
//            result.map(this::publishEvents).mapLeft(this::publishEvents);
            return Match(result).of(
                    Case($Left($()), this::publishEvents),
                    Case($Right($()), this::publishEvents));
        });
    }

    /**
     * 处理完command, 发送成功通知
     *
     * @param bookCheckedOut
     * @return
     */
    private Result publishEvents(BookCheckedOut bookCheckedOut) {
        patronRepository
                .publish(bookCheckedOut);
        return Success;
    }

    /**
     * 处理完command,失败,  发送处理失败通知
     *
     * @param bookCheckingOutFailed
     * @return
     */
    private Result publishEvents(BookCheckingOutFailed bookCheckingOutFailed) {
        patronRepository
                .publish(bookCheckingOutFailed);
        return Rejection;
    }

    /**
     * 找到申请的图书
     *
     * @param id
     * @param patronId
     * @return
     */
    private BookOnHold find(BookId id, PatronId patronId) {
        return findBookOnHold
                .findBookOnHold(id, patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Cannot find book on hold with Id: " + id.getBookId()));
    }

    /**
     * 找到借阅人实体
     *
     * @param patronId
     * @return
     */
    private Patron find(PatronId patronId) {
        return patronRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }

}
