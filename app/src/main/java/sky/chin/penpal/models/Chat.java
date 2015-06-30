package sky.chin.penpal.models;

public class Chat {

    private String id;
    private String title;
    private String timestamp;
    private String profilePhoto;

    public Chat(String id, String title, String timestamp, String profilePhoto) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.profilePhoto = profilePhoto;
    }

    public boolean hasProfilePhoto() {
        return profilePhoto != null && !"".equals(profilePhoto);
    }

    public String getTitle() {
        return title;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public String getId() {
        return id;
    }
}
