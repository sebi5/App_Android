package sky.chin.penpal.server;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

import sky.chin.penpal.server.interfaces.ServerResponseListener;

public class StringRequestWithParams extends StringRequest {

    private Map<String, String> mParams;

    public StringRequestWithParams(int method,
                                   String url,
                                   Map<String, String> parameters,
                                   ServerResponseListener mListener) {

        super(method, url, new ResponseListener(mListener), new ResponseErrorListener(mListener));
        this.mParams = parameters;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return this.mParams;
    }
}
