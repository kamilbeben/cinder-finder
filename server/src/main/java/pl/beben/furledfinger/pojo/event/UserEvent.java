package pl.beben.furledfinger.pojo.event;

import pl.beben.furledfinger.pojo.UserPojo;

public class UserEvent extends AbstractEvent<UserPojo> {
  private static final long serialVersionUID = -3729954182175247260L;

  public UserEvent(Type type, UserPojo userPojo) {
    super(type, userPojo);
  }
}
