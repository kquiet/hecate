package org.kquiet.hecate.telegram;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.hecate.HecateConfig;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Telegram data access object.
 *
 * @author monkey
 *
 */
@Component
public class TelegramDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(TelegramDao.class);

  @Autowired
  private HecateConfig hecateConfig;

  /**
   * call telegram sendPhoto api.
   *
   * @param req request
   * @return result
   */
  @WithSpan
  public Mono<Boolean> sendPhoto(@SpanAttribute("req") SendPhotoRequest req,
      Flux<DataBuffer> photoStream) {
    WebClient client =
        WebClient.create(String.format(hecateConfig.getTelegramSendPhotoUrl(), req.getToken()));
    Mono<Boolean> result = client.post().contentType(MediaType.MULTIPART_FORM_DATA)
        .body((outputMessage, context) -> Mono.defer(() -> {
          MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
          bodyBuilder.part("chat_id", req.getChatId());
          bodyBuilder.part("caption", req.getCaption());

          if (req.getPhoto() != null && !req.getPhoto().isBlank()) {
            bodyBuilder.part("photo", req.getPhoto());
          } else {
            bodyBuilder.asyncPart("photo", photoStream, DataBuffer.class).filename("somefilename");
          }

          return BodyInserters.fromMultipartData(bodyBuilder.build()).insert(outputMessage,
              context);
        })).exchangeToMono(response -> {
          int statusCode = response.statusCode().value();
          Mono<Boolean> returnMono = Mono.just(statusCode == 200);
          if (LOGGER.isDebugEnabled()) {
            Mono<String> bodyMono = response.bodyToMono(String.class);
            bodyMono.subscribe(body -> LOGGER.debug("sendPhoto result:{} {}", statusCode, body));
          }
          return returnMono;
        });
    return result;
  }
}
