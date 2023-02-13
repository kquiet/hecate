package org.kquiet.hecate.api.telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * SendPhotoRequest unit test.
 *
 * @author monkey
 *
 */
public class SendPhotoRequestTest {
  static class ChildSendPhotoRequest extends SendPhotoRequest {
  }

  @ParameterizedTest()
  @CsvSource({"-123456789, 987654321:XXXXXXX, test1, https://localhost/path/to/sendPhotoTest.jpg"})
  @Order(1)
  void objectBasicTest(String chatId, String token, String caption, String photo) {
    SendPhotoRequest obj1 = new SendPhotoRequest();
    obj1.setCaption(caption);
    obj1.setChatId(chatId);
    obj1.setPhoto(photo);
    obj1.setToken(token);

    SendPhotoRequest obj2 = new SendPhotoRequest();
    obj2.setCaption(caption);
    obj2.setChatId(chatId);
    obj2.setPhoto(photo);
    obj2.setToken(token);

    ChildSendPhotoRequest obj3 = new ChildSendPhotoRequest();
    obj3.setCaption(caption);
    obj3.setChatId(chatId);
    obj3.setPhoto(photo);
    obj3.setToken(token);

    assertEquals(obj1.toString(), obj2.toString());
    assertEquals(obj1.hashCode(), obj2.hashCode());
    assertNotEquals(obj1.hashCode(), obj3.hashCode());
    assertTrue(obj1.equals(obj2));
    assertTrue(obj1.equals(obj1));
    assertFalse(obj1.equals(null));
    assertFalse(obj1.equals(obj3));
  }
}
