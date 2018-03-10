package actors;

import akka.actor.*;
import messages.*;
import play.libs.Akka;

import java.util.HashMap;
import java.util.Map;

public class Chat extends AbstractActor {

    private final Map<String, ActorRef> users;
    private final ActorRef chatManager;
    private final String chatName;

    public static Props props(String chatName) {
        return Props.create(Chat.class, chatName);
    }

    public Chat(String chatName) {
        this.chatName = chatName;
        this.chatManager = Akka.system().actorFor("akka://application/user/ChatManager");
        this.users = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, message -> {
                    users.entrySet().forEach((entry) -> {
                        entry.getValue().tell(message, getSelf());
                    });
                })
                .match(SubscribeChat.class, message -> {
                    if (users.containsKey(message.getUser())){
                        //If I already have this user
                        getSender().tell(new DuplicatedUser(message.getUser()), getSelf());
                    }else{
                        //If is a new user, I subscribe it
                        users.put(message.getUser(), getSender());
                    }
                })
                .match(UnsubscribeChat.class, message -> {
                    users.remove(message.getUser());
                    if (users.isEmpty()) {
                        //If there aren't clients in this chat, I remove this chat
                        chatManager.tell(new UnsubscribeChatManager(chatName), getSelf());
                        self().tell(PoisonPill.getInstance(), self());
                    }
                })
                .matchAny(System.err::println)
                .build();
    }

}