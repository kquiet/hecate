package org.kquiet.hecate.telegram;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.kquiet.hecate.api.telegram.SendPhotoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Telegram service.
 *
 * @author monkey
 *
 */
@Component
public class TelegramService {
  @Autowired
  private TelegramDao dao;

  @WithSpan
  public Mono<Boolean> sendPhoto(@SpanAttribute("req") SendPhotoRequest req,
      Flux<DataBuffer> photoStream) {
    return dao.sendPhoto(req, photoStream);
  }
}
