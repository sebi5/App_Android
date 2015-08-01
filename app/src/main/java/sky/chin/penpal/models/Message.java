package sky.chin.penpal.models;

public class Message {

    private String id;
    private String text;
    private String messageDate;
    private String posterId;
    private String userPhoto;
    private String masterId;

    public Message(String id, String text, String messageDate, String posterId, String userPhoto, String masterId) {
        this.id = id;
        this.text = text;
        this.messageDate = messageDate;
        this.posterId = posterId;
        this.userPhoto = userPhoto;
        this.masterId = masterId;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getMessageDate() {
        return messageDate;
    }

    public String getPosterId() {
        return posterId;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public String getMasterId() {
        return masterId;
    }
}
