package sky.chin.penpal.server.requests;

import com.android.volley.Request;

import sky.chin.penpal.configs.Url;

public class RegisterRequest extends ServerRequest {

    @Override
    public int method() {
        return Request.Method.POST;
    }

    @Override
    public String url() {
        return Url.SIGNUP;
    }

    private String username;
    private String fullName;
    private String password;
    private String email;
    private String gender;
    private String birthDate;
    private String country;
    private String region;

    private RegisterRequest(Builder builder) {
        setUsername(builder.username);
        setFullName(builder.fullName);
        setPassword(builder.password);
        setEmail(builder.email);
        setGender(builder.gender);
        setBirthDate(builder.birthDate);
        setCountry(builder.country);
        setRegion(builder.region);

        // Preset a parameter
        addParam("device", "2");
    }

    public void setUsername(String username) {
        this.username = username;
        addParam("username", this.username);
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        addParam("fullname", this.fullName);
    }

    public void setPassword(String password) {
        this.password = password;
        addParam("password", this.password);
    }

    public void setEmail(String email) {
        this.email = email;
        addParam("user_email", this.email);
    }

    public void setGender(String gender) {
        this.gender = gender;
        addParam("gender", this.gender);
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
        addParam("birthdate", this.birthDate);
    }

    public void setCountry(String country) {
        this.country = country;
        addParam("country", this.country);
    }

    public void setRegion(String region) {
        this.region = region;
        addParam("region", this.region);
    }

    public static class Builder {

        private String username;
        private String fullName;
        private String password;
        private String email;
        private String gender;
        private String birthDate;
        private String country;
        private String region;

        public Builder username(String value){
            this.username = value;
            return this;
        }

        public Builder fullName(String value){
            this.fullName = value;
            return this;
        }

        public Builder password(String value){
            this.password = value;
            return this;
        }

        public Builder email(String value){
            this.email = value;
            return this;
        }

        public Builder gender(String value){
            this.gender = value;
            return this;
        }

        public Builder birthDate(String value){
            this.birthDate = value;
            return this;
        }

        public Builder country(String value){
            this.country = value;
            return this;
        }

        public Builder region(String value){
            this.region = value;
            return this;
        }

        public RegisterRequest build(){ return new RegisterRequest(this); }

    }
}
