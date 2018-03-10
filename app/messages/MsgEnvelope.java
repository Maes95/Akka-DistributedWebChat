package messages;

public class MsgEnvelope {
    private final String topic;
    private final Object body;

    public MsgEnvelope(String topic, Object body) {
        this.topic = topic;
        this.body = body;
    }

    public String topic() {
        return topic;
    }

    public Object body() {
        return body;
    }
}