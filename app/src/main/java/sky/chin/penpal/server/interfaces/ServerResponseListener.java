package sky.chin.penpal.server.interfaces;

import org.json.JSONObject;

public interface ServerResponseListener {
    void onSuccess(JSONObject data);
    void onError(String content);
}
