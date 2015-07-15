package sky.chin.penpal.server;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import sky.chin.penpal.server.interfaces.ServerResponseListener;

public class ResponseErrorListener implements Response.ErrorListener {

    private ServerResponseListener mListener;

    public ResponseErrorListener(ServerResponseListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (error != null) {
            Log.d("Response Error", error.getMessage());
            mListener.onError(error.getMessage());
        }
    }
}
