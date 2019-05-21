package com.example.mobilesaucychat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mobilesaucychat.Shared.Variables;
import com.example.mobilesaucychat.adapters.MessageListAdapter;
import com.example.mobilesaucychat.models.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.Nullable;

public class ChatRoomActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton imgBtnSend;
    private ImageButton imgBtnGallery;
    private EditText etSend;
    private RecyclerView rclViewMessage;
    private ArrayList messageList;
    private MessageListAdapter messageListAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private StorageReference mStorageRef;
    private FirebaseFirestore firebaseFirestore;
    private Variables variables;

    private static int PICK_IMAGE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        variables = Variables.getInstance();

        messageList = new ArrayList<>();
        messageListAdapter = new MessageListAdapter(this, messageList);
        findViews();

    }

    @Override
    protected void onStart() {
        super.onStart();
        messageList.clear();

        firebaseFirestore.collection("messages").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Error while loading", Toast.LENGTH_SHORT).show();
                    return;
                }
                //get new message from the db
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = dc.getDocument();
                        Message m = documentSnapshot.toObject(Message.class);

                        //add new message to the list
                        messageList.add(m);

                        //scroll to the bottom on new message
                        rclViewMessage.smoothScrollToPosition(messageListAdapter.getItemCount() -1);
                    }
                    // sort messages in the list
                    Collections.sort(messageList, new Comparator<Message>() {
                        @Override
                        public int compare(Message message1, Message message2) {
                            return message1.getTime().compareTo(message2.getTime());
                        }
                    });
                    messageListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void findViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundColor(Color.parseColor("#DC143C"));

        rclViewMessage = findViewById(R.id.lstViewMessage);
        rclViewMessage.setHasFixedSize(true);
        rclViewMessage.setLayoutManager(new LinearLayoutManager(this));
        rclViewMessage.setAdapter(messageListAdapter);

        etSend = findViewById(R.id.etSend);
        imgBtnSend = findViewById(R.id.imgBtnSend);
        imgBtnGallery = findViewById(R.id.imgBtnGallery);

        setTitle("");

        openGalleryOnClick();
        sendMessageOnClick();
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

    /**
     * Open image chooser
     */
    private void openGalleryOnClick() {
        imgBtnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Create an Intent with action as ACTION_PICK
                Intent intent=new Intent(Intent.ACTION_PICK);
                // Sets the type as image/*. This ensures only components of type image are selected
                intent.setType("image/*");
                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                // Launching the Intent
                startActivityForResult(intent,PICK_IMAGE);
            }
        });
    }

    /**
     * send new message
     */
    private void sendMessageOnClick() {
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etSend.getText().toString().trim();

                //create new message
                final Message message = new Message(
                        firebaseAuth.getCurrentUser().getUid(),
                        text,
                        Timestamp.now()
                );

                //send message to the db
                firebaseFirestore.collection("messages").add(message)
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
                etSend.setText("");
            }
        });
    }

    /**
     * send selected image to the database
     */
    private void saveImageToTheDatabase(Uri pictureUri) {
        // get user id
        String userId = firebaseAuth.getCurrentUser().getUid();

        // get selected file
        File newFile = new File(pictureUri.getPath());

        // create id for the image
        String generatedId = database.getReference("messages").push().getKey();

        StorageReference riversRef = mStorageRef.child("message-pictures/" + generatedId);

        // set metadata for the image
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("originalName", newFile.getName())
                .setCustomMetadata("userId", userId)
                .build();

        /*Bitmap bm = BitmapFactory.decodeFile(newFile.getPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);*/

        // send image to the firestore
        riversRef.putFile(pictureUri,metadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Log.d(variables.LOGTAG, "onSuccess: You just uploaded a picture to firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d(variables.LOGTAG, "onFailure: Something went wrong...");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null ) {
            // selected picture Uri
            Uri pictureUri = data.getData();

            if(pictureUri != null) {
                // send new image to the fire storage
                saveImageToTheDatabase(pictureUri);
            }
        }
    }
}