package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.util.Optional;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import se.yrgo.libraryapp.entities.*;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserDaoTest {
    @Mock
    private DataSource ds;
    @Mock
    private Connection conn;
    @Mock
    private Statement stmt;
    @Mock
    private ResultSet rs;

    @Test
    void getExistingLoginInfo() throws SQLException {
        final String username = "test";
        final UserId id = UserId.of(1);
        final String passwordHash =
                "$argon2i$v=19$m=16,t=2,p=1$QldXU09Sc2dzOWdUalBKQw$LgKb6x4usOpDLTlXCBVhxA";

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(contains(username))).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(id.getId());
        when(rs.getString("password_hash")).thenReturn(passwordHash);

        UserDao userDao = new UserDao(ds);
        LoginInfo info = userDao.getLoginInfo(username).get();
        assertThat(info.getUserId()).isEqualTo(id);
        assertThat(info.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    void getNonExistingLoginInfo() throws SQLException {
        final String username = "test";

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(contains(username))).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        UserDao userDao = new UserDao(ds);
        assertThat(userDao.getLoginInfo(username)).isEmpty();
    }

    @Test
    void getExistingUser() throws SQLException {
        final String userId = "1";
        final UserId id = UserId.of(userId);
        final String username = "testuser";
        final String realname = "bosse";
        final User expectedUser = new User(id, username, realname);

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("user")).thenReturn(username);
        when(rs.getString("realname")).thenReturn(realname);

        UserDao userDao = new UserDao(ds);
        assertThat(userDao.get(userId)).isEqualTo(Optional.of(expectedUser));
    }

    @Test
    void getNonExistingUser() throws SQLException {
        final String username = "testuser";

        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        UserDao userDao = new UserDao(ds);
        assertThat(userDao.get(username)).isEmpty();
    }



    @ParameterizedTest
    @ValueSource(strings = {
            "-1",
            "abc"
    })
    @NullAndEmptySource
    void getUserByInvalidIdParam(String id) throws SQLException {
        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);

        doThrow(new SQLException()).when(stmt).executeQuery(anyString());

        UserDao userDao = new UserDao(ds);
        assertThat(userDao.get(id)).isEqualTo(Optional.empty());
    }

    //Todo: Somehow check for SQL injection

}