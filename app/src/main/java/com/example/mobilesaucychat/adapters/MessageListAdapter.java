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

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    Context context;
    List<Message> data;

    private static LayoutInflater inflater = null;

    public MessageListAdapter(Context context, List<Message> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MessageViewHolder(
                LayoutInflater.from(context).inflate(R.layout.message_list_row, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
        Message message = data.get(i);

        messageViewHolder.txtMessage.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView txtMessage;

        public MessageViewHolder(View messageView) {
            super(messageView);

            txtMessage = messageView.findViewById(R.id.txtMessage);
        }
    }
}
