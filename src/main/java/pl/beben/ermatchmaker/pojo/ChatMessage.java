package pl.beben.ermatchmaker.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class ChatMessage implements Serializable {
  private static final long serialVersionUID = 9031001801992200143L;

  UserPojo userPojo;
  Long timestamp;
  String content;

}
