package messages;

import java.io.Serializable;

public class UnsubscribeChatManager implements Serializable{

    private final String chat;

    public UnsubscribeChatManager (String chat){
        this.chat = chat;
    }

    public String getChat() {
        return chat;
    }
}
