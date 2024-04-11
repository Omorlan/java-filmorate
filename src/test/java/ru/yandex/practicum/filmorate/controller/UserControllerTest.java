package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.util.HttpStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerTest {

    private URI uri;
    private HttpClient client;
    private ConfigurableApplicationContext app;

    private HttpResponse<String> sendPostRequest(String requestBody) throws IOException, InterruptedException {
        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .setHeader("Accept", "application/json")
                .setHeader("Content-type", "application/json")
                .uri(uri)
                .build();
        HttpResponse.BodyHandler<String> handlerPost = HttpResponse.BodyHandlers.ofString();
        return client.send(requestPost, handlerPost);
    }

    private HttpResponse<String> sendPutRequest(String requestBody) throws IOException, InterruptedException {
        HttpRequest requestPut = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Accept", "application/json")
                .header("Content-type", "application/json")
                .uri(uri)
                .build();
        return client.send(requestPut, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeEach
    void setUp() {
        app = SpringApplication.run(FilmorateApplication.class);
        uri = URI.create("http://localhost:8080/users");
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @AfterEach
    void tearDown() {
        app.close();
    }

    @Nested
    @DisplayName("POST method tests")
    class PostTests {

        @Test
        @DisplayName("Creating a User Should Be Successful")
        void creatingUserShouldBeSuccessful() throws IOException, InterruptedException {
            String userData = "{\"login\": \"Oleg1955\"," +
                    "\"name\": \"Oleg\"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"1955-11-18\"}";
            HttpResponse<String> response = sendPostRequest(userData);
            assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
        }

        @Test
        void creatingUserWithoutNameShouldUseLoginAsName() throws IOException, InterruptedException {
            String userData = "{\"login\": \"Oleg1955\"," +
                    "\"name\": \" \"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"1955-11-18\"}";
            HttpResponse<String> response = sendPostRequest(userData);

            assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
            String responseBody = response.body();
            assertTrue(responseBody.contains("\"name\":\"Oleg1955\""));
        }

        @Test
        void creatingUserWithSameEmailShouldReturn500() throws IOException, InterruptedException {
            String userData = "{\"login\": \"Oleg1955\"," +
                    "\"name\": \"Oleg \"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"1955-11-18\"}";
            HttpResponse<String> response = sendPostRequest(userData);

            assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
            String userPavel = "{\"login\": \"Pavel\"," +
                    "\"name\": \"Pavel \"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"1955-11-18\"}";
            HttpResponse<String> response2 = sendPostRequest(userPavel);
            assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), response2.statusCode());

        }


        @ParameterizedTest
        @MethodSource("provideEmailData")
        @DisplayName("Creating a User With Diff Emails Should Return Correct Code")
        void creatingUserWithDiffEmailsShouldReturnCorrectCode(String email, int expectedStatusCode) throws IOException, InterruptedException {
            String userData = "{\"login\": \"Oleg1955\"," +
                    "\"name\": \"Oleg\"," +
                    "\"email\":\"" + email + "\"," +
                    "\"birthday\": \"1955-11-18\"}";
            HttpResponse<String> response = sendPostRequest(userData);
            assertEquals(expectedStatusCode, response.statusCode());
        }

        private static Stream<Object[]> provideEmailData() {
            return Stream.of(
                    new Object[]{"email", HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{"", HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{null, HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{"test@ya.ru", HttpStatusCode.OK.getCode()},
                    new Object[]{"test@.ru", HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{"test@yandex.com", HttpStatusCode.OK.getCode()},
                    new Object[]{"это-неправильный?эмейл@", HttpStatusCode.BAD_REQUEST.getCode()}
            );
        }

        @ParameterizedTest
        @MethodSource("provideLoginData")
        @DisplayName("Creating a User With Diff Logins Should Return Correct Code")
        void creatingUserWithDiffLoginsShouldReturnCorrectCode(String login, int expectedStatusCode) throws IOException, InterruptedException {
            String userData = "{\"login\":\"" + login + "\"," +
                    "\"name\": \"Oleg\"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"1955-11-18\"}";
            HttpResponse<String> response = sendPostRequest(userData);
            assertEquals(expectedStatusCode, response.statusCode());
        }

        private static Stream<Object[]> provideLoginData() {
            return Stream.of(
                    new Object[]{"Oleg88", HttpStatusCode.OK.getCode()},
                    new Object[]{"", HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{" Kaban", HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()},
                    new Object[]{"InternetGeroi ", HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()},
                    new Object[]{"Oleg User", HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()}
            );
        }

        @ParameterizedTest
        @MethodSource("provideBirthdayData")
        @DisplayName("Creating a User With Diff Birthdays Should Return Correct Code")
        void creatingUserWithDiffBirthdaysShouldReturnCorrectCode(String birthday, int expectedStatusCode) throws IOException, InterruptedException {
            String userData = "{\"login\": \"Oleg1955\"," +
                    "\"name\": \"Oleg\"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"" + birthday + "\"}";
            HttpResponse<String> response = sendPostRequest(userData);
            assertEquals(expectedStatusCode, response.statusCode());
        }

        private static Stream<Object[]> provideBirthdayData() {
            LocalDate currentDate = LocalDate.now();
            LocalDate previousDate = currentDate.minusDays(1);
            String previousDateString = previousDate.toString();
            return Stream.of(
                    new Object[]{"2014-11-11", HttpStatusCode.OK.getCode()},
                    new Object[]{"2052-11-11", HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{previousDateString, HttpStatusCode.OK.getCode()}
            );
        }
    }

    @Nested
    @DisplayName("PUT method tests")
    class PutTests {
        @ParameterizedTest
        @MethodSource("provideIdData")
        @DisplayName("Updating a Film Should Be Correct")
        void updatingUserShouldBeSuccessful(String id, int expectedStatusCode) throws IOException, InterruptedException {
            String userData = "{\"login\": \"Oleg1955\"," +
                    "\"name\": \"Oleg\"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"1955-11-18\"}";
            sendPostRequest(userData);
            String updatedUserData = "{\"id\": \"" + id + "\"," +
                    "\"login\": \"Oleg1955\"," +
                    "\"name\": \"Ya ne Oleg\"," +
                    "\"email\": \"Oleg1955@yandex.ru\"," +
                    "\"birthday\": \"1955-11-18\"}";
            HttpResponse<String> response = sendPutRequest(updatedUserData);
            assertEquals(expectedStatusCode, response.statusCode());
        }

        private static Stream<Object[]> provideIdData() {
            return Stream.of(
                    new Object[]{"1", HttpStatusCode.OK.getCode()},
                    new Object[]{"5", HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()},
                    new Object[]{"-7", HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()}
            );
        }
    }


}