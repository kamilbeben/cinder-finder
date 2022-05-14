package pl.beben.ermatchmaker.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class IdentifiedRoomPojo extends RoomDraftPojo {
  private static final long serialVersionUID = -6252303914164361059L;

  Long id;
  RoomMemberPojo host;
  
  public IdentifiedRoomPojo(Long id, UserPojo host, RoomDraftPojo draft) {
    super(draft.getGame(), draft.getPlatform(), draft.getType(), draft.getName(), draft.getDescription(), draft.getPassword(), draft.getLocationId());
    this.id = id;
    this.host = new RoomMemberPojo(host);
    this.host.setOnline(true);
  }

}
