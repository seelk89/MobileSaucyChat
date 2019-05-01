package com.example.mobilesaucychat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mobilesaucychat.R;
import com.example.mobilesaucychat.models.Message;

import java.util.List;

public class MessageListAdapter extends BaseAdapter {
    List<Message> data;
    Context context;
    private static LayoutInflater inflater = null;

    public MessageListAdapter(Context context, List<Message> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.message_list_row, null);
        TextView textView = view.findViewById(R.id.txtMessage);
        textView.setText(data.get(position).getMessage());

        return view;
    }
}
