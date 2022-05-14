package pl.beben.ermatchmaker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
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
  public void setInGameName(@RequestBody String inGameName) {
    userService.setInGameName(inGameName);
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

  @DeleteMapping("/room/close_owned_by_current_user")
  public void removeRoom() {
    roomService.closeRoomOwnedByCurrentUser();
  }

  @DeleteMapping("/room/leave")
  public void leaveRoom(@RequestParam("id") Long id) {
    roomService.leaveRoom(id);
  }

  @GetMapping("/room/all")
  public List<? extends IdentifiedRoomPojo> getActiveRooms(@RequestParam("game") Game game,
                                                           @RequestParam("platform") Platform platform) {
    
    return roomService.getActive(game, platform);
  }
  
}
