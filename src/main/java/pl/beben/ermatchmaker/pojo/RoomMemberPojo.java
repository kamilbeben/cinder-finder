package pl.beben.ermatchmaker.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class RoomMemberPojo extends UserPojo {
  
  @JsonProperty("isOnline")
  boolean isOnline;
  
  public RoomMemberPojo(UserPojo user) {
    super(user.getUserName(), user.getInGameName());
  }
  
}
