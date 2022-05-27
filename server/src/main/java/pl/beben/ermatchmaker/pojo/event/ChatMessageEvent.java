package pl.beben.ermatchmaker.pojo.event;

import pl.beben.ermatchmaker.pojo.ChatMessage;

public class ChatMessageEvent extends AbstractEvent<ChatMessage> {
  private static final long serialVersionUID = -2971539561823764181L;

  public ChatMessageEvent(ChatMessage message) {
    super(Type.CHAT_MESSAGE, message);
  }
}
