package sky.chin.penpal.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.ServerRequest;

public class Server {
    private static Server mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private Server(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized Server getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Server(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void sendRequest(ServerRequest request, ServerResponseListener responseListener) {
        Log.d("Server", "=========================================================");
        Log.d("Server", "Method: " + (request.method() == Request.Method.POST ? "POST" : "GET"));
        Log.d("Server", "Url: " + request.url());
        Log.d("Server", "Params: " + request.getParams().toString());
        Log.d("Server", "---------------------------------------------------------");

        addToRequestQueue(new StringRequestWithParams(
                        request.method(),
                        request.url(),
                        request.getParams(),
                        responseListener)
        );
    }
}
