package sky.chin.penpal.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.adapters.MessageAdapter;
import sky.chin.penpal.interfaces.OnRecyclerViewItemClickListener;
import sky.chin.penpal.models.Message;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.server.interfaces.ServerResponseListener;
import sky.chin.penpal.server.requests.GetMessagesRequest;
import sky.chin.penpal.utils.AuthManager;

public class MessageActivity extends SuperActivity implements OnRecyclerViewItemClickListener,
        ServerResponseListener{

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

    private void findMessages(String id) {
        mSwipeRefreshLayout.setRefreshing(true);

        AuthManager authManager = AuthManager.getInstance(this);

        Server.getInstance(this).sendRequest(
                new GetMessagesRequest.Builder()
                        .id(id)
                        .userId(authManager.getUserId())
                        .userPassword(authManager.getUserPassword())
                        .build(), this);
    }

    @Override
    public void onSuccess(JSONObject data) {
        try {
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
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onError(String content) {
        // TODO show error message
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRecyclerViewItemClicked(int position) {

    }
}
