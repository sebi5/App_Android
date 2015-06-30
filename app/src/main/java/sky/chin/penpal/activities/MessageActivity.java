package sky.chin.penpal.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sky.chin.penpal.R;
import sky.chin.penpal.adapters.MessageAdapter;
import sky.chin.penpal.configs.Url;
import sky.chin.penpal.interfaces.OnRecyclerViewItemClickListener;
import sky.chin.penpal.models.Message;
import sky.chin.penpal.utils.AuthManager;
import sky.chin.penpal.utils.VolleySingleton;

public class MessageActivity extends SuperActivity implements OnRecyclerViewItemClickListener {

    private static final String LOG = MessageActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<Message> mMessages = new ArrayList<>();
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (getIntent() != null) {
            id = getIntent().getStringExtra("id");
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                findMessages(id);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MessageAdapter(mMessages, this);
        mRecyclerView.setAdapter(mAdapter);

        findMessages(id);
    }

    private void findMessages(final String id) {
        mSwipeRefreshLayout.setRefreshing(true);

        StringRequest jsObjRequest = new StringRequest
                (Request.Method.POST, Url.MESSAGES, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG, "Response: " + response);

                        try {
                            JSONObject jsonResp = new JSONObject(response);
                            JSONArray dataArray = jsonResp.getJSONArray("data");

                            JSONObject data;
                            for (int i = 0; i < dataArray.length(); i++) {
                                data = dataArray.getJSONObject(i);
                                String code = data.getString("code");
                                if ("0".equals(code)) {
                                    JSONArray messages = data.getJSONArray("messages")
                                            .getJSONArray(0);

                                    // Clear old records
                                    mMessages.clear();

                                    for (int k = 0; k < messages.length(); k++) {
                                        JSONArray item = messages.getJSONArray(k);
                                        mMessages.add(new Message(item.getString(1),
                                                item.getString(0),
                                                item.getString(2)));
                                    }

                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    // TODO show error message
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG, "Error: " + error.getMessage());
                        // TODO show error message
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);

                AuthManager authManager = AuthManager.getInstance(MessageActivity.this);

                params.put("u_id", authManager.getUserId());
                params.put("u_pass", authManager.getUserPassword());
                params.put("p_chk", "key");

                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    @Override
    public void onRecyclerViewItemClicked(int position) {

    }
}
