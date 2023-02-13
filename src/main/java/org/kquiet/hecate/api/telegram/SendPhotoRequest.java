package org.kquiet.hecate.api.telegram;

/**
 * Request for telegram sendPhoto api.
 *
 * @author monkey
 *
 */
public class SendPhotoRequest {
  private String token;
  private String chatId;
  private String photo;
  private String caption;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  public String getPhoto() {
    return photo;
  }

  public void setPhoto(String photo) {
    this.photo = photo;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }
}
