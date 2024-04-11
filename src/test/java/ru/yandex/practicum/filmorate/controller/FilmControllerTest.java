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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilmControllerTest {
    private static URI uri;
    private static HttpClient client;
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
        uri = URI.create("http://localhost:8080/films");
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
        @DisplayName("Creation Of The Correct Task Must Be Successful")
        void createCorrectTaskMustBeSuccessful() throws IOException, InterruptedException {
            String film = "{\"name\": \"Film\"," +
                    "\"description\": \"Film desc\"," +
                    "\"releaseDate\": \"2022-05-05\"," +
                    "\"duration\": \"120\"}";
            HttpResponse<String> response = sendPostRequest(film);
            assertEquals(HttpStatusCode.OK.getCode(), response.statusCode());
        }

        @Test
        void shouldReturn400WhenNameIsEmpty() throws IOException, InterruptedException {
            String film = "{\"name\": \"    \"," +
                    "\"description\": \"Film is about dreams\"," +
                    "\"releaseDate\": \"2010-07-22\"," +
                    "\"duration\": \"148\"}";
            HttpResponse<String> response = sendPostRequest(film);
            assertEquals(HttpStatusCode.BAD_REQUEST.getCode(), response.statusCode());
        }

        @ParameterizedTest
        @MethodSource("provideDescriptionData")
        @DisplayName("Should return 400 when description is wrong")
        void shouldReturn400WhenDescriptionIsWrong(String description, int expectedStatusCode) throws IOException, InterruptedException {
            String film = "{\"name\": \"Film\"," +
                    "\"description\": \"" + description + "\"," +
                    "\"releaseDate\": \"2010-07-22\"," +
                    "\"duration\": \"148\"}";
            HttpResponse<String> response = sendPostRequest(film);
            assertEquals(expectedStatusCode, response.statusCode());
        }

        private static Stream<Object[]> provideDescriptionData() {
            String longString190 = "a".repeat(190);
            String longString200 = "a".repeat(200);
            String longString210 = "a".repeat(210);
            return Stream.of(
                    new Object[]{"This is Film", HttpStatusCode.OK.getCode()},
                    new Object[]{" ", HttpStatusCode.OK.getCode()},
                    new Object[]{null, HttpStatusCode.OK.getCode()},
                    new Object[]{longString190, HttpStatusCode.OK.getCode()},
                    new Object[]{longString200, HttpStatusCode.OK.getCode()},
                    new Object[]{longString210, HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()},
                    new Object[]{"", HttpStatusCode.OK.getCode()}
            );
        }

        @ParameterizedTest
        @MethodSource("provideDurationData")
        void shouldReturnCorrectStatusCodeForNegativeDuration(String duration, int expectedStatusCode) throws IOException, InterruptedException {
            String film = "{\"name\": \"Film\"," +
                    "\"description\": \"Film desc\"," +
                    "\"releaseDate\": \"2011-11-12\"," +
                    "\"duration\": \"" + duration + "\"}";
            HttpResponse<String> response = sendPostRequest(film);
            assertEquals(expectedStatusCode, response.statusCode());
        }

        private static Stream<Object[]> provideDurationData() {
            return Stream.of(
                    new Object[]{"-5", HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{"0", HttpStatusCode.BAD_REQUEST.getCode()},
                    new Object[]{"5", HttpStatusCode.OK.getCode()}
            );
        }

        @ParameterizedTest
        @MethodSource("provideReleaseDateData")
        void shouldReturnCorrectStatusCode(String releaseDate, int expectedStatusCode) throws IOException, InterruptedException {
            String film = "{\"name\": \"Film\"," +
                    "\"description\": \"Film desc\"," +
                    "\"releaseDate\": \"" + releaseDate + "\"," +
                    "\"duration\": \"60\"}";
            HttpResponse<String> response = sendPostRequest(film);
            assertEquals(expectedStatusCode, response.statusCode());
        }

        private static Stream<Object[]> provideReleaseDateData() {
            return Stream.of(
                    new Object[]{"1895-12-28", HttpStatusCode.OK.getCode()},
                    new Object[]{"1895-12-27", HttpStatusCode.INTERNAL_SERVER_ERROR.getCode()},
                    new Object[]{"2015-11-20", HttpStatusCode.OK.getCode()}
            );
        }
    }

    @Nested
    @DisplayName("PUT method tests")
    class PutTests {
        @ParameterizedTest
        @MethodSource("provideIdData")
        @DisplayName("Updating a Film Should Be Correct")
        void updatingFilmShouldBeSuccessful(String id, int expectedStatusCode) throws IOException, InterruptedException {
            String film = "{\"name\": \"Film\"," +
                    "\"description\": \"Film desc\"," +
                    "\"releaseDate\": \"2022-05-05\"," +
                    "\"duration\": \"120\"}"; //Тело запроса
            sendPostRequest(film);
            String updatedFilmData = "{\"id\": \"" + id + "\"," +
                    "\"name\": \"Updated Film\"," +
                    "\"description\": \"Updated description\"," +
                    "\"releaseDate\": \"2022-01-01\"," +
                    "\"duration\": 120}";
            HttpResponse<String> response = sendPutRequest(updatedFilmData);
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