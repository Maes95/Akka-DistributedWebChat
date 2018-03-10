package utils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.japi.LookupEventBus;
import messages.Message;
import messages.MsgEnvelope;

public class EventBus extends LookupEventBus<MsgEnvelope, ActorRef, String> {

    private static EventBus _eventBus = new EventBus();

    public static EventBus eventBus(){
        return _eventBus;
    }

    final ActorSystem system;

    public EventBus(){
         this.system = ActorSystem.create("Messages");
    }

    // is used for extracting the classifier from the incoming events
    @Override public String classify(MsgEnvelope event) {
        return event.topic();
    }

    // will be invoked for each event for all subscribers which registered themselves
    // for the event’s classifier
    @Override public void publish(MsgEnvelope event, ActorRef subscriber) {
        subscriber.tell(event.body(), ActorRef.noSender());
    }

    // must define a full order over the subscribers, expressed as expected from
    // `java.lang.Comparable.compare`
    @Override public int compareSubscribers(ActorRef a, ActorRef b) {
        return a.compareTo(b);
    }

    // determines the initial size of the index data structure
    // used internally (i.e. the expected number of different classifiers)
    @Override public int mapSize() {
        return 128;
    }

    @Override
    public boolean subscribe(ActorRef subscriber, String to) {
        this.system.eventStream().subscribe(subscriber, Message.class);
        return super.subscribe(subscriber, to);
    }
}
