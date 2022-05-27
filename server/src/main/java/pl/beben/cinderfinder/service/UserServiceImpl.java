package pl.beben.cinderfinder.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import pl.beben.cinderfinder.domain.Platform;
import pl.beben.cinderfinder.pojo.UserPojo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

  private final Map<String, UserPojo> sessionIdToUser = new ConcurrentHashMap<>();

  @Override
  public UserPojo getOrCreateUserBySessionId(String sessionId) {
    return sessionIdToUser.computeIfAbsent(
      sessionId,
      key -> new UserPojo("guest_" + nextId(), null, null)
    );
  }
  
  @Override
  public UserPojo getCurrentUser() {
    return getOrCreateUserBySessionId(
      ((WebAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails())
        .getSessionId()
    );
  }
  
  @Override
  public UserPojo getByUserName(String userName) {
    return sessionIdToUser.values().stream()
      .filter(user -> Objects.equals(user.getUserName(), userName))
      .findAny()
      .orElse(null);
  }

  @Override
  public void setInGameName(String inGameName) {
    getCurrentUser()
      .setInGameName(inGameName);
  }

  @Override
  public void setLastSelectedPlatform(Platform lastSelectedPlatform) {
    getCurrentUser()
      .setLastSelectedPlatform(lastSelectedPlatform);
  }

  private static long idSequence = (long) 1e4;
  private static synchronized long nextId() {
    return ++idSequence;
  }

}
