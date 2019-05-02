package com.example.mobilesaucychat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mobilesaucychat.adapters.MessageListAdapter;
import com.example.mobilesaucychat.models.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton imgBtnSend;
    EditText etSend;
    RecyclerView rclViewMessage;
    ArrayList messageList;
    MessageListAdapter messageListAdapter;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fbFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        firebaseAuth = FirebaseAuth.getInstance();
        fbFirestore = FirebaseFirestore.getInstance();

        messageList = new ArrayList<>();
        messageListAdapter = new MessageListAdapter(this, messageList);

        findViews();

        // Get firebase messages
        readMessages();

        setTitle("");
    }

    public void findViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundColor(Color.parseColor("#ff0000"));

        rclViewMessage = findViewById(R.id.lstViewMessage);
        rclViewMessage.setHasFixedSize(true);
        rclViewMessage.setLayoutManager(new LinearLayoutManager(this));
        rclViewMessage.setAdapter(messageListAdapter);

        etSend = findViewById(R.id.etSend);
        imgBtnSend = findViewById(R.id.imgBtnSend);

        //lstViewMessage.setAdapter(new MessageListAdapter(this, messageList));

        setListeners();
    }

    private void readMessages() {
        fbFirestore.collection("messages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> documentSnapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : documentSnapshotList) {
                        Message m = d.toObject(Message.class);
                        messageList.add(m);
                    }
                    Collections.sort(messageList, new Comparator<Message>() {
                        @Override
                        public int compare(Message message1, Message message2) {
                            return message2.getTime().compareTo(message1.getTime());
                        }
                    });
                    messageListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "Messages collection is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setListeners() {
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSendMessage();
            }
        });
    }

    private void onClickSendMessage() {
        String etText = etSend.getText().toString().trim();

        CollectionReference fbFs = fbFirestore.collection("messages");

        Message message = new Message(
                firebaseAuth.getCurrentUser().getUid(),
                etText,
                Timestamp.now()
        );

        fbFs.add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(ChatRoomActivity.this, "Message added", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatRoomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        readMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuClass) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menuClass);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.itemUserPage) {
            startActivity(new Intent(getApplicationContext(), UserPageActivity.class));
        }

        if (id == R.id.itemLogout) {
            firebaseAuth.signOut();
            Toast.makeText(getApplicationContext(), "You have signed out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
