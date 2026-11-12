package se.yrgo.libraryapp.services;

import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.yrgo.libraryapp.dao.UserDao;
import se.yrgo.libraryapp.entities.*;

public class UserService {
    private static Logger logger = LoggerFactory.getLogger(UserDao.class);

    private UserDao userDao;
    private PasswordEncoder encoder;

    @Inject
    UserService(UserDao userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
    }

    public Optional<UserId> validate(String username, String password) {
        Optional<LoginInfo> maybeLoginInfo = userDao.getLoginInfo(username);
        if (maybeLoginInfo.isEmpty()) {
            return Optional.empty();
        }

        LoginInfo loginInfo = maybeLoginInfo.get();

        if (!encoder.matches(password, loginInfo.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(loginInfo.getUserId());
    }

    public boolean register(String name, String realname, String password) {
        if (name == null || name.isEmpty()) return false;
        if (realname == null || realname.isEmpty()) return false;
        if (password == null || password.isEmpty()) return false;

        String passwordHash = encoder.encode(password);

        // handle names like Ian O'Toole
        realname = realname.replace("'", "\\'");

        try {
            return userDao.insertUserAndRole(name, realname, passwordHash);
        } catch (SQLException ex) {
            logger.error("Unable to register user " + name, ex);
            return false;
        }
    }

    public boolean isNameAvailable(String name) {
        if (name == null || name.trim().length() < 3) {
            return false;
        }

        Optional<User> existingUser = userDao.getByName(name);
        if (existingUser.isPresent() && existingUser.get().getName().equals(name)) {
            return false;
        }
        return true;
    }
}
