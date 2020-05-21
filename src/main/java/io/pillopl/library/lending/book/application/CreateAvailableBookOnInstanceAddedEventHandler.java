package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookInstanceAddedToCatalogue;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;

import java.util.UUID;

@AllArgsConstructor
public class CreateAvailableBookOnInstanceAddedEventHandler {

    private final BookRepository bookRepository;

    @EventListener
    void handle(BookInstanceAddedToCatalogue event) {
        //收到书实体添加到目录的时间后,保存可用书`
        bookRepository.save(new AvailableBook(new BookId(event.getBookId()), event.getType(), ourLibraryBranch(), Version.zero()));
    }

    private LibraryBranchId ourLibraryBranch() {
        //from properties
        return new LibraryBranchId(UUID.randomUUID());
    }


}
