package actors;

import akka.actor.*;
import com.fasterxml.jackson.databind.JsonNode;
import messages.*;

import java.util.ArrayList;
import java.util.List;
import play.libs.Akka;


public class User extends AbstractActor {
    
    private final static String DUPLICATE_MSG = "{\"type\":\"system\",\"message\":\"This user already exists\"}";

    private final ActorRef out;
    private final ActorRef chatManager;
    private final List<Message> unsendMessages;
    private ActorRef chat;
    private String username;
    

    public static Props props(ActorRef out) {
        return Props.create(User.class, out, Akka.system().actorFor("akka://application/user/ChatManager"));
    }

    public User(ActorRef out, ActorRef chatManager) {
        this.out = out;
        this.chatManager = chatManager;
        this.unsendMessages = new ArrayList<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()

                // WEBSOCKET(CLIENT) TO USER
                .match(String.class, message -> {
                    JsonNode json= utils.Utils.getJson(message);
                    //Initial message. Send the chat name to ChatManager
                    if (!json.has("message")){
                        username = json.get("name").asText();
                        GetChat getChat = new GetChat(json.get("chat").asText());
                        chatManager.tell(getChat, getSelf());
                    }
                    // Normal message, send to the chat
                    else{
                        Message msg = new Message(json.get("name").asText(),json.get("message").asText());
                        // To avoid the first messages to be sent before the chat actorRef is received
                        if (chat!= null){
                            chat.tell(msg, getSelf());
                        }else{
                            unsendMessages.add(msg);
                        }
                    }
                })

                // CHAT TO CLIENT
                .match(Message.class, message -> { out.tell(message.getJson().toString(), self()); })

                // CLIENT TO CHATMANAGER
                .match(GetChat.class, message -> {
                    SubscribeChat subscribeChat = new SubscribeChat(username);
                    chat = message.getChat();
                    chat.tell(subscribeChat,getSelf());

                    unsendMessages.forEach((msg) -> {
                        chat.tell(msg, getSelf());
                    });
                    unsendMessages.clear();
                })

                // CHAT TO CLIENT (DUPLICATED USER)
                .match(DuplicatedUser.class, message -> {
                    chat = null;
                    out.tell(DUPLICATE_MSG,getSelf());
                    self().tell(PoisonPill.getInstance(), self());
                })
                .matchAny(System.err::println)
                .build();
    }


    // When the websocket are closed
    @Override
    public void postStop(){
        if (chat!=null){
            //If I was connected to any chat, I unsubscribe
            chat.tell(new UnsubscribeChat(username),getSelf());
        }
    }
    
}
