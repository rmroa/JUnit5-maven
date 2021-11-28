package com.rm.junit.service;

import com.rm.junit.dao.UserDao;
import com.rm.junit.entity.User;
import com.rm.junit.extension.ConditionalExtension;
import com.rm.junit.extension.GlobalExtension;
import com.rm.junit.extension.PostProcessingExtension;
import com.rm.junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith({
        UserServiceParamResolver.class,
        GlobalExtension.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
//        ThrowableExtension.class
})
class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "456");

    @Captor
    ArgumentCaptor<Integer> argumentCaptor;

    @Mock(lenient = true)
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
//        lenient().when(userDao.delete(IVAN.getId())).thenReturn(true);
        doReturn(true).when(userDao).delete(IVAN.getId()); // stub для конкретного mock
//        Mockito.mock(UserDao.class, withSettings().lenient()); // аналогия @Mock(lenient = true)
//        this.userDao = Mockito.spy(UserDao.class);
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());

        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
//        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());

//        Mockito.when(userDao.delete(IVAN.getId()))
//                .thenReturn(true)
//                .thenReturn(false);

//        BDDMockito.given(userDao.delete(IVAN.getId())).willReturn(true);
//        BDDMockito.willReturn(true).given(userDao.delete(IVAN.getId()));

        boolean deleteResult = userService.delete(IVAN.getId());
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

//        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(userDao, Mockito.times(3)).delete(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());

        assertThat(deleteResult).isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("users will be empty if no user added")
    void userEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();

//        MatcherAssert.assertThat(users, IsEmptyCollection.empty());
        assertTrue(users.isEmpty());
    }

    @Test
    void userSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        List<User> users = userService.getAll();

        assertThat(users).hasSize(2);
//        assertEquals(2, users.size());
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

//        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId()));
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @Nested
    @Tag("login")
    class LoginTest {

        @Test
        void loginSuccessIfUserExist() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
//        assertTrue(maybeUser.isPresent());
//        maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        }

        @Test
        @Disabled("flaky, need to see")
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), "not correct");

            assertTrue(maybeUser.isEmpty());
        }

        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login("not correct", "123");

            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void checkLoginFunctionalityPerformance() {
            System.out.println(Thread.currentThread().getName());
            assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {
                System.out.println(Thread.currentThread().getName());
                Thread.sleep(100);
                return userService.login(IVAN.getUsername(), IVAN.getPassword());
            });
        }

        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "not correct")),
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("not correct", null))
            );
        }

        @ParameterizedTest(name = "{arguments} test")
//        @ArgumentsSource() // функциональность, которая подставит нам аргументы с помощью DI, его задача предоставить поток аргументов в наш метод
//        @NullSource // реализовывает NullArgumentProvider. используется с одним параметром
//        @EmptySource // реализовывает EmptyArgumentProvider. используется с одним параметром
//        @ValueSource(strings = { // используется с одним параметром
//                "Ivan", "Petr"
//        })
//        @EnumSource // используется при входных данных (Enum)
        @MethodSource("com.rm.junit.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1) // numLinesToSkip - пропустить кол- во строк затраченных на заголовок
//        @CsvSource({
//                "Ivan,123",
//                "Petr,456   ",
//        })
        @DisplayName("login param test")
            // переопределить название
            // пароль в аргументах можно представить в виду Integer, тк легко конвертится в String
        void loginParameterizedTest(String username, String password, Optional<User> user) { // просим JUnit предоставить нам параметры
            userService.add(IVAN, PETR);
            Optional<User> oneUser = userService.login(username, password);

            assertThat(oneUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "456", Optional.of(PETR)),
                Arguments.of("Petr", "not correct", Optional.empty()),
                Arguments.of("not correct", "123", Optional.empty())
        );
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }
}