package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.*;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static java.util.stream.Collectors.*;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
//亲娘来,居然是主入口
class PatronsDatabaseRepository implements Patrons {
    //实体仓储类
    //PatronsDatabaseRepository 和它 有什么区别?
    private final PatronEntityRepository patronEntityRepository;
    //工具类
    private final DomainModelMapper domainModelMapper;
    //实体消息分发器,存到数据库后再分发,这很科学
    private final DomainEvents domainEvents;

    /**
     * 找到数据库里的实体记录, 然后转乘patron实体,进行各种操作
     *
     * @param patronId
     * @return
     */
    @Override
    public Option<Patron> findBy(PatronId patronId) {
        return Option.of(patronEntityRepository
                .findByPatronId(patronId.getPatronId()))
                .map(domainModelMapper::map);
    }

    @Override
    public Patron publish(PatronEvent domainEvent) {
        Patron result = Match(domainEvent).of(
                //创建新借阅人
                Case($(instanceOf(PatronCreated.class)), this::createNewPatron),
                //处理新事件----重点关注一下这里
                Case($(), this::handleNextEvent));
        //发布事件
        domainEvents.publish(domainEvent.normalize());
        return result;
    }

    /**
     * 创建新借阅人 TODO:好像curd没有实现类
     *
     * @param domainEvent
     * @return
     */
    private Patron createNewPatron(PatronCreated domainEvent) {
        PatronDatabaseEntity entity = patronEntityRepository
                .save(new PatronDatabaseEntity(domainEvent.patronId(), domainEvent.getPatronType()));
        return domainModelMapper.map(entity);
    }

    /**
     * 处理事件
     *
     * @param domainEvent
     * @return
     */
    private Patron handleNextEvent(PatronEvent domainEvent) {
        //找到数据库实体
        PatronDatabaseEntity entity = patronEntityRepository.findByPatronId(domainEvent.patronId().getPatronId());
        //为什么让数据库实体去处理事件呢,应该会导致databaseEntity发生变化了哦
        entity = entity.handle(domainEvent);
        //最后再save一下,好像也没有实现
        entity = patronEntityRepository.save(entity);
        return domainModelMapper.map(entity);
    }

}

/**
 * CURD 操作类,NB了
 */
interface PatronEntityRepository extends CrudRepository<PatronDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_database_entity p where p.patron_id = :patronId")
    PatronDatabaseEntity findByPatronId(@Param("patronId") UUID patronId);

}

@AllArgsConstructor
class DomainModelMapper {

    private final PatronFactory patronFactory;

    Patron map(PatronDatabaseEntity entity) {
        return patronFactory.create(
                entity.patronType,
                new PatronId(entity.patronId),
                mapPatronHolds(entity),
                mapPatronOverdueCheckouts(entity)
        );
    }

    Map<LibraryBranchId, Set<BookId>> mapPatronOverdueCheckouts(PatronDatabaseEntity patronDatabaseEntity) {
        return
                patronDatabaseEntity
                        .checkouts
                        .stream()
                        .collect(groupingBy(OverdueCheckoutDatabaseEntity::getLibraryBranchId, toSet()))
                        .entrySet()
                        .stream()
                        .collect(toMap(
                                (Entry<UUID, Set<OverdueCheckoutDatabaseEntity>> entry) -> new LibraryBranchId(entry.getKey()), entry -> entry
                                        .getValue()
                                        .stream()
                                        .map(entity -> (new BookId(entity.bookId)))
                                        .collect(toSet())));
    }

    Set<Tuple2<BookId, LibraryBranchId>> mapPatronHolds(PatronDatabaseEntity patronDatabaseEntity) {
        return patronDatabaseEntity
                .booksOnHold
                .stream()
                .map(entity -> Tuple.of((new BookId(entity.bookId)), new LibraryBranchId(entity.libraryBranchId)))
                .collect(toSet());
    }

}
