package pl.beben.cinderfinder.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class RoomDetails extends IdentifiedRoomPojo {
  private static final long serialVersionUID = 5668632899628063985L;

  List<ChatMessage> messages = new ArrayList<>();
  List<RoomMemberPojo> guests = new ArrayList<>();

  public RoomDetails(Long id, UserPojo host, RoomDraftPojo draft) {
    super(id, host, draft);
  }
  
  public RoomDetails addGuest(UserPojo user) {
    final var member = new RoomMemberPojo(user);
    member.setOnline(true);
    guests.add(member);
    return this;
  }

  public boolean updateIsOnlineFlagByUserName(String userName, boolean isOnline) {

    final var host = getHost();

    if (Objects.equals(host.getUserName(), userName)) {
      if (host.isOnline() == isOnline)
        return false;
      
      host.setOnline(isOnline);
      return true;

    } else {
      final var anyUserHasBeenUpdatedReference = new AtomicBoolean(false);

      guests.stream()
        .filter(guest ->
          Objects.equals(guest.getUserName(), userName) &&
          guest.isOnline() != isOnline
        )
        .forEach(guest -> {
          guest.setOnline(isOnline);
          anyUserHasBeenUpdatedReference.set(true);
        });
      return anyUserHasBeenUpdatedReference.get();
    }
  }

  public boolean removeGuestByUserName(String userName) {
    return guests.removeIf(user -> Objects.equals(user.getUserName(), userName));
  }
  
  public RoomDetails addMessage(ChatMessage message) {
    messages.add(message);
    return this;
  }
}
