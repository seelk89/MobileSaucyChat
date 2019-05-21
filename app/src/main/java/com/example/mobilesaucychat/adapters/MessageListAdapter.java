package com.example.mobilesaucychat.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobilesaucychat.R;
import com.example.mobilesaucychat.Shared.Variables;
import com.example.mobilesaucychat.models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> data;
    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef;
    private FirebaseFirestore firebaseFirestore;
    private HashMap<String,Uri> imageHashMap;
    private Variables variables;

    private int VIEW_TYPE_MESSAGE_SENT = 1;
    private int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    public MessageListAdapter(Context context, List<Message> data) {
        this.context = context;
        this.data = data;
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        variables = Variables.getInstance();
        imageHashMap = new HashMap<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position).getUserId().equals(firebaseAuth.getCurrentUser().getUid())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
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
        Log.d(variables.LOGTAG, " " + messageListRow.txtMessage.getHeight());
        Log.d(variables.LOGTAG, " " + messageListRow.txtMessage.getWidth());
        if(message.getImageId()!= null) {
            //show message Image
            displayMessageImage(message.getImageId(),messageListRow.messageImage);
        } else {
            //hide imageView
            messageListRow.messageImage.getLayoutParams().width = 100;
            messageListRow.messageImage.getLayoutParams().height = 100;
            messageListRow.messageImage.requestLayout();
        }
        //display profile picture
            displayProfilePicture(message.getUserId(), messageListRow.userPicture);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        ImageView messageImage;
        ImageView userPicture;

        public MessageViewHolder(View messageListRow) {
            super(messageListRow);
            userPicture = messageListRow.findViewById(R.id.userPicture);
            txtMessage = messageListRow.findViewById(R.id.txtMessage);
            messageImage = messageListRow.findViewById(R.id.messageImage);
        }
    }

    /**
     * display users profile picture
     */
    private void displayProfilePicture(final String userId, final ImageView imageView) {
        firebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        if(document.get("imageId") != null) {

                            // check if image is already in the hashMap
                        if(imageHashMap.containsKey(document.get("imageId").toString())) {
                            // get image from the hashMap
                            Uri uri = imageHashMap.get(document.get("imageId").toString());

                            //display profile picture
                            Glide.with(context).load(uri).into(imageView);
                        } else {
                            // get profile picture from the database and display it
                            getProfilePicture(document.get("imageId").toString(), imageView);
                        }
                        }
                    } else {
                        Log.d(variables.LOGTAG, "No such document");
                    }
                } else {
                    Log.d(variables.LOGTAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     *get and display message image
     */
    private void displayMessageImage(String imageId, final ImageView imageView) {
        // get picture from storage
        mStorageRef.child("message-pictures/"+ imageId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //change size of imageView
                imageView.getLayoutParams().height = 750;
                imageView.getLayoutParams().width = 750;
                imageView.requestLayout();

                //display image
                Glide.with(context).load(uri).into(imageView);

                Log.d(variables.LOGTAG, "Success: You got image from firestorage");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(variables.LOGTAG, "" + exception);
            }
        });
    }

    /**
     *get users profile picture from the db
     */
    private void getProfilePicture(final String imageId, final ImageView imageView) {
        mStorageRef.child("user-pictures/"+ imageId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // add image to the hashmap
                imageHashMap.put(imageId,uri);

                // display image
                Glide.with(context).load(uri).into(imageView);

                Log.d(variables.LOGTAG, "Youve got profile picture");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(variables.LOGTAG, "" + exception);
            }
        });
    }
}
