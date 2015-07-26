package sky.chin.penpal.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.activities.MessageActivity;
import sky.chin.penpal.configs.Url;
import sky.chin.penpal.models.Thread;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.utils.TimestampUtils;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> {
    private ArrayList<Thread> mDataset;
    private Activity mActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public NetworkImageView mUserPhoto;
        public TextView mText, mUsername, mMessageDate;
        public ViewHolder(View v) {
            super(v);
            mUserPhoto = (NetworkImageView) v.findViewById(R.id.userPhoto);
            mText = (TextView) v.findViewById(R.id.text);
            mUsername = (TextView) v.findViewById(R.id.username);
            mMessageDate = (TextView) v.findViewById(R.id.messageDate);
        }
    }

    public ThreadAdapter(Activity activity) {
        mActivity = activity;
        mDataset = new ArrayList<>();
    }

    public void clearThreads() {
        mDataset.clear();
        notifyDataSetChanged();
    }


    public void addThread(Thread thread) {
        mDataset.add(thread);
        notifyItemInserted(mDataset.indexOf(thread));
    }

    @Override
    public ThreadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thread, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Thread c = mDataset.get(position);
        holder.mText.setText(c.getText());
        holder.mUsername.setText(c.getUsername());
        holder.mMessageDate.setText(TimestampUtils.convertTimestampToText(c.getMessageDate()));

        holder.mUserPhoto.setDefaultImageResId(R.drawable.default_image);
        holder.mUserPhoto.setImageUrl(Url.PROFILE_PHOTOS + "/" + c.getUserPhoto(),
                Server.getInstance(mActivity).getImageLoader());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent message = new Intent(mActivity, MessageActivity.class);
                message.putExtra(MessageActivity.INTENT_MESSAGE_ID, c.getPosterId());
                mActivity.startActivity(message);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
