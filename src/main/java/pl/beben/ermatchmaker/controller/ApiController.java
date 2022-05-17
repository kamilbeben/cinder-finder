package pl.beben.ermatchmaker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.RoomType;
import pl.beben.ermatchmaker.pojo.IdentifiedRoomPojo;
import pl.beben.ermatchmaker.pojo.RoomDetails;
import pl.beben.ermatchmaker.pojo.RoomDraftPojo;
import pl.beben.ermatchmaker.pojo.UserPojo;
import pl.beben.ermatchmaker.service.RoomService;
import pl.beben.ermatchmaker.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

  private final RoomService roomService;
  private final UserService userService;

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

  @PostMapping("/room/register_to_and_get_details")
  public RoomDetails registerToAndGetDetails(@RequestParam("id") Long id) {
    return roomService.registerToAndGetDetails(id);
  }

  @PostMapping("/room/ping_returning_update_timestamp")
  public Long pingReturningUpdateTimestamp(@RequestParam("id") Long id) {
    return roomService.pingReturningUpdateTimestamp(id);
  }

  @PostMapping("/room/create_returning_id")
  public Long createRoomReturningId(@RequestBody RoomDraftPojo room) {
    return roomService.createRoomReturningId(room);
  }

  @DeleteMapping("/room/leave")
  public void leaveRoom(@RequestParam("id") Long id) {
    roomService.leaveRoom(id);
  }

  @DeleteMapping("/room/owned_by_current_user/close")
  public void removeRoom() {
    roomService.closeRoomOwnedByCurrentUser();
  }
  
  @DeleteMapping("/room/owned_by_current_user/kick_guest")
  public void kickGuest(@RequestParam("guest_user_name") String guestUserName) {
    roomService.kickGuestFromRoomOwnerByCurrentUser(guestUserName);
  }

  @GetMapping("/room/all")
  public List<? extends IdentifiedRoomPojo> getActiveRooms(@RequestParam(name = "game") Game game,
                                                           @RequestParam(name = "platform") Platform platform,
                                                           @RequestParam(name = "host_query", required = false) String hostQuery,
                                                           @RequestParam(name = "room_query", required = false) String roomQuery,
                                                           @RequestParam(name = "room_type", required = false) List<RoomType> roomTypes,
                                                           @RequestParam(name = "location_ids", required = false) List<String> locationIds) {
    
    return roomService.getActive(game, platform, hostQuery, roomQuery, roomTypes, locationIds);
  }
  
}
