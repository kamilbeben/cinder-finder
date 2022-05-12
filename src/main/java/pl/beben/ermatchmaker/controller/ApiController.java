package pl.beben.ermatchmaker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.TestEntity;
import pl.beben.ermatchmaker.domain.repository.TestEntityRepository;
import pl.beben.ermatchmaker.pojo.RoomPojo;
import pl.beben.ermatchmaker.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
  
  private final TestEntityRepository testEntityRepository;
  private final RoomService roomService;
  
  @GetMapping("/rooms")
  public List<RoomPojo> getActiveRooms(@RequestParam("game") Game game,
                                       @RequestParam("platform") Platform platform) {
    
    return roomService.getActive(game, platform);
  }
  
  @PostMapping("/create_room")
  public Long createRoom(@RequestBody RoomPojo room) {
    return roomService.createNewRoom(room);
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
