package pl.beben.ermatchmaker.service;

import org.springframework.web.context.request.async.DeferredResult;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.RoomType;
import pl.beben.ermatchmaker.pojo.IdentifiedRoomPojo;
import pl.beben.ermatchmaker.pojo.RoomDetails;
import pl.beben.ermatchmaker.pojo.RoomDraftPojo;
import pl.beben.ermatchmaker.pojo.event.AbstractEvent;

import java.util.List;

public interface RoomService {

  void subscribeToRoomEvent(Long roomId, DeferredResult<List<AbstractEvent>> deferredResult);

  void subscribeToGeneralEvent(Game game, Platform platform, DeferredResult<List<AbstractEvent>> deferredResult);

  List<? extends IdentifiedRoomPojo> getActive(Game game, Platform platform, String hostQuery, String roomQuery, List<RoomType> roomTypes, List<String> locationIds);

  RoomDetails registerToAndGetDetails(Long id);

  Long createRoomReturningId(RoomDraftPojo room);

  void closeRoomOwnedByCurrentUser();

  void kickGuestFromRoomOwnerByCurrentUser(String guestUserName);

  void ping(Long id);

  void leaveRoom(Long roomId);
}
