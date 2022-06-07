package pl.beben.furledfinger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import pl.beben.furledfinger.domain.Game;
import pl.beben.furledfinger.domain.Platform;
import pl.beben.furledfinger.domain.RoomType;
import pl.beben.furledfinger.pojo.*;
import pl.beben.furledfinger.pojo.event.AbstractEvent;
import pl.beben.furledfinger.service.RoomService;
import pl.beben.furledfinger.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

  private final RoomService roomService;
  private final UserService userService;

  // user

  @GetMapping("/user/me")
  public UserPojo getLoggedUser() {
    return userService.getCurrentUser();
  }

  @PostMapping("/user/in_game_name")
  public void setInGameName(@RequestParam("value") String inGameName) {
    userService.setInGameName(inGameName);
  }

  @PostMapping("/user/last_selected_platform")
  public void setInGameName(@RequestParam("value") Platform lastSelectedPlatform) {
    userService.setLastSelectedPlatform(lastSelectedPlatform);
  }

  // specific room

  @PostMapping("/room/create_returning_id")
  public Long createRoomReturningId(@RequestBody RoomDraftPojo room) {
    return roomService.createRoomReturningId(room);
  }

  @DeleteMapping("/room/owned_by_current_user/close")
  public void removeRoom() {
    roomService.closeRoomOwnedByCurrentUser();
  }

  @PostMapping("/room/register_to_and_get_details")
  public RoomDetails registerToAndGetDetails(@RequestParam("id") Long id) {
    return roomService.registerToAndGetDetails(id);
  }

  @GetMapping("/room/subscribe_to_event")
  public DeferredResult<List<AbstractEvent>> subscribeToRoomEvent(@RequestParam("id") Long id) {
    final var result = new DeferredResult<List<AbstractEvent>>();
    roomService.subscribeToRoomEvent(id, result);
    return result;
  }

  @DeleteMapping("/room/owned_by_current_user/kick_guest")
  public void kickGuest(@RequestParam("guest_user_name") String guestUserName) {
    roomService.kickGuestFromRoomOwnerByCurrentUser(guestUserName);
  }

  @PostMapping("/room/ping")
  public void pingReturningUpdateTimestamp(@RequestParam("id") Long id) {
    roomService.ping(id);
  }

  @DeleteMapping("/room/leave")
  public void leaveRoom(@RequestParam("id") Long id) {
    roomService.leave(id);
  }

  @PostMapping("/room/message")
  public void addMessage(@RequestParam("id") Long roomId, @RequestBody ChatMessageRequest chatMessageRequest) {
    roomService.addMessage(roomId, chatMessageRequest.getContent());
  }

  // all rooms

  // TODO restrict serialized properties to IdentifiedRoomPojo (ignore "guests", "chatMessages", etc)
  @GetMapping("/room/all")
  public List<IdentifiedRoomPojo> searchRooms(@RequestParam(name = "game") Game game,
                                              @RequestParam(name = "platform") Platform platform,
                                              @RequestParam(name = "host_query", required = false) String hostQuery,
                                              @RequestParam(name = "room_query", required = false) String roomQuery,
                                              @RequestParam(name = "room_type", required = false) List<RoomType> roomTypes,
                                              @RequestParam(name = "location_ids", required = false) List<String> locationIds,
                                              @RequestParam(name = "min_host_level", required = false) Integer minHostLevel,
                                              @RequestParam(name = "max_host_level", required = false) Integer maxHostLevel) {

    return roomService.searchRooms(game, platform, hostQuery, roomQuery, roomTypes, locationIds, minHostLevel, maxHostLevel);
  }

  @GetMapping("/room/all/subscribe_to_event")
  public DeferredResult<List<AbstractEvent>> subscribeToGeneralEvent(@RequestParam(name = "game") Game game,
                                                                     @RequestParam(name = "platform") Platform platform) {

    final var result = new DeferredResult<List<AbstractEvent>>();
    roomService.subscribeToGeneralEvent(game, platform, result);
    return result;
  }
}
