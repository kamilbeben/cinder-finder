package pl.beben.ermatchmaker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.TestEntity;
import pl.beben.ermatchmaker.domain.repository.TestEntityRepository;
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
  
  private final TestEntityRepository testEntityRepository;
  private final RoomService roomService;
  private final UserService userService;

  @PostMapping("/in_game_name")
  public void setInGameName(@RequestBody String inGameName) {
    userService.setInGameName(inGameName);
  }

  @PostMapping("/register_to_and_get_details")
  public RoomDetails registerToAndGetDetails(@RequestParam("id") Long id) {
    return roomService.registerToAndGetDetails(id);
  }

  @PostMapping("/ping_returning_update_timestamp")
  public Long pingReturningUpdateTimestamp(@RequestParam("id") Long id) {
    return roomService.pingReturningUpdateTimestamp(id);
  }

  @PostMapping("/create_room")
  public Long createRoom(@RequestBody RoomDraftPojo room) {
    return roomService.createNewRoom(room);
  }

  @GetMapping("/rooms")
  public List<? extends IdentifiedRoomPojo> getActiveRooms(@RequestParam("game") Game game,
                                                           @RequestParam("platform") Platform platform) {
    
    return roomService.getActive(game, platform);
  }
  
  @GetMapping("/me")
  public UserPojo getLoggedUser() {
    return userService.getCurrentUser();
  }
  
  @GetMapping("/test")
  public Iterable<TestEntity> getTest() {
    
    if (false) {
      final var entity = new TestEntity();
      entity.setName("kek");
      testEntityRepository.save(entity);
    }
    
    return testEntityRepository.findAll();
  }
  
}
