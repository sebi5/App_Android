package sky.chin.penpal.models;

public class Message {

    private String text;
    private String timestamp;
    private String senderId;

    public Message(String text, String timestamp, String senderId) {
        this.text = text;
        this.timestamp = timestamp;
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSenderId() {
        return senderId;
    }
}
