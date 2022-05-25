package pl.beben.ermatchmaker.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.RoomType;
import pl.beben.ermatchmaker.pojo.*;
import pl.beben.ermatchmaker.pojo.event.AbstractEvent;
import pl.beben.ermatchmaker.pojo.event.ChatMessageEvent;
import pl.beben.ermatchmaker.pojo.event.IdentifiedRoomEvent;
import pl.beben.ermatchmaker.pojo.event.UserEvent;
import pl.beben.ermatchmaker.service.utils.QueryUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static pl.beben.ermatchmaker.pojo.event.AbstractEvent.Type;

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
  private final Map<Long, Set<DeferredResult<List<AbstractEvent>>>> roomIdToEventSubscribers = new ConcurrentHashMap<>();
  private final Map<String, Set<DeferredResult<List<AbstractEvent>>>> generalEventKeyToEventSubscribers = new ConcurrentHashMap<>();

  private final List<RoomDetails> rooms = Collections.synchronizedList(new ArrayList<>(
    Arrays.asList(
      new RoomDetails(nextId(), testUser1, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "First room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)", 23))
        .addGuest(testUser2)
        .addGuest(testUser3)
        .addMessage(new ChatMessage(testUser1, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5l), "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration"))
        .addMessage(new ChatMessage(testUser2, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2l), "All the Lorem Ipsum generators on the Internet tend to repeat predefined chunks as necessary, making this the first true generator on the Internet."))
        .addMessage(new ChatMessage(testUser4, System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1l), "no xD")),
      new RoomDetails(nextId(), testUser2, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.COOP, "Second room", "Help plz", "ghzx", "Beastman of Farum Azula (Limgrave)", null))
        .addGuest(testUser4)
        .addGuest(testUser3)
        .addGuest(testUser1),
      new RoomDetails(nextId(), testUser3, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Third room", "Help plz", "ghzx", "Bloodhound Knight Darriwil (Limgrave)", 55)),
      new RoomDetails(nextId(), testUser4, new RoomDraftPojo(Game.ELDEN_RING, Platform.PSX, RoomType.PVP, "Fourth room", "Help plz", "ghzx", "Deathbird (Limgrave)", 34)),
      new RoomDetails(nextId(), testUser2, new RoomDraftPojo(Game.ELDEN_RING, Platform.XBOX, RoomType.COOP, "Fifth room", "Help plz", "ghzx", "Demi-Human Chief (Limgrave)", 133))
    )
  ));

  // specific room

  @Override
  public Long createRoomReturningId(RoomDraftPojo roomDraft) {
    final var currentUser = userService.getCurrentUser();
    closeRoomOwnedByCurrentUser();
    removeCurrentUserFromOtherRooms(currentUser, null);

    final var room = new RoomDetails(nextId(), currentUser, roomDraft);
    room.setId(nextId());
    this.rooms.add(room);
    publishGeneralEvent(room, new IdentifiedRoomEvent(Type.ROOM_HAS_BEEN_CREATED, room));
    return room.getId();
  }

  @Override
  public void closeRoomOwnedByCurrentUser() {

    getRoomOwnedByCurrentUser()
      .ifPresent(room -> {
        rooms.remove(room);
        publishGeneralEvent(room, new IdentifiedRoomEvent(Type.ROOM_HAS_BEEN_REMOVED, room));
      });
  }

  @Override
  public RoomDetails registerToAndGetDetails(Long id) {

    final var room = getById(id);
    final var currentUser = userService.getCurrentUser();

    if (room != null) {
      removeCurrentUserFromOtherRooms(currentUser, room);

      if (!isCurrentUserIsAMemberOfThisRoom(room, currentUser)) {
        publishRoomEvent(id, new UserEvent(Type.USER_HAS_JOINED, currentUser));
        room.addGuest(currentUser);
      }

      ping(room.getId());
      updateIsOnlineFlagOnAllUsers(room);
    }

    return room;
  }

  @Override
  public void subscribeToRoomEvent(Long roomId, DeferredResult<List<AbstractEvent>> deferredResult) {
    roomIdToEventSubscribers
      .computeIfAbsent(roomId, key -> new HashSet<>())
      .add(deferredResult);
  }

  @Override
  public void kickGuestFromRoomOwnerByCurrentUser(String guestUserName) {
    getRoomOwnedByCurrentUser()
      .ifPresent(room -> {
        if (room.removeGuestByUserName(guestUserName))
          publishRoomEvent(room.getId(), new UserEvent(Type.USER_HAS_LEFT, userService.getByUserName(guestUserName)));
      });
  }

  @Override
  public void ping(Long id) {
    final var currentUser = userService.getCurrentUser();
    final var room = getById(id);

    final var userNameToPingTimestamp = roomIdToUserNameToPingTimestamp.computeIfAbsent(id, key -> new ConcurrentHashMap<>());
    userNameToPingTimestamp.put(currentUser.getUserName(), System.currentTimeMillis());

    if (!isCurrentUserIsAMemberOfThisRoom(room, currentUser)) {
      publishRoomEvent(id, new UserEvent(Type.USER_HAS_JOINED, currentUser));
      room.addGuest(currentUser);
    }

    updateIsOnlineFlagOnAllUsers(room, userNameToPingTimestamp);
  }

  @Override
  public void leave(Long id) {
    Optional
      .ofNullable(getById(id))
      .ifPresent(room -> {
        final var currentUser = userService.getCurrentUser();
        if (room.removeGuestByUserName(currentUser.getUserName()))
          publishRoomEvent(id, new UserEvent(Type.USER_HAS_LEFT, currentUser));
      });
  }

  @Override
  public void addMessage(Long roomId, String content) {
    final var chatMessage =
      new ChatMessage(
        userService.getCurrentUser(),
        System.currentTimeMillis(),
        content
      );

    getById(roomId).addMessage(chatMessage);

    publishRoomEvent(roomId, new ChatMessageEvent(chatMessage));
  }

  // all rooms

  @Override
  public List<IdentifiedRoomPojo> searchRooms(@NonNull Game game,
                                              @NonNull Platform platform,
                                              String hostQuery,
                                              String roomQuery,
                                              List<RoomType> roomTypes,
                                              List<String> locationIds,
                                              Integer minHostLevel,
                                              Integer maxHostLevel) {
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
        QueryUtils.matches(room.getName(), roomQuery) &&
        (
          minHostLevel == null ||
          (
            room.getHostLevel() != null &&
            room.getHostLevel() >= minHostLevel
          )
        ) &&
        (
          maxHostLevel == null ||
          (
            room.getHostLevel() != null &&
            room.getHostLevel() <= maxHostLevel
          )
        )
      )
      .sorted(
        ((Comparator<RoomDetails>) (left, right) -> QueryUtils.compareByQuery(left.getHost().getUserName(), right.getHost().getUserName(), hostQuery))
          .thenComparing((left, right) -> QueryUtils.compareByQuery(left.getHost().getInGameName(), right.getHost().getInGameName(), hostQuery))
          .thenComparing((left, right) -> QueryUtils.compareByQuery(left.getName(), right.getName(), roomQuery))
      )
      .collect(Collectors.toList());
  }

  @Override
  public void subscribeToGeneralEvent(Game game, Platform platform, DeferredResult<List<AbstractEvent>> deferredResult) {
    generalEventKeyToEventSubscribers
      .computeIfAbsent(createGeneralEventKey(game, platform), key -> new HashSet<>())
      .add(deferredResult);;
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
    final var lowerPingTimestampLimitForUserToBeConsideredOnline = System.currentTimeMillis() - (2 * pingInterval.toMillis());
    
    final var events = new ArrayList<AbstractEvent>();

    userNameToPingTimestamp
      .forEach((userName, pingTimestamp) -> {
        final var user = userService.getByUserName(userName);
        final var userIsOnline = lowerPingTimestampLimitForUserToBeConsideredOnline < pingTimestamp;
        final var userShouldBeKickedOut = !Objects.equals(userName, room.getHost().getUserName()) && lowerPingTimestampLimitForUserToBeKickedOut >= pingTimestamp;

        if (room.updateIsOnlineFlagByUserName(userName, userIsOnline))
          events.add(
            new UserEvent(
              userIsOnline
                ? Type.USER_IS_ONLINE
                : Type.USER_IS_OFFLINE,
              user
            ));
        
        if (userShouldBeKickedOut) {
          if (room.removeGuestByUserName(user.getUserName()))
            events.add(new UserEvent(Type.USER_HAS_LEFT, user));
        }
      });
    
    publishRoomEvent(room.getId(), events);
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
      .forEach(room -> {
        if (room.removeGuestByUserName(currentUser.getUserName()))
          publishRoomEvent(room.getId(), new UserEvent(Type.USER_HAS_LEFT, currentUser));
      });
  }
  
  private void publishGeneralEvent(RoomDraftPojo room, AbstractEvent event) {
    publishGeneralEvent(room.getGame(), room.getPlatform(), Arrays.asList(event));
  }

  private void publishGeneralEvent(Game game, Platform platform, List<AbstractEvent> events) {
    if (events == null || events.isEmpty())
      return;

    final var originalEventSubscribers = generalEventKeyToEventSubscribers.computeIfAbsent(createGeneralEventKey(game, platform), key -> new HashSet<>());
    final var eventSubscribers = new HashSet<>(originalEventSubscribers);

    originalEventSubscribers.clear();
    eventSubscribers.forEach(deferredResult -> deferredResult.setResult(events));
  }
  
  private void publishRoomEvent(Long roomId, AbstractEvent event) {
    publishRoomEvent(roomId, Arrays.asList(event));
  }

  private void publishRoomEvent(Long roomId, List<AbstractEvent> events) {
    if (events == null || events.isEmpty())
      return;

    final var originalEventSubscribers = roomIdToEventSubscribers.computeIfAbsent(roomId, key -> new HashSet<>());
    final var eventSubscribers = new HashSet<>(originalEventSubscribers);

    originalEventSubscribers.clear();
    eventSubscribers.forEach(deferredResult -> deferredResult.setResult(events));
  }

  private String createGeneralEventKey(Game game, Platform platform) {
    return game + ";" + platform;
  }

  private static long idSequence = (long) 1e4;
  private static synchronized long nextId() {
    return ++idSequence;
  }
  
}
