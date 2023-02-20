package org.kquiet.hecate.telegram;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
  @Operation(summary = """
      Send photo(url or file) through telegram; url take precedence over file if both are supplied
      """)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200",
          description = "request was executed; see response body(boolean value) for send result"),
      @ApiResponse(responseCode = "4xx/5xx", description = "bad request or other error",
          content = {@Content(examples = {@ExampleObject("""
              {"timestamp":1676862796078,"path":"/telegram/sendPhoto","status":415
              ,"error":"Unsupported Media Type","requestId":"a5076391-14"}
              """)})})})
  @PostMapping(path = "/sendPhoto", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public Mono<Boolean> sendPhoto(
      @Parameter(description = "request in json format; req.photo accepts photo url",
          content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}) @RequestPart(
              name = "req") SendPhotoRequest req,
      @Parameter(description = "photo file") @RequestPart(name = "photoPart",
          required = false) Mono<FilePart> photoPart) {
    return service.sendPhoto(req,
        photoPart != null ? photoPart.flatMapMany(s -> s.content()) : Flux.empty());
  }
}
