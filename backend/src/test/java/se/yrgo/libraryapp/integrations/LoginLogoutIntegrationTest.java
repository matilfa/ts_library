package se.yrgo.libraryapp.integrations;

import io.jooby.JoobyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import se.yrgo.libraryapp.App;
import se.yrgo.libraryapp.entities.forms.LoginData;
import se.yrgo.libraryapp.entities.forms.RegisterUserData;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("integration")
@Tag("foobar")
@Testcontainers
@JoobyTest(value = App.class, factoryMethod = "createApp")
public class LoginLogoutIntegrationTest {
    @Container
    private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>
            ("mysql:8.0.29");

    private static final String LOGIN_ENDPOINT = "/login";
    private static final String LOGOUT_ENDPOINT = "/logout";
    private static final String REGISTER_ENDPOINT = "/register";


    public static App createApp() {
        System.setProperty("db.url", mySQLContainer.getJdbcUrl());
        System.setProperty("db.user", mySQLContainer.getUsername());
        System.setProperty("db.password", mySQLContainer.getPassword());
        return new App();
    }

    @AfterAll
    static void reset() {
        System.clearProperty("db.url");
        System.clearProperty("db.user");
        System.clearProperty("db.password");
    }

    private HttpResponse<String> sendHttpRequest(HttpRequest req) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGetRequest(int serverPort,
                                                String endpoint) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" +
                serverPort + endpoint)).build();

        return sendHttpRequest(req);
    }

    private HttpResponse<String> sendPostRequest(int serverPort,
                                                 String endpoint,
                                                 String jsonBody) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = null;

        if (jsonBody == null || jsonBody.isEmpty()) {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonBody);
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" +
                        serverPort + endpoint))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(bodyPublisher)
                .build();

        return sendHttpRequest(req);
    }

    @Test
    public void alwaysOkToLogout(int serverPort) throws IOException {
        try {
            var resp = sendGetRequest(serverPort, LOGOUT_ENDPOINT);
            assertEquals("", resp.body());
            assertEquals(HttpURLConnection.HTTP_OK, resp.statusCode());
        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }
    }

    private String getUserDataAsJsonString(String username, String password) throws JsonProcessingException {
        LoginData loginData = new LoginData(username, password);
        return getJsonString(loginData);
    }

    private String getUserDataAsJsonString(String username,
                                           String realName,
                                           String password) throws JsonProcessingException {
        RegisterUserData userData = new RegisterUserData(username, realName, password);
        return getJsonString(userData);
    }

    private <T> String getJsonString(T bodyData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(bodyData);
    }

    @Test
    public void loginAndLogout(int serverPort) throws IOException {
        String json = getUserDataAsJsonString("test", "yrgoP4ssword");

        try {
            var loginResponse = sendPostRequest(serverPort, LOGIN_ENDPOINT, json);
            assertEquals(HttpURLConnection.HTTP_OK, loginResponse.statusCode());
        } catch (IllegalArgumentException | InterruptedException e) {
            fail(e);
        }

        try {
            var logoutResponse = sendPostRequest(serverPort, LOGOUT_ENDPOINT, null);
            assertEquals(HttpURLConnection.HTTP_OK, logoutResponse.statusCode());
        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }
    }

    @Test
    public void invalidUserLogin_shouldReturn401(int serverPort) throws IOException {
        String jsonInvalidUsername = getUserDataAsJsonString("someRandomUsername", "yrgoP4ssword");
        String jsonInvalidPassword = getUserDataAsJsonString("test", "yrgopassword");

        try {
            var loginResponse = sendPostRequest(serverPort, LOGIN_ENDPOINT, jsonInvalidUsername);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, loginResponse.statusCode());

            loginResponse = sendPostRequest(serverPort, LOGIN_ENDPOINT, jsonInvalidPassword);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, loginResponse.statusCode());
        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }
    }

    @Test
    public void registerNonExistingUser(int serverPort) throws IOException {
        String json = getUserDataAsJsonString("newUser", "John Doe", "verysecret");

        try {
            var resp = sendPostRequest(serverPort, REGISTER_ENDPOINT, json);
            assertTrue(resp.body().contains("true"));
        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }
    }

    @Test
    public void registerExistingUser(int serverPort) throws IOException {
        String json = getUserDataAsJsonString("test", "unknown", "yrgoP4ssword");

        try {
            var resp = sendPostRequest(serverPort, REGISTER_ENDPOINT, json);
            assertTrue(resp.body().contains("false"));
        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }
    }

    @Test
    public void registerUserWithInvalidParams(int serverPort) throws IOException {
        String json = getUserDataAsJsonString("", "  ", null);

        try {
            var resp = sendPostRequest(serverPort, REGISTER_ENDPOINT, json);
            assertTrue(resp.body().contains("false"));
        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }
    }
}
