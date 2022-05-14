package pl.beben.ermatchmaker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.RoomType;
import pl.beben.ermatchmaker.pojo.IdentifiedRoomPojo;
import pl.beben.ermatchmaker.pojo.RoomDetails;
import pl.beben.ermatchmaker.pojo.RoomDraftPojo;
import pl.beben.ermatchmaker.pojo.UserPojo;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
  
  final UserPojo testUser1 = new UserPojo("Guest2132", "Arek");
  final UserPojo testUser2 = new UserPojo("Guest2133", "Bezik");
  final UserPojo testUser3 = new UserPojo("Guest2134", "Fok");
  final UserPojo testUser4 = new UserPojo("Guest2135", "Kupa");
  
  private final UserService userService;

  @Value("${matchmaker.room-ping-interval}")
  private Duration pingInterval;
  
  private final Map<Long, Map<String, Long>> roomIdToUserNameToPingTimestamp = new ConcurrentHashMap<>();

  private final List<RoomDetails> rooms = Collections.synchronizedList(new ArrayList<>(
    Arrays.asList(
      new RoomDetails(nextId(), testUser1, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "First room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)"))
        .addGuest(testUser2)
        .addGuest(testUser3),
      new RoomDetails(nextId(), testUser1, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "Second room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)"))
        .addGuest(testUser4)
        .addGuest(testUser3)
        .addGuest(testUser2),
      new RoomDetails(nextId(), testUser1, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Third room", "Help plz", "ghzx", "Bloodhound Knight Darriwil (Limgrave)")),
      new RoomDetails(nextId(), testUser1, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Fourth room", "Help plz", "ghzx", "Deathbird (Limgrave)")),
      new RoomDetails(nextId(), testUser1, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "Fifth room", "Help plz", "ghzx", "Demi-Human Chief (Limgrave)"))
    )
  ));
  
  @Override
  public List<? extends IdentifiedRoomPojo> getActive(Game game, Platform platform) {
    return rooms;
  }

  @Override
  public RoomDetails registerToAndGetDetails(Long id) {

    final var room = getById(id);
    final var currentUser = userService.getCurrentUser();

    if (room != null) {
      removeCurrentUserFromOtherRooms(currentUser, room);

      if (!isCurrentUserIsAMemberOfThisRoom(room, currentUser))
        room.addGuest(currentUser);

      pingReturningUpdateTimestamp(room.getId());
      updateIsOnlineFlagOnAllUsers(room);
    }

    return room;
  }

  @Override
  public Long createRoomReturningId(RoomDraftPojo roomDraft) {
    closeRoomOwnedByCurrentUser();
    final var room = new RoomDetails(nextId(), userService.getCurrentUser(), roomDraft);
    room.setId(nextId());
    this.rooms.add(room);
    return room.getId();
  }

  @Override
  public void closeRoomOwnedByCurrentUser() {

    rooms.stream()
      .filter(room -> Objects.equals(room.getHost().getUserName(), userService.getCurrentUser().getUserName()))
      .findAny()
      .ifPresent(room -> {
        rooms.remove(room);
      });
  }

  @Override
  public Long pingReturningUpdateTimestamp(Long id) {
    final var currentUser = userService.getCurrentUser();
    final var room = getById(id);
    
    final var userNameToPingTimestamp = roomIdToUserNameToPingTimestamp.computeIfAbsent(id, key -> new ConcurrentHashMap<>());
    userNameToPingTimestamp.put(currentUser.getUserName(), System.currentTimeMillis());

    if (!isCurrentUserIsAMemberOfThisRoom(room, currentUser))
      room.addGuest(currentUser);

    updateIsOnlineFlagOnAllUsers(room, userNameToPingTimestamp);

    return room.getUpdateTimestamp();
  }

  @Override
  public void leaveRoom(Long roomId) {
    Optional
      .ofNullable(getById(roomId))
      .ifPresent(room -> room.removeGuestByUserName(userService.getCurrentUser().getUserName()));
  }
  
  private void updateIsOnlineFlagOnAllUsers(RoomDetails room) {
    updateIsOnlineFlagOnAllUsers(
      room,
      roomIdToUserNameToPingTimestamp.computeIfAbsent(room.getId(), key -> new ConcurrentHashMap<>())
    );
  }

  private void updateIsOnlineFlagOnAllUsers(RoomDetails room, Map<String, Long> userNameToPingTimestamp) {

    final var minPingTimestampForUserToBeConsideredOnline = System.currentTimeMillis() - (3 * pingInterval.toMillis());

    userNameToPingTimestamp
      .forEach((userName, pingTimestamp) ->
        room.updateIsOnlineFlagByUserName(userName, minPingTimestampForUserToBeConsideredOnline < pingTimestamp)
      );
  }

  private RoomDetails getById(Long id) {
    return rooms.stream()
      .filter(it -> Objects.equals(it.getId(), id))
      .findAny()
      .orElse(null);
  }

  private boolean isCurrentUserIsAMemberOfThisRoom(RoomDetails room, UserPojo currentUser) {
    return
      Objects.equals(room.getHost().getUserName(), currentUser.getUserName()) ||
      room.getGuests().stream()
        .anyMatch(guest -> Objects.equals(guest.getUserName(), currentUser.getUserName()));
  }
  
  private void removeCurrentUserFromOtherRooms(UserPojo currentUser, RoomDetails currentRoom) {
    rooms.stream()
      .filter(room -> !Objects.equals(room.getId(), currentRoom.getId()))
      .forEach(room -> room.removeGuestByUserName(currentUser.getUserName()));
  }

  private static long idSequence = (long) 1e4;
  private static synchronized long nextId() {
    return ++idSequence;
  }
  
}
