package pl.beben.cinderfinder.pojo.event;

import pl.beben.cinderfinder.pojo.ChatMessage;

public class ChatMessageEvent extends AbstractEvent<ChatMessage> {
  private static final long serialVersionUID = -2971539561823764181L;

  public ChatMessageEvent(ChatMessage message) {
    super(Type.CHAT_MESSAGE, message);
  }
}
