package pl.beben.furledfinger.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.beben.furledfinger.domain.Game;
import pl.beben.furledfinger.domain.Platform;
import pl.beben.furledfinger.domain.RoomType;
import pl.beben.furledfinger.pojo.*;
import pl.beben.furledfinger.pojo.event.AbstractEvent;
import pl.beben.furledfinger.pojo.event.ChatMessageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
  "matchmaker.room.ping-interval=100ms",
  "matchmaker.room.kick-after=600ms",
  "missed-host-ping-count-limit=10"
})
class ApiControllerTest {

  private static final String PLATFORM = Platform.PSX.name();
  private static final String GAME = Game.ELDEN_RING.name();

  @Autowired MockMvc mvc;

  final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Test
  public void fetchMeTest() throws Exception {
    
    // given
    final var firstAuthentication = createAuthentication();
    final var secondAuthentication = createAuthentication();
    
    // when
    final var firstUser = httpUserMe(firstAuthentication);
    final var secondUser = httpUserMe(secondAuthentication);
    
    // then
    Assert.assertNotNull(firstUser);
    Assert.assertNotNull(secondUser);
    Assert.assertNotEquals(firstUser, secondUser);
  }
  
  @Test
  public void setInGameNameTest() throws Exception {
    
    // given
    final var authentication = createAuthentication();
    final var inGameName = "Andrzej Duda";
    
    // when
    httpUserSetInGameName(authentication, inGameName);
    final var user = httpUserMe(authentication);
    
    // then
    Assert.assertNotNull(user);
    Assert.assertEquals(inGameName, user.getInGameName());
  }

  @Test
  public void chatMessagesEventsTestUsingLongPolling() throws Exception {

    // given
    final var roomId = httpCreateRoomReturningId(createAuthentication());
    final var roomEventSubscriber =
      mvc
        .perform(
          get("/api/room/subscribe_to_event?id=" + roomId)
            .with(authentication(createAuthentication()))
        );

    final var chattyAuthentication = createAuthentication();
    final var chattyUserInGameName = "kek";
    httpUserSetInGameName(chattyAuthentication, chattyUserInGameName);

    final var chatMessageContent = UUID.randomUUID() + "; " + "xxxx_ 32321321312 aąeęóćżźł";

    // when
    httpAddMessage(chattyAuthentication, roomId, chatMessageContent);

    // then
    roomEventSubscriber.andExpect(status().isOk());

    // given
    final var chatMessageEvents =
      (List<ChatMessageEvent>) roomEventSubscriber
        .andReturn()
          .getAsyncResult();

    // then
    Assert.assertNotNull(chatMessageEvents);
    Assert.assertEquals(1, chatMessageEvents.size());

    // given
    final var chatMessageEvent = chatMessageEvents.get(0);

    // then
    Assert.assertNotNull(chatMessageEvent);
    Assert.assertNotNull(chatMessageEvent.getPayload());
    Assert.assertEquals(AbstractEvent.Type.CHAT_MESSAGE, chatMessageEvent.getType());
    Assert.assertEquals(chattyUserInGameName, chatMessageEvent.getPayload().getUser().getInGameName());
    Assert.assertEquals(chatMessageContent, chatMessageEvent.getPayload().getContent());
    Assert.assertNotNull(chatMessageEvent.getPayload().getTimestamp());
  }

  @Test
  public void roomHasBeenClosedAfterHostInactivityTest() throws Exception {

    // given
    final var hostUserAuthentication = createAuthentication();
    final var roomId = httpCreateRoomReturningId(hostUserAuthentication);

    // when
    // room should be closed after 10 missed pings, let's also make sure that it won't be closed after 15 regular pings
    for (var i = 0; i < 15; i++) {
      httpRoomPing(hostUserAuthentication, roomId);
      Thread.sleep(100l); // ping interval, see class annotations
    }

    // given
    var roomDetails = httpRoomRegisterToAndGetRoomDetails(createAuthentication(), roomId);

    // then assert that room wasn't closed
    Assert.assertNotNull(roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());

    // room is expected to be closed after 10 * ping_interval because the host is not pinging it
    Thread.sleep(2000l);

    // given
    roomDetails = httpRoomRegisterToAndGetRoomDetails(createAuthentication(), roomId);

    // then assert that room wasn't closed
    Assert.assertNull(roomDetails);
  }

  @Test
  public void generalLongPollingTest() throws Exception {
    // given
    final var subscribers = new ArrayList<ResultActions>();

    // let's hope that server can handle that many subscribers
    for (var i = 0; i < 1e3; i++)
      subscribers.add(
        mvc
          .perform(
            get("/api/room/all/subscribe_to_event?game=" + GAME + "&platform=" + PLATFORM)
              .with(authentication(createAuthentication()))
          ));
    
    // this will trigger ROOM_HAS_BEEN_CREATED event
    httpCreateRoomReturningId(createAuthentication());

    for (ResultActions subscriber : subscribers) {
      final var events = (List<AbstractEvent>)
        subscriber
          .andExpect(status().isOk())
          .andReturn()
            .getAsyncResult();
      
      Assert.assertEquals(1, events.size());
      Assert.assertEquals(AbstractEvent.Type.ROOM_HAS_BEEN_CREATED, events.get(0).getType());
    }
  }

  @Test
  public void roomManagementTest() throws Exception {
    
    // given
    final var hostUserAuthentication = createAuthentication();
    final var firstGuestUserAuthentication = createAuthentication();
    final var secondGuestUserAuthentication = createAuthentication();
    httpUserSetInGameName(hostUserAuthentication, "host");
    httpUserSetInGameName(firstGuestUserAuthentication, "first");
    httpUserSetInGameName(secondGuestUserAuthentication, "second");
    
    
    final var availableRoomsBeforeCreation = httpRoomAll(hostUserAuthentication);

    // when
    final var roomId = httpCreateRoomReturningId(hostUserAuthentication);

    // then
    Assert.assertNotNull("Created room id is null", roomId);
    
    // when
    final var availableRoomsAfterCreation = httpRoomAll(hostUserAuthentication);
    
    Assert.assertEquals(availableRoomsBeforeCreation.size() + 1, availableRoomsAfterCreation.size());

    // when
    var roomDetails = httpRoomRegisterToAndGetRoomDetails(firstGuestUserAuthentication, roomId);

    // then
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(1, roomDetails.getGuests().size());

    // when
    roomDetails = httpRoomRegisterToAndGetRoomDetails(secondGuestUserAuthentication, roomId);

    // then
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(2, roomDetails.getGuests().size());
    Assert.assertEquals(2, roomDetails.getGuests().stream().filter(RoomMemberPojo::isOnline).count());

    // when
    // 2 missed pings should mark guest as offline
    for (var i = 0; i < 3; i++) {
      httpRoomPing(hostUserAuthentication, roomId);
      httpRoomPing(firstGuestUserAuthentication, roomId);

      Thread.sleep(100l); // ping interval, see class annotations
    }
    roomDetails = httpRoomRegisterToAndGetRoomDetails(hostUserAuthentication, roomId);

    // then
    // has second guest been marked as offline
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(2, roomDetails.getGuests().size());
    Assert.assertEquals(1, roomDetails.getGuests().stream().filter(RoomMemberPojo::isOnline).count());

    // when
    // another 3 missed pings should kick that guest
    for (var i = 0; i < 4; i++) {
      httpRoomPing(hostUserAuthentication, roomId);
      httpRoomPing(firstGuestUserAuthentication, roomId);

      Thread.sleep(100l); // ping interval, see class annotations
    }
    roomDetails = httpRoomRegisterToAndGetRoomDetails(hostUserAuthentication, roomId);

    // then
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(1, roomDetails.getGuests().size());
    
    // when
    httpRoomLeave(firstGuestUserAuthentication, roomId);
    roomDetails = httpRoomRegisterToAndGetRoomDetails(hostUserAuthentication, roomId);

    // then
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(0, roomDetails.getGuests().size());

    // when
    httpRoomCloseOwnedByCurrentUser(hostUserAuthentication);
    roomDetails = httpRoomRegisterToAndGetRoomDetails(hostUserAuthentication, roomId);

    // then
    Assert.assertNull("roomDetails is null", roomDetails);

    // when
    final var availableRoomsAfterRemoving = httpRoomAll(hostUserAuthentication);
    
    // then
    Assert.assertEquals(availableRoomsBeforeCreation.size(), availableRoomsAfterRemoving.size());
  }

  private Long httpCreateRoomReturningId(Authentication hostUserAuthentication) throws Exception {

    final var roomDraft = new RoomDraftPojo();
    roomDraft.setName("name1");
    roomDraft.setDescription("desc1");
    roomDraft.setLocationId("location1");
    roomDraft.setGame(Game.valueOf(GAME));
    roomDraft.setPlatform(Platform.valueOf(PLATFORM));
    roomDraft.setType(RoomType.COOP);
    roomDraft.setPassword("abcdef");

    return Long.valueOf(
      mvc
        .perform(
          post("/api/room/create_returning_id")
            .content(objectMapper.writeValueAsString(roomDraft))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .with(authentication(hostUserAuthentication))
        )
        .andExpect(
          status().isOk()
        )
        .andReturn()
        .getResponse()
        .getContentAsString()
    );
  }

  private void httpRoomCloseOwnedByCurrentUser(Authentication authentication) throws Exception {
    mvc
      .perform(
        delete("/api/room/owned_by_current_user/close")
          .with(authentication(authentication))
      )
      .andExpect(
        status().isOk()
      )
      .andReturn()
      .getResponse();
  }

  private void httpRoomLeave(Authentication authentication, Long roomId) throws Exception {
    mvc
      .perform(
        delete("/api/room/leave?id=" + roomId)
          .with(authentication(authentication))
      )
      .andExpect(
        status().isOk()
      )
      .andReturn()
      .getResponse();
  }

  private UserPojo httpUserMe(Authentication authentication) throws Exception {
    return objectMapper.readValue(
      mvc
        .perform(
          get("/api/user/me")
            .with(authentication(authentication))
        )
        .andExpect(
          status().isOk()
        )
        .andReturn()
          .getResponse()
          .getContentAsString(),
      UserPojo.class
    );
  }
  private void httpUserSetInGameName(Authentication authentication, String inGameName) throws Exception {
    mvc
      .perform(
        post("/api/user/in_game_name?value=" + inGameName)
          .with(authentication(authentication))
      )
      .andExpect(
        status().isOk()
      )
      .andReturn();
  }
  
  private RoomDetails httpRoomRegisterToAndGetRoomDetails(Authentication authentication, Long roomId) throws Exception {
    final var responseJson =
      mvc
        .perform(
          post("/api/room/register_to_and_get_details?id=" + roomId)
            .with(authentication(authentication))
        )
        .andExpect(
          status().isOk()
        )
        .andReturn()
          .getResponse()
          .getContentAsString();

    return responseJson == null || responseJson.isEmpty()
      ? null
      : objectMapper.readValue(responseJson, RoomDetails.class);
  }

  private List<IdentifiedRoomPojo> httpRoomAll(Authentication authentication) throws Exception {
    return objectMapper.readValue(
      mvc
        .perform(
          get("/api/room/all?game=" + GAME + "&platform=" + PLATFORM)
            .with(authentication(authentication))
        )
        .andExpect(
          status().isOk()
        )
        .andReturn()
        .getResponse()
        .getContentAsString(),
      new TypeReference<>() {}
    );
  }
  
  private void httpRoomPing(Authentication authentication, Long roomId) throws Exception {
    mvc
      .perform(
        post("/api/room/ping?id=" + roomId)
          .with(authentication(authentication))
      )
      .andExpect(
        status().isOk()
      )
      .andReturn();
  }

  private void httpAddMessage(Authentication authentication, Long roomId, String content) throws Exception {
    final var payload = new ChatMessageRequest();
    payload.setContent(content);
    mvc
      .perform(
        post("/api/room/message?id=" + roomId)
          .with(authentication(authentication))
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(payload))
      )
      .andExpect(
        status().isOk()
      )
      .andReturn();
  }

  private Authentication createAuthentication () {

    final var details = Mockito.mock(WebAuthenticationDetails.class);
    Mockito.when(details.getSessionId()).thenReturn(UUID.randomUUID().toString());
    Mockito.when(details.getRemoteAddress()).thenReturn("127.0.0.1");

    final var authentication = new AnonymousAuthenticationToken(UUID.randomUUID().toString(), "anonymousUser", Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    authentication.setDetails(details);
    return authentication;
  }
}
