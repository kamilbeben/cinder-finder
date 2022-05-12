package pl.beben.ermatchmaker.service;

import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.pojo.RoomPojo;

import java.util.List;

public interface RoomService {
  List<RoomPojo> getActive(Game game, Platform platform);

  Long createNewRoom(RoomPojo room);
}
