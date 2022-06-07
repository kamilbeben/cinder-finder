package pl.beben.furledfinger.pojo;

import lombok.*;
import lombok.experimental.FieldDefaults;
import pl.beben.furledfinger.domain.Game;
import pl.beben.furledfinger.domain.Platform;
import pl.beben.furledfinger.domain.RoomType;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class RoomDraftPojo implements Serializable {
  private static final long serialVersionUID = 8308428558849036961L;

  Game game;
  Platform platform;
  RoomType type;

  String name;
  String description;
  String password;

  String locationId;
  Integer hostLevel;

}
