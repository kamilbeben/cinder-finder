package pl.beben.ermatchmaker.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import pl.beben.ermatchmaker.pojo.UserPojo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

  private final Map<String, UserPojo> sessionIdToUser = new ConcurrentHashMap<>();

  @Override
  public UserPojo getOrCreateUserBySessionId(String sessionId) {
    return sessionIdToUser.computeIfAbsent(
      sessionId,
      key -> new UserPojo("guest_" + nextId(), null)
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
  public void setInGameName(String inGameName) {
    getCurrentUser()
      .setInGameName(inGameName);
  }

  private static long idSequence = (long) 1e4;
  private static synchronized long nextId() {
    return ++idSequence;
  }

}
