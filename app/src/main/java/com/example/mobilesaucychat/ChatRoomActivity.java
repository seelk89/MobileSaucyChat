package com.example.mobilesaucychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatRoomActivity extends AppCompatActivity {

    Toolbar mToolbar;
   ImageButton imgBtnSend;
    EditText etSend;
    ListView lstViewMessage;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        firebaseAuth.getInstance();

        findViews();
        setTitle("");
    }

    public void findViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        lstViewMessage = findViewById(R.id.lstViewMessage);
        etSend = findViewById(R.id.etSend);
        imgBtnSend = findViewById(R.id.imgBtnSend);

        setListeners();
    }

    private void setListeners()
    {
     imgBtnSend.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             onClickSendMessage();
             Toast.makeText(getApplicationContext(), "You have clicked", Toast.LENGTH_SHORT).show();
         }
     });
    }

    private void onClickSendMessage() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue(etSend.toString().trim());
        Toast.makeText(getApplicationContext(), "You have sent a message", Toast.LENGTH_SHORT).show();
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
