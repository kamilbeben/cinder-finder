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
  List<ChatMessage> messages = new ArrayList<>();
  List<RoomMemberPojo> guests = new ArrayList<>();

  public RoomDetails(Long id, UserPojo host, RoomDraftPojo draft) {
    super(id, host, draft);
    refreshUpdateTimestamp();
  }
  
  public RoomDetails addGuest(UserPojo user) {
    final var member = new RoomMemberPojo(user);
    member.setOnline(true);
    guests.add(member);
    refreshUpdateTimestamp();
    return this;
  }
  
  public void updateIsOnlineFlagByUserName(String userName, boolean isOnline) {

    final var host = getHost();

    if (Objects.equals(host.getUserName(), userName)) {
      if (host.isOnline() == isOnline)
        return;
      
      host.setOnline(isOnline);
      refreshUpdateTimestamp();

    } else {
      guests.stream()
        .filter(guest ->
          Objects.equals(guest.getUserName(), userName) &&
          guest.isOnline() != isOnline
        )
        .forEach(guest -> {
          guest.setOnline(isOnline);
          refreshUpdateTimestamp();
        });
    }
  }

  public void removeGuestByUserName(String userName) {
    final var userWasInGuestsList = guests.removeIf(user -> Objects.equals(user.getUserName(), userName));

    if (userWasInGuestsList)
      refreshUpdateTimestamp();
  }
  
  public void addMessage(ChatMessage message) {
    messages.add(message);
    refreshUpdateTimestamp();
  }
  
  private void refreshUpdateTimestamp() {
    this.updateTimestamp = System.currentTimeMillis();
  }
}
