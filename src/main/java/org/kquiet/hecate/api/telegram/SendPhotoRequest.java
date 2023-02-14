package org.kquiet.hecate.api.telegram;

import java.util.Objects;

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

  @Override
  public String toString() {
    return "SendPhotoRequest [token=" + token + ", chatId=" + chatId + ", photo=" + photo
        + ", caption=" + caption + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getClass(), caption, chatId, photo, token);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SendPhotoRequest other = (SendPhotoRequest) obj;
    return Objects.equals(caption, other.caption) && Objects.equals(chatId, other.chatId)
        && Objects.equals(photo, other.photo) && Objects.equals(token, other.token);
  }
}
