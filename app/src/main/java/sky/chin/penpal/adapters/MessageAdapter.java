package sky.chin.penpal.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.configs.Url;
import sky.chin.penpal.models.Message;
import sky.chin.penpal.server.Server;
import sky.chin.penpal.utils.PrefUtils;
import sky.chin.penpal.utils.TimestampUtils;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private ArrayList<Message> mDataset;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mText, mTimestamp;
        public NetworkImageView mProfilePhoto;
        public ViewHolder(View v) {
            super(v);
            mText = (TextView) v.findViewById(R.id.text);
            mTimestamp = (TextView) v.findViewById(R.id.messageDate);
            mProfilePhoto = (NetworkImageView) v.findViewById(R.id.userPhoto);
        }
    }

    public MessageAdapter(Context context) {
        mContext = context;
        mDataset = new ArrayList<>();
    }

    public void addMessages(ArrayList<Message> messages) {
        mDataset.addAll(0, messages);
        notifyDataSetChanged();
    }

    public void clearMessages() {
        mDataset.clear();
    }

    public void addMessage(Message message) {
        mDataset.add(message);
        notifyItemInserted(mDataset.indexOf(message));
    }

    public void removeMessage(Message message) {
        notifyItemRemoved(mDataset.indexOf(message));
        mDataset.remove(message);
    }

    public ArrayList<Message> getMessages() {
        return mDataset;
    }

    @Override
    public int getItemViewType(int position) {
        Message c = mDataset.get(position);
        return c.getPosterId().equals(PrefUtils.getPrefsUserId(mContext)) ?
                1/* Sent by me */:
                0/* Sent by friend */;
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        switch (viewType) {
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false));
            case 1:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_me, parent, false));
        }
        return null;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Message c = mDataset.get(position);
        holder.mText.setText(c.getText());
        holder.mTimestamp.setText(TimestampUtils.convertTimestampToText(c.getMessageDate()));

        holder.mProfilePhoto.setDefaultImageResId(R.drawable.default_img);
        holder.mProfilePhoto.setImageUrl(Url.PROFILE_PHOTOS + "/" + c.getUserPhoto(),
                Server.getInstance(mContext).getImageLoader());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
