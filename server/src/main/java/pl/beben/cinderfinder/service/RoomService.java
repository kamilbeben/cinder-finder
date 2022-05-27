package pl.beben.cinderfinder.service;

import lombok.NonNull;
import org.springframework.web.context.request.async.DeferredResult;
import pl.beben.cinderfinder.domain.Game;
import pl.beben.cinderfinder.domain.Platform;
import pl.beben.cinderfinder.domain.RoomType;
import pl.beben.cinderfinder.pojo.IdentifiedRoomPojo;
import pl.beben.cinderfinder.pojo.RoomDetails;
import pl.beben.cinderfinder.pojo.RoomDraftPojo;
import pl.beben.cinderfinder.pojo.event.AbstractEvent;

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
