package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent.*;

//图书馆日报.   投射模型
//read shadown 读投射
public interface DailySheet {
    /**
     * 查询马上到期的checkout
     *
     * @return
     */
    CheckoutsToOverdueSheet queryForCheckoutsToOverdue();

    /**
     * 查询要过期的onHold
     *
     * @return
     */
    HoldsToExpireSheet queryForHoldsToExpireSheet();


    void handle(BookPlacedOnHold event);

    void handle(BookHoldCanceled event);

    void handle(BookHoldExpired event);

    void handle(BookCheckedOut event);

    void handle(BookReturned event);


}
