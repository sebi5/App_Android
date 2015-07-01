package sky.chin.penpal.server.requests;

import com.android.volley.Request;

import sky.chin.penpal.configs.Url;

public class LoginRequest extends ServerRequest {

    private String user;
    private String password;

    private LoginRequest(Builder builder) {
        setUser(builder.user);
        setPassword(builder.password);
    }

    public void setUser(String user) {
        this.user = user;
        addParam("user", this.user);
    }

    public void setPassword(String password) {
        this.password = password;
        addParam("password", this.password);
    }

    @Override
    public int method() {
        return Request.Method.POST;
    }

    @Override
    public String url() {
        return Url.LOGIN;
    }

    public static class Builder {

        private String user;
        private String password;

        public Builder user(String u){
            this.user = u;
            return this;
        }

        public Builder password(String p){
            this.password = p;
            return this;
        }

        public LoginRequest build(){ return new LoginRequest(this); }

    }
}
