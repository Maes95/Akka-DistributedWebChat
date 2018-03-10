package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

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
                .match(String.class, message -> {

                })
                .matchAny(System.err::println)
                .build();
    }

}
