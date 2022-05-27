package pl.beben.ermatchmaker.pojo.event;

import pl.beben.ermatchmaker.pojo.UserPojo;

public class UserEvent extends AbstractEvent<UserPojo> {
  private static final long serialVersionUID = -3729954182175247260L;

  public UserEvent(Type type, UserPojo userPojo) {
    super(type, userPojo);
  }
}
