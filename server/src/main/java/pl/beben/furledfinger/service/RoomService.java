package pl.beben.furledfinger.service;

import lombok.NonNull;
import org.springframework.web.context.request.async.DeferredResult;
import pl.beben.furledfinger.domain.Game;
import pl.beben.furledfinger.domain.Platform;
import pl.beben.furledfinger.domain.RoomType;
import pl.beben.furledfinger.pojo.IdentifiedRoomPojo;
import pl.beben.furledfinger.pojo.RoomDetails;
import pl.beben.furledfinger.pojo.RoomDraftPojo;
import pl.beben.furledfinger.pojo.event.AbstractEvent;

import java.util.List;

public interface RoomService {

  // specific room
  Long createRoomReturningId(RoomDraftPojo room);

  void closeRoomOwnedByCurrentUser();

  RoomDetails registerToAndGetDetails(Long id);

  void subscribeToRoomEvent(Long roomId, DeferredResult<List<AbstractEvent>> deferredResult);

  void kickGuestFromRoomOwnerByCurrentUser(String guestUserName);

  void ping(Long id);

  void leave(Long id);

  void addMessage(Long roomId, String content);

  // all rooms
  List<IdentifiedRoomPojo> searchRooms(@NonNull Game game,
                                       @NonNull Platform platform,
                                       String hostQuery,
                                       String roomQuery,
                                       List<RoomType> roomTypes,
                                       List<String> locationIds,
                                       Integer minHostLevel,
                                       Integer maxHostLevel);

  void subscribeToGeneralEvent(Game game, Platform platform, DeferredResult<List<AbstractEvent>> deferredResult);
}
