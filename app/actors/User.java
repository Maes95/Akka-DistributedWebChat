package actors;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import com.fasterxml.jackson.databind.JsonNode;
import messages.*;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import play.libs.Akka;


public class User extends AbstractActor {
    
    private final static String DUPLICATE_MSG = "{\"type\":\"system\",\"message\":\"This user already exists\"}";
    private final static Set<String> users = new ConcurrentSkipListSet<>();

    private final ActorRef out;
    private String userName;
    private String chatName;
    private ActorRef mediator;
    

    public static Props props(ActorRef out) {
        return Props.create(User.class, out);
    }

    public User(ActorRef out) {
        this.out = out;
        this.mediator = DistributedPubSub.get(getContext().system()).mediator();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()

                // WEBSOCKET(CLIENT) TO USER
                .match(String.class, message -> {
                    JsonNode json= utils.Utils.getJson(message);
                    //Initial message
                    if (!json.has("message")){
                        this.userName = json.get("name").asText();
                        if(users.contains(this.userName)){
                            // USERNAME LOCKED
                            out.tell(DUPLICATE_MSG,getSelf());
                            self().tell(PoisonPill.getInstance(), self());
                        }else{
                            // USERNAME AVAILIBLE
                            this.chatName = json.get("chat").asText();
                            users.add(this.userName);

                            // subscribe to the topic named "content"
                            this.mediator.tell(new DistributedPubSubMediator.Subscribe(this.chatName, getSelf()),getSelf());
                            //EventBus.eventBus().subscribe(getSelf(), chatName);
                        }
                    }
                    // Normal message
                    else{
                        Message msg = new Message(json.get("name").asText(),json.get("message").asText());
                        //EventBus.eventBus().publish(new MsgEnvelope(this.chatName, msg));
                        mediator.tell(new DistributedPubSubMediator.Publish(this.chatName, msg),getSelf());
                    }
                })

                // CHAT TO CLIENT
                .match(Message.class, message -> { out.tell(message.getJson().toString(), self()); })
                .matchAny(System.err::println)
                .build();
    }


    // When the websocket are closed
    @Override
    public void postStop(){
        if (this.chatName!=null){
            mediator.tell(new DistributedPubSubMediator.Unsubscribe(this.chatName, getSelf()),getSelf());
        }
    }
    
}
