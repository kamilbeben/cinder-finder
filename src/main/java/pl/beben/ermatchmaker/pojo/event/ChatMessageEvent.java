package pl.beben.ermatchmaker.pojo.event;

import pl.beben.ermatchmaker.pojo.ChatMessage;

public class ChatMessageEvent extends AbstractEvent<ChatMessage> {
  private static final long serialVersionUID = -2971539561823764181L;

  public ChatMessageEvent(Type type, ChatMessage message) {
    super(type, message);
  }
}
