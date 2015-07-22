package sky.chin.penpal.models;

public class Message {

    private String text;
    private String timestamp;
    private String senderId;

    private String profilePhoto;

    public Message(String text, String timestamp, String senderId, String profilePhoto) {
        this.text = text;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.profilePhoto = profilePhoto;
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

    public String getProfilePhoto() {
        return profilePhoto;
    }
}
