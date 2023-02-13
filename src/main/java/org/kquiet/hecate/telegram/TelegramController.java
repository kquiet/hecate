package org.kquiet.hecate.telegram;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Telegram controller.
 *
 * @author monkey
 *
 */
@RestController
@RequestMapping("/telegram")
public class TelegramController {
  @Autowired
  TelegramService service;

  /**
   * send photo by request.
   *
   * @param req if both imageUrl and photo are supplied in {#SendPhotoRequest}, imageUrl would take
   *        precedence.
   * @return result
   */
  @PostMapping(path = "/sendPhoto",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
  @WithSpan
  public Mono<Boolean> sendPhoto(
      @RequestPart(name = "req") @SpanAttribute("req") SendPhotoRequest req,
      @RequestPart(name = "photoPart", required = false) Mono<FilePart> photoPart) {
    return service.sendPhoto(req,
        photoPart != null ? photoPart.flatMapMany(s -> s.content()) : Flux.empty());
  }
}
