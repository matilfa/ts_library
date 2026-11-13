package se.yrgo.libraryapp.integrations;

import io.jooby.JoobyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import se.yrgo.libraryapp.App;
import se.yrgo.libraryapp.entities.LoginInfo;
import se.yrgo.libraryapp.entities.Role;
import se.yrgo.libraryapp.entities.UserId;
import se.yrgo.libraryapp.entities.forms.LoginData;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Tag("foobar")
@Testcontainers
@JoobyTest(value = App.class, factoryMethod = "createApp")
public class LoginLogoutIntegrationTest {
    @Container
    private static MySQLContainer<?> mySQLContainer = new MySQLContainer<>
            ("mysql:8.0.29");

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

    @Test
    public void alwaysOkToLogout(int serverPort) throws IOException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" +
                    serverPort + "/logout")).build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            assertEquals("", resp.body());
            assertEquals(HttpURLConnection.HTTP_OK, resp.statusCode());
        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }
    }

    @Test
    public void loginAndLogout(int serverPort) throws IOException, InterruptedException {
        LoginData loginData = new LoginData(
                "test", "yrgoP4ssword");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(loginData);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest loginRequest = HttpRequest.newBuilder(URI.create("http://localhost:" +
                        serverPort + "/login"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpRequest logoutRequest = HttpRequest.newBuilder(URI.create("http://localhost:" +
                        serverPort + "/logout"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> respLogin = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(HttpURLConnection.HTTP_OK, respLogin.statusCode());
//            assertTrue(checkIfLoggedIn(client, serverPort));

            HttpResponse<String> respLogout = client.send(logoutRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals(HttpURLConnection.HTTP_OK, respLogin.statusCode());

        } catch (IllegalArgumentException | InterruptedException ex) {
            fail(ex);
        }

    }

//    private boolean checkIfLoggedIn(HttpClient client, int serverPort) throws IOException, InterruptedException {
//        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" +
//                        serverPort + "/login/check")).build();
//
//        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
//
//        return resp.statusCode() == HttpURLConnection.HTTP_OK &&
//                resp.body().contains(Role.USER.toString());
//    }
}
