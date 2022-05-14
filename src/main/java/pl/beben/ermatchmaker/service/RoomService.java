package pl.beben.ermatchmaker.service;

import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.pojo.IdentifiedRoomPojo;
import pl.beben.ermatchmaker.pojo.RoomDetails;
import pl.beben.ermatchmaker.pojo.RoomDraftPojo;

import java.util.List;

public interface RoomService {
  List<? extends IdentifiedRoomPojo> getActive(Game game, Platform platform);
  RoomDetails registerToAndGetDetails(Long id);

  Long createNewRoom(RoomDraftPojo room);

  void closeRoomOwnedByCurrentUser();

  Long pingReturningUpdateTimestamp(Long id);
}
