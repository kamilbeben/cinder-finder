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
import pl.beben.ermatchmaker.service.utils.QueryUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
  
  final UserPojo testUser1 = new UserPojo("Guest2132", "Arek", null);
  final UserPojo testUser2 = new UserPojo("Guest2133", "Bezik", null);
  final UserPojo testUser3 = new UserPojo("Guest2134", "Fok", null);
  final UserPojo testUser4 = new UserPojo("Guest2135", "Kupa", null);
  
  private final UserService userService;

  @Value("${matchmaker.room.ping-interval}")
  private Duration pingInterval;

  @Value("${matchmaker.room.kick-after}")
  private Duration kickAfter;
  
  private final Map<Long, Map<String, Long>> roomIdToUserNameToPingTimestamp = new ConcurrentHashMap<>();

  private final List<RoomDetails> rooms = Collections.synchronizedList(new ArrayList<>(
    Arrays.asList(
      new RoomDetails(nextId(), testUser1, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "First room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)"))
        .addGuest(testUser2)
        .addGuest(testUser3),
      new RoomDetails(nextId(), testUser2, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "Second room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)"))
        .addGuest(testUser4)
        .addGuest(testUser3)
        .addGuest(testUser1),
      new RoomDetails(nextId(), testUser3, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Third room", "Help plz", "ghzx", "Bloodhound Knight Darriwil (Limgrave)")),
      new RoomDetails(nextId(), testUser4, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Fourth room", "Help plz", "ghzx", "Deathbird (Limgrave)")),
      new RoomDetails(nextId(), testUser2, new RoomDraftPojo(Game.ELDEN_RING, Platform.XBOX, RoomType.COOP, "Fifth room", "Help plz", "ghzx", "Demi-Human Chief (Limgrave)"))
    )
  ));
  
  @Override
  public List<? extends IdentifiedRoomPojo> getActive(Game game, Platform platform, String hostQuery, String roomQuery, List<RoomType> roomTypes, List<String> locationIds) {
    return rooms.stream()
      .filter(room ->
        room.getGame() == game &&
        room.getPlatform() == platform &&
        (
          roomTypes == null ||
          roomTypes.isEmpty() ||
          roomTypes.contains(room.getType())
        ) &&
        (
          locationIds == null ||
          locationIds.isEmpty() ||
          locationIds.contains(room.getLocationId())
        ) &&
        (
          QueryUtils.matches(room.getHost().getUserName(), hostQuery) ||
          QueryUtils.matches(room.getHost().getInGameName(), hostQuery)
        ) &&
        QueryUtils.matches(room.getName(), roomQuery)
      )
      .sorted(
        ((Comparator<RoomDetails>) (left, right) -> QueryUtils.compareByQuery(left.getHost().getUserName(), right.getHost().getUserName(), hostQuery))
          .thenComparing((left, right) -> QueryUtils.compareByQuery(left.getHost().getInGameName(), right.getHost().getInGameName(), hostQuery))
          .thenComparing((left, right) -> QueryUtils.compareByQuery(left.getName(), right.getName(), roomQuery))
      )
      .collect(Collectors.toList());
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
    final var currentUser = userService.getCurrentUser();
    closeRoomOwnedByCurrentUser();
    removeCurrentUserFromOtherRooms(currentUser, null);

    final var room = new RoomDetails(nextId(), currentUser, roomDraft);
    room.setId(nextId());
    this.rooms.add(room);
    return room.getId();
  }

  @Override
  public void closeRoomOwnedByCurrentUser() {

    getRoomOwnedByCurrentUser()
      .ifPresent(room -> {
        rooms.remove(room);
      });
  }

  @Override
  public void kickGuestFromRoomOwnerByCurrentUser(String guestUserName) {
    getRoomOwnedByCurrentUser()
      .ifPresent(room -> room.removeGuestByUserName(guestUserName));
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

  private Optional<RoomDetails> getRoomOwnedByCurrentUser() {
    return rooms.stream()
      .filter(room -> Objects.equals(room.getHost().getUserName(), userService.getCurrentUser().getUserName()))
      .findAny();
  }
  
  private void updateIsOnlineFlagOnAllUsers(RoomDetails room) {
    updateIsOnlineFlagOnAllUsers(
      room,
      roomIdToUserNameToPingTimestamp.computeIfAbsent(room.getId(), key -> new ConcurrentHashMap<>())
    );
  }

  private void updateIsOnlineFlagOnAllUsers(RoomDetails room, Map<String, Long> userNameToPingTimestamp) {

    final var lowerPingTimestampLimitForUserToBeKickedOut = System.currentTimeMillis() - kickAfter.toMillis();
    final var lowerPingTimestampLimitForUserToBeConsideredOnline = System.currentTimeMillis() - (3 * pingInterval.toMillis());

    userNameToPingTimestamp
      .forEach((userName, pingTimestamp) -> {

        room.updateIsOnlineFlagByUserName(userName, lowerPingTimestampLimitForUserToBeConsideredOnline < pingTimestamp);
        
        if (!Objects.equals(userName, room.getHost().getUserName()) && lowerPingTimestampLimitForUserToBeKickedOut >= pingTimestamp)
          room.removeGuestByUserName(userName);
      });
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
      .filter(room -> currentRoom == null || !Objects.equals(room.getId(), currentRoom.getId()))
      .forEach(room -> room.removeGuestByUserName(currentUser.getUserName()));
  }

  private static long idSequence = (long) 1e4;
  private static synchronized long nextId() {
    return ++idSequence;
  }
  
}
