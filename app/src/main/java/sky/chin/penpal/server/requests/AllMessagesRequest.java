package sky.chin.penpal.server.requests;

import com.android.volley.Request;

import sky.chin.penpal.configs.Url;

public class AllMessagesRequest extends ServerRequest {
    @Override
    public int method() {
        return Request.Method.POST;
    }

    @Override
    public String url() {
        return Url.MESSAGES_ALL;
    }

    private String userId;
    private String userPassword;

    private AllMessagesRequest(Builder builder) {
        setUserId(builder.userId);
        setUserPassword(builder.userPassword);
    }

    public void setUserId(String userId) {
        this.userId = userId;
        addParam("u_id", this.userId);
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        addParam("u_pass", this.userPassword);
    }

    public static class Builder {

        private String userId;
        private String userPassword;

        public Builder userId(String value){
            this.userId = value;
            return this;
        }

        public Builder userPassword(String value){
            this.userPassword = value;
            return this;
        }

        public AllMessagesRequest build(){ return new AllMessagesRequest(this); }

    }
}
