package pl.beben.ermatchmaker.service;

import pl.beben.ermatchmaker.domain.Platform;
import pl.beben.ermatchmaker.pojo.UserPojo;

public interface UserService {
  UserPojo getOrCreateUserBySessionId(String sessionId);

  UserPojo getCurrentUser();

  UserPojo getByUserName(String userName);

  void setInGameName(String inGameName);

  void setLastSelectedPlatform(Platform platform);
}
