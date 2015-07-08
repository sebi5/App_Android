package sky.chin.penpal.server.requests;

import com.android.volley.Request;

import sky.chin.penpal.configs.Url;

public class SendMessageRequest extends ServerRequest {
    @Override
    public int method() {
        return Request.Method.POST;
    }

    @Override
    public String url() {
        return Url.MESSAGE;
    }

    private String id;
    private String userId;
    private String userPassword;
    private String message;

    private SendMessageRequest(Builder builder) {
        setId(builder.id);
        setUserId(builder.userId);
        setUserPassword(builder.userPassword);
        setMessage(builder.message);
    }

    public void setId(String id) {
        this.id = id;
        addParam("id", this.id);
    }

    public void setUserId(String userId) {
        this.userId = userId;
        addParam("u_id", this.userId);
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        addParam("u_pass", this.userPassword);
    }

    public void setMessage(String message) {
        this.message = message;
        addParam("message", this.message);
    }

    public static class Builder {

        private String id;
        private String userId;
        private String userPassword;
        private String message;

        public Builder id(String value){
            this.id = value;
            return this;
        }

        public Builder userId(String value){
            this.userId = value;
            return this;
        }

        public Builder userPassword(String value){
            this.userPassword = value;
            return this;
        }

        public Builder message(String value){
            this.message = value;
            return this;
        }

        public SendMessageRequest build(){ return new SendMessageRequest(this); }

    }
}
