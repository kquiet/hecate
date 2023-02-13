package org.kquiet.hecate.telegram;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.hecate.HecateConfig;
import org.kquiet.hecate.Launcher;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Telegram integration test.
 *
 * @author monkey
 *
 */
@SpringBootTest(classes = {Launcher.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
@EnableAutoConfiguration()
@AutoConfigureWebTestClient
@DirtiesContext
public class TelegramIntegrationTest {
  private static MockWebServer mockWebServer;

  @TempDir
  static Path tempDir;

  @Autowired
  HecateConfig hecateConfig;

  @LocalServerPort
  private int port;

  @Autowired
  WebTestClient webTestClient;

  @BeforeAll
  static void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @ParameterizedTest()
  @CsvSource({
      "-123456789, 987654321:XXXXXXX, test21, https://localhost/path/to/controllerSendPhotoThroughTest.jpg, ",
      "-234567890, 098765432:YYYYY, test22, , controllerSendPhotoThroughTest.png"})
  @Order(1)
  void controllerSendPhotoThroughTest(String chatId, String token, String caption, String photo,
      String photoFileName) throws IOException {
    String sendPhotoUrl = mockWebServer.url("/bot%s/sendPhoto").toString();
    hecateConfig.setTelegramSendPhotoUrl(sendPhotoUrl);
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"ok\": true}"));

    SendPhotoRequest req = new SendPhotoRequest();
    req.setChatId(chatId);
    req.setToken(token);
    req.setCaption(caption);
    req.setPhoto(photo);

    webTestClient.post().uri("http://localhost:" + port + "/telegram/sendPhoto")
        .body((outputMessage, context) -> Mono.defer(() -> {
          MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
          bodyBuilder.part("req", req).contentType(MediaType.APPLICATION_JSON);

          if (photo == null || photo.isBlank()) {
            Flux<DataBuffer> sourceFlux =
                DataBufferUtils.readAsynchronousFileChannel(
                    () -> AsynchronousFileChannel.open(
                        Files.createFile(tempDir.resolve(photoFileName)), StandardOpenOption.READ),
                    new DefaultDataBufferFactory(), 512 * 1024);
            bodyBuilder.asyncPart("photo", sourceFlux, DataBuffer.class).filename("somefilename");
          }

          return BodyInserters.fromMultipartData(bodyBuilder.build()).insert(outputMessage,
              context);
        })).exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("true");
  }
}
