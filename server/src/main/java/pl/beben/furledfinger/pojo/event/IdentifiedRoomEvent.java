package pl.beben.furledfinger.pojo.event;

import pl.beben.furledfinger.pojo.IdentifiedRoomPojo;

public class IdentifiedRoomEvent extends AbstractEvent<IdentifiedRoomPojo> {
  private static final long serialVersionUID = -210349848803605887L;

  public IdentifiedRoomEvent(Type type, IdentifiedRoomPojo identifiedRoomPojo) {
    super(type, identifiedRoomPojo);
  }
}
