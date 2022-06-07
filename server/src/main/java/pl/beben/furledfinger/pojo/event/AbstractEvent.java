package pl.beben.furledfinger.pojo.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public abstract class AbstractEvent<PAYLOAD> implements Serializable {
  private static final long serialVersionUID = 4422540240210492581L;

  public enum Type {
    CHAT_MESSAGE,
    USER_HAS_JOINED,
    USER_HAS_LEFT,
    USER_IS_ONLINE,
    USER_IS_OFFLINE,

    ROOM_HAS_BEEN_CREATED,
    ROOM_HAS_BEEN_REMOVED
  }

  Type type;
  PAYLOAD payload;
  
}
