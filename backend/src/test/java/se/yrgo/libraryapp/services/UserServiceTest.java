package se.yrgo.libraryapp.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.yrgo.libraryapp.dao.UserDao;
import se.yrgo.libraryapp.entities.LoginInfo;
import se.yrgo.libraryapp.entities.User;
import se.yrgo.libraryapp.entities.UserId;
import se.yrgo.libraryapp.validators.RealName;

import java.sql.SQLException;
import java.util.Optional;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserServiceTest {
    @Mock
    private UserDao userDao;

    @Test
    @SuppressWarnings("deprecation")
    void correctLogin() {
        final String userId = "1";
        final UserId id = UserId.of(userId);
        final String username = "testuser";
        final String password = "password";
        final String passwordHash = "password";
        final LoginInfo info = new LoginInfo(id, passwordHash);
        final PasswordEncoder encoder =
                org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();

        when(userDao.getLoginInfo(username)).thenReturn(Optional.of(info));

        UserService userService = new UserService(userDao, encoder);
        assertThat(userService.validate(username,
                password)).isEqualTo(Optional.of(id));
    }

    @ParameterizedTest
    @SuppressWarnings("deprecation")
    @CsvSource(value = {
            "bosseTheMan, Bosse Bredsladd, 123password",
            "some_aristocat, Thomas O'Malley, password123"
    })
    void registerNewUserWithValidParams(String name, String realname, String password) throws SQLException {
        when(userDao.insertUserAndRole(anyString(), anyString(), anyString()))
                .thenReturn(true);

        UserService userService = new UserService(
                userDao, org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
        assertThat(userService.register(name, realname, password)).isTrue();
    }

//    @Test
//    void registerUser_CorrectlyInsertsRealnameWithSpecialChars() throws SQLException {
//        String realnameWithApostrophe = "Thomas O'Malley";
//        String nameCopy = "Thomas O'Malley";
//
//        when(userDao.insertUserAndRole(anyString(), realnameWithApostrophe, anyString()))
//                .thenReturn(true);
//
//        UserService userService = new UserService(
//                userDao, org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
//        userService.register("user123", realnameWithApostrophe, "password123");
//
//        verify(RealName).
//    }

    @ParameterizedTest
    @SuppressWarnings("deprecation")
    @CsvSource(value = {
            " , , ",
            "null, null, null"
    }, nullValues = "null")
    void registerNewUserWithInvalidParams(String name, String realname, String password) throws SQLException {
        UserService userService = new UserService(
                userDao, org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
        assertThat(userService.register(name, realname, password)).isFalse();
    }

    @Test
    void isNameAvailable_WithAvailableName() {
        String name = "bosse1";
        when(userDao.getByName(name)).thenReturn(Optional.empty());
        UserService userService = new UserService(
                userDao, org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());

        assertThat(userService.isNameAvailable(name)).isTrue();
    }

    @Test
    void isNameAvailable_WithUnavailableName() {
        String name = "bosse1";
        when(userDao.getByName(name)).thenReturn(
                Optional.of(new User(UserId.of(1), name, "Bosse Bredsladd")));

        UserService userService = new UserService(
                userDao, org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());

        assertThat(userService.isNameAvailable(name)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = "bo     ")
    @NullAndEmptySource
    void isNameAvailable_WithInvalidParam(String name) {
        UserService userService = new UserService(
                userDao, org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());

        assertThat(userService.isNameAvailable(name)).isFalse();
    }
}