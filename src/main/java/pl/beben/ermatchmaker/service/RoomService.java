package pl.beben.ermatchmaker.service;

import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.RoomType;
import pl.beben.ermatchmaker.pojo.IdentifiedRoomPojo;
import pl.beben.ermatchmaker.pojo.RoomDetails;
import pl.beben.ermatchmaker.pojo.RoomDraftPojo;

import java.util.List;

public interface RoomService {
  List<? extends IdentifiedRoomPojo> getActive(Game game, Platform platform, String hostQuery, String roomQuery, List<RoomType> roomTypes, List<String> locationIds);
  RoomDetails registerToAndGetDetails(Long id);

  Long createRoomReturningId(RoomDraftPojo room);

  void closeRoomOwnedByCurrentUser();

  void kickGuestFromRoomOwnerByCurrentUser(String guestUserName);

  Long pingReturningUpdateTimestamp(Long id);

  void leaveRoom(Long roomId);
}
