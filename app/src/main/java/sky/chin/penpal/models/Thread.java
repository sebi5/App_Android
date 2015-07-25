package sky.chin.penpal.models;

public class Thread {

    private String id;
    private String text;
    private String messageDate;
    private String userPhoto;
    private String username;
    private String posterId;
    private boolean read;

    public Thread(String id,
                  String text,
                  String messageDate,
                  String userPhoto,
                  String username,
                  String posterId,
                  boolean read) {
        this.id = id;
        this.text = text;
        this.messageDate = messageDate;
        this.userPhoto = userPhoto;
        this.username = username;
        this.posterId = posterId;
        this.read = read;
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

    public String getUserPhoto() {
        return userPhoto;
    }

    public String getUsername() {
        return username;
    }

    public String getPosterId() {
        return posterId;
    }

    public boolean isRead() {
        return read;
    }
}
