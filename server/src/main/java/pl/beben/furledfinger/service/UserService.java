package pl.beben.furledfinger.service;

import pl.beben.furledfinger.domain.Platform;
import pl.beben.furledfinger.pojo.UserPojo;

public interface UserService {
  UserPojo getOrCreateUserBySessionId(String sessionId);

  UserPojo getCurrentUser();

  UserPojo getByUserName(String userName);

  void setInGameName(String inGameName);

  void setLastSelectedPlatform(Platform platform);
}
