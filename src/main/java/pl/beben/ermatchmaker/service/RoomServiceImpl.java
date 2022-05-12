package pl.beben.ermatchmaker.service;

import org.springframework.stereotype.Service;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.RoomType;
import pl.beben.ermatchmaker.pojo.RoomPojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
  
  private final List<RoomPojo> rooms = Collections.synchronizedList(new ArrayList<>(
    Arrays.asList(
      new RoomPojo(nextId(), Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "First room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)", "noob"),
      new RoomPojo(nextId(), Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "Second room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)", "noob"),
      new RoomPojo(nextId(), Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Third room", "Help plz", "ghzx", "Bloodhound Knight Darriwil (Limgrave)", "noob"),
      new RoomPojo(nextId(), Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Fourth room", "Help plz", "ghzx", "Deathbird (Limgrave)", "arek"),
      new RoomPojo(nextId(), Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "Fifth room", "Help plz", "ghzx", "Demi-Human Chief (Limgrave)", "noob")
    )
  ));
  
  @Override
  public List<RoomPojo> getActive(Game game, Platform platform) {
    return rooms;
  }
  
  @Override
  public Long createNewRoom(RoomPojo room) {
    room.setId(nextId());
    this.rooms.add(room);
    return room.getId();
  }
  
  private static long idSequence = (long) 1e4;
  private static synchronized long nextId() {
    return ++idSequence;
  }
  
}
