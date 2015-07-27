package org.motechproject.nms.testing.it.tracking;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.testing.tracking.domain.Author;
import org.motechproject.nms.testing.tracking.domain.Book;
import org.motechproject.nms.testing.tracking.repository.AuthorDataService;
import org.motechproject.nms.testing.tracking.repository.BookDataService;
import org.motechproject.nms.tracking.domain.ChangeLog;
import org.motechproject.nms.tracking.repository.ChangeLogDataService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class TrackManyToManyChangesBundleIT extends BasePaxIT {

    @Inject
    AuthorDataService authorDataService;

    @Inject
    BookDataService bookDataService;

    @Inject
    ChangeLogDataService changeLogDataService;

    @Before
    public void setUp() {
        authorDataService.deleteAll();
        bookDataService.deleteAll();
        changeLogDataService.deleteAll();
    }

    @Test
    public void testChangesTrackedForInstanceCreation() {
        Book electricSheep = createBooksInTransaction("Electric Sheep");
        Book zombies = createBooksInTransaction("Zombies");
        Author johnDoe = createAuthorInTransaction("Jonh Doe", electricSheep, zombies);
        Author janeRoe = createAuthorInTransaction("Jane Roe", electricSheep, zombies);

        List<ChangeLog> johnDoeChanges = getAuthorChangeLogs(johnDoe);
        assertEquals(1, johnDoeChanges.size());
        String johnDoeChange = johnDoeChanges.get(0).getChange();
        assertThat(johnDoeChange, Matchers.containsString("name(null, Jonh Doe)"));
        assertThat(johnDoeChange, Matchers.containsString(String.format("books(added[%d,%d])", electricSheep.getId(), zombies.getId())));

        List<ChangeLog> janeRoeChanges = getAuthorChangeLogs(janeRoe);
        assertEquals(1, janeRoeChanges.size());
        String janeRoeChange = janeRoeChanges.get(0).getChange();
        assertThat(janeRoeChange, Matchers.containsString("name(null, Jane Roe)"));
        assertThat(janeRoeChange, Matchers.containsString(String.format("books(added[%d,%d])", electricSheep.getId(), zombies.getId())));
    }

    @Test
    public void testChangesTrackedForInstanceUpdate() {
        Book electricSheep = createBooksInTransaction("Electric Sheep");
        Book zombies = createBooksInTransaction("Zombies");
        Book chocolateBunny = createBooksInTransaction("Chocolate Bunny");
        Book areYouThere = createBooksInTransaction("Are you there?");
        Author johnDoe = createAuthorInTransaction("John Doe", electricSheep, zombies);
        Author janeRoe = createAuthorInTransaction("Jane Roe", electricSheep, zombies, chocolateBunny);
        johnDoe = updateAuthorInTransaction(johnDoe.getId(), "John Doe", electricSheep, chocolateBunny, areYouThere);
        janeRoe = updateAuthorInTransaction(janeRoe.getId(), "Jane Roe", zombies, areYouThere);

        List<ChangeLog> johnDoeChanges = getAuthorChangeLogs(johnDoe);
        assertEquals(2, johnDoeChanges.size());
        String johnDoeChange = getLatestChangeLog(johnDoeChanges).getChange();
        assertThat(johnDoeChange, Matchers.containsString(String.format("books(added[%d,%d], removed[%d])", chocolateBunny.getId(), areYouThere.getId(), zombies.getId())));

        List<ChangeLog> janeRoeChanges = getAuthorChangeLogs(janeRoe);
        assertEquals(2, janeRoeChanges.size());
        String janeRoeChange = getLatestChangeLog(janeRoeChanges).getChange();
        assertThat(janeRoeChange, Matchers.containsString(String.format("books(added[%d], removed[%d,%d])", areYouThere.getId(), electricSheep.getId(), chocolateBunny.getId())));
    }

    @Test
    public void testChangesTrackedForCollectionManipulations() {
        final Book electricSheep = createBooksInTransaction("Electric Sheep");
        final Book zombies = createBooksInTransaction("Zombies");
        final Book chocolateBunny = createBooksInTransaction("Chocolate Bunny");
        final Book areYouThere = createBooksInTransaction("Are you there?");
        final Author johnDoe = createAuthorInTransaction("John Doe", electricSheep, zombies);
        final Author janeRoe = createAuthorInTransaction("Jane Roe", electricSheep, zombies, chocolateBunny);
        authorDataService.doInTransaction(new TransactionCallback<Author>() {
            @Override
            public Author doInTransaction(TransactionStatus transactionStatus) {
                Author author = authorDataService.findById(johnDoe.getId());
                Book book1 = getBook(chocolateBunny);
                Book book2 = getBook(areYouThere);
                Book book3 = getBook(zombies);
                author.getBooks().add(book1);
                author.getBooks().add(book2);
                author.getBooks().remove(book3);
                return authorDataService.update(author);
            }
        });
        authorDataService.doInTransaction(new TransactionCallback<Author>() {
            @Override
            public Author doInTransaction(TransactionStatus transactionStatus) {
                Author author = authorDataService.findById(janeRoe.getId());
                Book book1 = getBook(areYouThere);
                Book book2 = getBook(electricSheep);
                Book book3 = getBook(chocolateBunny);
                author.getBooks().add(book1);
                author.getBooks().remove(book2);
                author.getBooks().remove(book3);
                return authorDataService.update(author);
            }
        });

        List<ChangeLog> johnDoeChanges = getAuthorChangeLogs(johnDoe);
        assertEquals(2, johnDoeChanges.size());
        String johnDoeChange = getLatestChangeLog(johnDoeChanges).getChange();
        assertThat(johnDoeChange, Matchers.containsString(String.format("books(added[%d,%d], removed[%d])", chocolateBunny.getId(), areYouThere.getId(), zombies.getId())));

        List<ChangeLog> janeRoeChanges = getAuthorChangeLogs(janeRoe);
        assertEquals(2, janeRoeChanges.size());
        String janeRoeChange = getLatestChangeLog(janeRoeChanges).getChange();
        assertThat(janeRoeChange, Matchers.containsString(String.format("books(added[%d], removed[%d,%d])", areYouThere.getId(), electricSheep.getId(), chocolateBunny.getId())));
    }

    private Author createAuthorInTransaction(final String name, final Book... books) {
        return authorDataService.doInTransaction(new TransactionCallback<Author>() {
            @Override
            public Author doInTransaction(TransactionStatus transactionStatus) {
                Author author = new Author();
                author.setName(name);
                author.setBooks(getBooks(books));
                return authorDataService.create(author);
            }
        });
    }

    private Author updateAuthorInTransaction(final Long id, final String name, final Book... books) {
        return authorDataService.doInTransaction(new TransactionCallback<Author>() {
            @Override
            public Author doInTransaction(TransactionStatus transactionStatus) {
                Author author = authorDataService.findById(id);
                author.setName(name);
                author.setBooks(getBooks(books));
                return authorDataService.update(author);
            }
        });
    }

    private Book createBooksInTransaction(final String name, final Author... authors) {
        return bookDataService.doInTransaction(new TransactionCallback<Book>() {
            @Override
            public Book doInTransaction(TransactionStatus transactionStatus) {
                Book book = new Book();
                book.setName(name);
                book.setAuthors(new HashSet<>(Arrays.asList(authors)));
                return bookDataService.create(book);
            }
        });
    }

    private Set<Book> getBooks(Book... books) {
        Set<Book> bookSet = new HashSet<>();
        for (Book book : books) {
            bookSet.add(getBook(book));
        }
        return bookSet;
    }

    private Book getBook(Book book) {
        return bookDataService.findById(book.getId());
    }

    private List<ChangeLog> getAuthorChangeLogs(Author author) {
        return changeLogDataService.findByEntityNameAndInstanceId(Author.class.getName(), author.getId());
    }

    private ChangeLog getLatestChangeLog(List<ChangeLog> changes) {
        return Collections.max(changes, new Comparator<ChangeLog>() {
            @Override
            public int compare(ChangeLog o1, ChangeLog o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
    }
}
