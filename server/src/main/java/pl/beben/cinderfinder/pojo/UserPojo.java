package pl.beben.cinderfinder.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import pl.beben.cinderfinder.domain.Platform;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class UserPojo {

  String userName; // unique
  String inGameName;
  Platform lastSelectedPlatform;

}
