package org.kquiet.hecate.telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kquiet.hecate.HecateConfig;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

/**
 * TelegramDao unit test.
 *
 * @author monkey
 *
 */
@ExtendWith(MockitoExtension.class)
public class TelegramDaoTest {
  private static MockWebServer mockWebServer;

  @TempDir
  static Path tempDir;

  @Mock
  HecateConfig hecateConfig;

  @InjectMocks
  TelegramDao dao;

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
      "-123456789, 987654321:XXXXXXX, test11, https://localhost/path/to/sendPhotoTest.jpg, ",
      "-234567890, 098765432:YYYYY, test12, , sendPhotoTest.png"})
  @Order(1)
  void sendPhotoTest(String chatId, String token, String caption, String photo,
      String photoFileName) throws IOException, InterruptedException {
    String sendPhotoUrl = mockWebServer.url("/bot%s/sendPhoto").toString();
    when(hecateConfig.getTelegramSendPhotoUrl()).thenReturn(sendPhotoUrl);
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"ok\": true}"));

    SendPhotoRequest req = new SendPhotoRequest();
    req.setChatId(chatId);
    req.setToken(token);
    req.setCaption(caption);
    req.setPhoto(photo);

    FirstStep<Boolean> firstStep;
    if (photo != null && !photo.isBlank()) {
      firstStep = StepVerifier.create(dao.sendPhoto(req, Flux.<DataBuffer>empty()));
    } else {
      Flux<DataBuffer> sourceFlux =
          DataBufferUtils
              .readAsynchronousFileChannel(
                  () -> AsynchronousFileChannel.open(
                      Files.createFile(tempDir.resolve(photoFileName)), StandardOpenOption.READ),
                  new DefaultDataBufferFactory(), 512 * 1024);
      firstStep = StepVerifier.create(dao.sendPhoto(req, sourceFlux));
    }
    firstStep.assertNext(s -> assertEquals(true, s)).verifyComplete();

    // assert request
    RecordedRequest request = mockWebServer.takeRequest();
    assertEquals("POST", request.getMethod());
    assertEquals(String.format(sendPhotoUrl, token), request.getRequestUrl().toString());
    assertEquals(0, request.getHeader("Content-Type").indexOf("multipart/form-data;boundary="));

    String requestBody = request.getBody().readUtf8().replaceAll("\r\n", System.lineSeparator());
    String chatIdBody = String.format("""
        Content-Disposition: form-data; name="chat_id"
        Content-Type: text/plain;charset=UTF-8
        Content-Length: %s

        %s""", chatId.length(), chatId);
    assertEquals(true, requestBody.indexOf(chatIdBody) > 0);

    String captionBody = String.format("""
        Content-Disposition: form-data; name="caption"
        Content-Type: text/plain;charset=UTF-8
        Content-Length: %s

        %s""", caption.length(), caption);
    assertEquals(true, requestBody.indexOf(captionBody) > 0);

    String photoBody;
    if (photo != null && !photo.isBlank()) {
      photoBody = String.format("""
          Content-Disposition: form-data; name="photo"
          Content-Type: text/plain;charset=UTF-8
          Content-Length: %s

          %s""", photo.length(), photo);

    } else {
      photoBody = """
          Content-Disposition: form-data; name="photo"; filename="somefilename"
          """;
    }
    assertEquals(true, requestBody.indexOf(photoBody) > 0);
  }
}
