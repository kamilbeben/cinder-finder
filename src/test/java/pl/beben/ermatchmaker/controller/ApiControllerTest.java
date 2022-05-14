package pl.beben.ermatchmaker.controller;

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
import pl.beben.ermatchmaker.domain.Game;
import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.domain.RoomType;
import pl.beben.ermatchmaker.pojo.IdentifiedRoomPojo;
import pl.beben.ermatchmaker.pojo.RoomDetails;
import pl.beben.ermatchmaker.pojo.RoomDraftPojo;
import pl.beben.ermatchmaker.pojo.UserPojo;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = "matchmaker.room-ping-interval=100ms")
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
    final var firstUser = httpMe(firstAuthentication);
    final var secondUser = httpMe(secondAuthentication);
    
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
    httpSetInGameName(authentication, inGameName);
    final var user = httpMe(authentication);
    
    // then
    Assert.assertNotNull(user);
    Assert.assertEquals(inGameName, user.getInGameName());
  }

  @Test
  public void roomManagementTest() throws Exception {
    
    // given
    final var hostUserAuthentication = createAuthentication();
    final var firstGuestUserAuthentication = createAuthentication();
    final var secondGuestUserAuthentication = createAuthentication();
    
    final var availableRoomsBeforeCreation = httpFetchRooms(hostUserAuthentication);

    // when
    final var roomId = httpCreateRoomReturningId(hostUserAuthentication);

    // then
    Assert.assertNotNull("Created room id is null", roomId);
    
    // when
    final var availableRoomsAfterCreation = httpFetchRooms(hostUserAuthentication);
    
    Assert.assertEquals(availableRoomsBeforeCreation.size() + 1, availableRoomsAfterCreation.size());

    // when
    var roomDetails = httpRegisterToAndGetRoomDetails(firstGuestUserAuthentication, roomId);

    // then
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(1, roomDetails.getGuests().size());

    // given
    final var roomUpdateTimestampBeforeSecondUserHasBeenRegistered = roomDetails.getUpdateTimestamp();
    
    // when
    roomDetails = httpRegisterToAndGetRoomDetails(secondGuestUserAuthentication, roomId);

    // then
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(2, roomDetails.getGuests().size());

    // given
    final var roomUpdateTimestampAfterSecondUserHasBeenRegistered = roomDetails.getUpdateTimestamp();
    
    // then
    Assert.assertTrue(roomUpdateTimestampAfterSecondUserHasBeenRegistered > roomUpdateTimestampBeforeSecondUserHasBeenRegistered);

    // when
    // 3 missed pings should kick a user
    Long lastPingedRoomUpdateTimestamp = null;
    for (var i = 0; i < 4; i++) {
      httpPingReturningUpdateTimestamp(hostUserAuthentication, roomId);
      lastPingedRoomUpdateTimestamp = httpPingReturningUpdateTimestamp(firstGuestUserAuthentication, roomId);

      Thread.sleep(100l); // ping interval, see class annotations
    }
    
    roomDetails = httpRegisterToAndGetRoomDetails(hostUserAuthentication, roomId);

    // then
    // has second guest been kicked?
    Assert.assertNotNull("roomDetails is null", roomDetails);
    Assert.assertEquals(roomId, roomDetails.getId());
    Assert.assertEquals(1, roomDetails.getGuests().size());
    Assert.assertTrue(roomDetails.getUpdateTimestamp() > roomUpdateTimestampAfterSecondUserHasBeenRegistered);
    Assert.assertEquals(lastPingedRoomUpdateTimestamp, roomDetails.getUpdateTimestamp());
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
          post("/api/create_room")
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

  private UserPojo httpMe(Authentication authentication) throws Exception {
    return objectMapper.readValue(
      mvc
        .perform(
          get("/api/me")
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
  private void httpSetInGameName(Authentication authentication, String inGameName) throws Exception {
    mvc
      .perform(
        post("/api/in_game_name")
          .content(inGameName)
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .with(authentication(authentication))
      )
      .andExpect(
        status().isOk()
      )
      .andReturn();
  }
  
  private RoomDetails httpRegisterToAndGetRoomDetails(Authentication authentication, Long roomId) throws Exception {
    return objectMapper.readValue(
      mvc
        .perform(
          post("/api/register_to_and_get_details?id=" + roomId)
            .with(authentication(authentication))
        )
        .andExpect(
          status().isOk()
        )
        .andReturn()
          .getResponse()
          .getContentAsString(),
      RoomDetails.class
    );
  }

  private List<IdentifiedRoomPojo> httpFetchRooms(Authentication authentication) throws Exception {
    return objectMapper.readValue(
      mvc
        .perform(
          get("/api/rooms?game=" + GAME + "&platform=" + PLATFORM)
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
  
  private Long httpPingReturningUpdateTimestamp(Authentication authentication, Long roomId) throws Exception {
    return Long.valueOf(
      mvc
        .perform(
          post("/api/ping_returning_update_timestamp?id=" + roomId)
            .with(authentication(authentication))
        )
        .andExpect(
          status().isOk()
        )
        .andReturn()
        .getResponse()
        .getContentAsString()
    );
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