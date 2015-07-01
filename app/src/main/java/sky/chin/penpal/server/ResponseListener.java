package sky.chin.penpal.server;

import android.util.Log;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sky.chin.penpal.server.interfaces.ServerResponseListener;

public class ResponseListener implements Response.Listener {

    private ServerResponseListener mListener;
    final private static String RESPONSE_CODE_OK = "0";

    public ResponseListener(ServerResponseListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onResponse(Object responseObject) {
        String response = (String) responseObject;

        try {
            JSONObject jsonResp = new JSONObject(response);
            JSONArray dataArray = jsonResp.getJSONArray("data");

            JSONObject data;
            data = dataArray.getJSONObject(0);
            String code = data.getString("code");
            if (RESPONSE_CODE_OK.equals(code)) {
                Log.d("Response", "Response OK");
                mListener.onSuccess(data);
            } else {
                Log.d("Response", "Response Not OK");
                mListener.onError(data.getString("message"));
            }

        } catch (JSONException e) {
            Log.d("Response", "JSONException, " + e.getMessage());
            mListener.onError(e.getMessage());
        }

        Log.d("Response", response);
    }
}
