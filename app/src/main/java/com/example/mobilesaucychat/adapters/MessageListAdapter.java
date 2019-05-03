package com.example.mobilesaucychat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilesaucychat.R;
import com.example.mobilesaucychat.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    Context context;
    List<Message> data;
    FirebaseAuth firebaseAuth;

    private int VIEW_TYPE_MESSAGE_SENT = 1;
    private int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    //private static LayoutInflater inflater = null;

    public MessageListAdapter(Context context, List<Message> data) {
        this.context = context;
        this.data = data;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).getUserId().equals(firebaseAuth.getCurrentUser().getUid())) {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        } else {
            return VIEW_TYPE_MESSAGE_SENT;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == VIEW_TYPE_MESSAGE_SENT) {
            return new MessageViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.sent_message_holder, viewGroup, false)
            );
        } else if (i == VIEW_TYPE_MESSAGE_RECEIVED) {
            return new MessageViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.received_message_holder, viewGroup, false)
            );
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageListRow, int i) {
        Message message = data.get(i);
        messageListRow.txtMessage.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;

        public MessageViewHolder(View messageListRow) {
            super(messageListRow);
            txtMessage = messageListRow.findViewById(R.id.txtMessage);
        }
    }
}
