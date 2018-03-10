package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import messages.GetChat;
import messages.UnsubscribeChatManager;
import play.libs.Akka;

import java.util.HashMap;
import java.util.Map;

public class ChatManager extends AbstractActor {

    private final Map<String,ActorRef> chats;

    public ChatManager() {
        chats = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetChat.class, message -> {
                    String chatName = message.getChatname();
                    //If i don't  have this chat, I create it
                    if (!chats.containsKey(chatName))
                        chats.put(chatName, Akka.system().actorOf(Chat.props(chatName)));
                    message.setChat(chats.get(chatName));
                    getSender().tell(message, getSelf());
                })
                .match(UnsubscribeChatManager.class, message -> {
                    chats.remove(message.getChat());
                })
                .matchAny(System.err::println)
                .build();
    }

}
