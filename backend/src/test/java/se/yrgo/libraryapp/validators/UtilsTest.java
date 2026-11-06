package se.yrgo.libraryapp.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

    @ParameterizedTest
    @CsvSource(value = {
            "John Doe, john doe",
            "He123 j, he j",
            "___BOSSE, bosse",
            ""
    })
    void testOnlyLettersAndWhitespace_WithCorrectInput(String input, String output) {
        assertThat(output).isEqualTo(Utils.onlyLettersAndWhitespace(input));
    }
}