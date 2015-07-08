package sky.chin.penpal.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import sky.chin.penpal.R;
import sky.chin.penpal.adapters.MessageAdapter;
import sky.chin.penpal.core.events.message.MessageEvent;
import sky.chin.penpal.interfaces.OnRecyclerViewItemClickListener;
import sky.chin.penpal.models.Message;
import sky.chin.penpal.utils.AuthManager;

public class MessageActivity extends AppCompatActivity implements OnRecyclerViewItemClickListener,
        Observer{

    private static final String LOG = MessageActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EditText messageBox;
    private Button sendButton;

    private ArrayList<Message> mMessages = new ArrayList<>();
    private String id;

    private MessageEvent mEvent;
    private AuthManager authManager = new AuthManager(this);

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
                mEvent.get(id, authManager.getUserId(), authManager.getUserPassword());
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MessageAdapter(mMessages, this);
        mRecyclerView.setAdapter(mAdapter);

        mEvent = new MessageEvent(this);
        mEvent.addObserver(this);

        mEvent.get(id, authManager.getUserId(), authManager.getUserPassword());

        messageBox = (EditText) findViewById(R.id.messageBox);
        sendButton = (Button) findViewById(R.id.btnSendMessage);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = messageBox.getText().toString();

                if ("".equals(text)) return;

                mEvent.send(text, id, authManager.getUserId(), authManager.getUserPassword());
            }
        });
    }

    @Override
    public void onRecyclerViewItemClicked(int position) {

    }

    @Override
    public void update(Observable observable, Object o) {
        mMessages = (ArrayList<Message>) o;
        mAdapter.notifyDataSetChanged();
    }
}
