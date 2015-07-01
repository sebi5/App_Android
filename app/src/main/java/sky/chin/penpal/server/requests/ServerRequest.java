package sky.chin.penpal.server.requests;

import java.util.HashMap;
import java.util.Map;

public abstract class ServerRequest {

    abstract public int method();
    abstract public String url();

    protected Map<String, String> params = new HashMap<>();

    public Map<String, String> getParams() {
        addParam("p_chk", "key");
        return params;
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }
}
