package se.yrgo.libraryapp.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class UsernameTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "bosse",
            "abcd",
            "CAPS",
            "1234567890sdfgGFDSA",
            "@@__--..",
            "aZ_7kP-2mN@9xQ.rT_4vB-8cL@1wE.6yH_3pD-0sF@5jK.2nM_9qR-7tX@4bCaaaaaaaaa" //70 chars
    })
    void checkValidate_WithCorrectUsername(String username) {
        assertThat(Username.validate(username)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "name with space",
            "abc",
            "!!##%%abc",
            "aZ_7kP-2mN@9xQ.rT_4vB-8cL@1wE.6yH_3pD-0sF@5jK.2nM_9qR-7tX@4bCaaaaaaaaab", //71 chars
            "åäöÅÄÖ"
    })
    @NullAndEmptySource
    void checkValidate_WithIncorrectUsername(String username) {
        assertThat(Username.validate(username)).isFalse();
    }
}
