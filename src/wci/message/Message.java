package wci.message;

/**
 * <h1>Message</h1>
 *
 * <p>Message format</p>
 */
public class Message {
    private MessageType type;
    private Object body;

    public Message(MessageType type,Object body){
        this.type = type;
        this.body = body;
    }

    /**
     * Getter
     * @return the message type.
     */
    public MessageType getType(){
        return type ;
    }

    /**
     * Getter
     * @return the message body.
     */
    public Object getBody(){
        return body;
    }
}
