package sky.chin.penpal.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import sky.chin.penpal.R;
import sky.chin.penpal.interfaces.OnRecyclerViewItemClickListener;
import sky.chin.penpal.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private ArrayList<Message> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mText;
        public ViewHolder(View v) {
            super(v);
            mText = (TextView) v.findViewById(R.id.title);
        }
    }

    public MessageAdapter() {
        mDataset = new ArrayList<>();
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
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Message c = mDataset.get(position);
        holder.mText.setText(c.getText());
        holder.mText.setGravity(Gravity.RIGHT);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
