package pl.beben.ermatchmaker.service;

import pl.beben.ermatchmaker.pojo.UserPojo;

public interface UserService {
  UserPojo getOrCreateUserBySessionId(String sessionId);

  UserPojo getCurrentUser();

  void setInGameName(String inGameName);
}
