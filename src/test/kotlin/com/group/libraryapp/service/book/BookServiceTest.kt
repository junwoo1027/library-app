package com.group.libraryapp.service.book;

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {
    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록이 정상 동작한다")
    fun saveBook() {
        // given
        val request = BookRequest("book", BookType.COMPUTER)

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()

        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo("book")
        assertThat(books[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다")
    fun loanBook() {
        // given
        val book = bookRepository.save(Book.fixture("book"))
        val user = userRepository.save(User("junwoo", null))
        val request = BookLoanRequest(user.name, book.name)

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()

        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("book")
        assertThat(results[0].status).isEqualTo(UserLoanStatus.LOANED)
        assertThat(results[0].user.id).isEqualTo(user.id)
    }

    @Test
    @DisplayName("책이 이미 대출되어있다면, 신규 대출이 실패한다")
    fun loanBookFailTest() {
        // given
        val book = bookRepository.save(Book.fixture("book"))
        val user = userRepository.save(User("junwoo", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(user, book.name))
        val request = BookLoanRequest(user.name, book.name)

        // when & then
        assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.apply {
            assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
        }
    }

    @Test
    @DisplayName("책 반납이 정상 동작한다")
    fun returnBook() {
        // given
        val user = userRepository.save(User("junwoo", null))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(user, "book"))
        val request = BookReturnRequest(user.name, "book")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()

        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    @DisplayName("책 대여 권수를 정상 확인한다")
    fun countLoanedBookTest() {
        // given
        val user = userRepository.save(User("junwoo", null))

        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(user, "book1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(user, "book2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(user, "book3", UserLoanStatus.RETURNED),
        ))

        // when
        val count = bookService.countLoanedBook()

        // then
        assertThat(count).isEqualTo(2)
    }

    @Test
    @DisplayName("분아별 책 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(
            listOf(
                Book("book1", BookType.COMPUTER),
                Book("book2", BookType.COMPUTER),
                Book("book3", BookType.SCIENCE),
                Book("book4", BookType.SOCIETY),
            )
        )

        // when
        val results = bookService.getBookStatistics()

        // then
        println("aa" + results)
        assertThat(results).hasSize(3)
        assertCount(results, BookType.COMPUTER, 2L)
        assertCount(results, BookType.SCIENCE, 1L)
        assertCount(results, BookType.SOCIETY, 1L)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
        val dto = results.first { result -> result.type == type }
        assertThat(dto.count).isEqualTo(count)
    }
}