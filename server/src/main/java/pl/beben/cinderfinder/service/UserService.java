package pl.beben.cinderfinder.service;

import pl.beben.cinderfinder.domain.Platform;
import pl.beben.cinderfinder.pojo.UserPojo;

public interface UserService {
  UserPojo getOrCreateUserBySessionId(String sessionId);

  UserPojo getCurrentUser();

  UserPojo getByUserName(String userName);

  void setInGameName(String inGameName);

  void setLastSelectedPlatform(Platform platform);
}
