package pl.beben.ermatchmaker.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class RoomDetails extends IdentifiedRoomPojo {
  private static final long serialVersionUID = 5668632899628063985L;

  Long updateTimestamp;
  List<UserPojo> guests = new ArrayList<>();

  public RoomDetails(Long id, UserPojo host, RoomDraftPojo draft) {
    super(id, host, draft);
    refreshUpdateTimestamp();
  }
  
  public RoomDetails addGuest(UserPojo user) {
    guests.add(user);
    refreshUpdateTimestamp();
    return this;
  }

  public void removeGuestByUserName(String userName) {
    final var userWasInGuestsList = guests.removeIf(user -> Objects.equals(user.getUserName(), userName));

    if (userWasInGuestsList)
      refreshUpdateTimestamp();
  }
  
  private void refreshUpdateTimestamp() {
    this.updateTimestamp = System.currentTimeMillis();
  }
}
