package se.yrgo.libraryapp.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class UsernameTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "bosse",
            "abcd",
            "CAPS",
            "1234567890sdfgGFDSA",
            "@@__--.."
    })
    void checkWithCorrectUsername(String username) {
        assertThat(Username.validate(username)).isTrue();
    }

    @Test
    void incorrectUsername() {
        assertThat(Username.validate("name with space")).isFalse();
    }
}
