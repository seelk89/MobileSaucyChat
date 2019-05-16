package com.example.mobilesaucychat.adapters;

import android.content.Context;
import android.media.Image;
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
import com.example.mobilesaucychat.models.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    Context context;
    List<Message> data;
    FirebaseAuth firebaseAuth;
    StorageReference mStorageRef;
    FirebaseFirestore firebaseFirestore ;
    private HashMap<String,Uri> imageHashMap;

    private int VIEW_TYPE_MESSAGE_SENT = 1;
    private int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    //private static LayoutInflater inflater = null;

    public MessageListAdapter(Context context, List<Message> data) {
        this.context = context;
        this.data = data;
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
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

        if(message.getImageId()!= null) {
            //show Image
            displayMessageImage(message.getImageId(),messageListRow.messageImage);
        } else {
            messageListRow.messageImage.getLayoutParams().width = 0;
            messageListRow.messageImage.getLayoutParams().height = 0;
            messageListRow.messageImage.requestLayout();
        }
        //get usersPictureId
            getImageIdFromUser(message.getUserId(), messageListRow.userPicture);

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

    private void getImageIdFromUser(final String userId, final ImageView imageView) {
        firebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        if(document.get("imageId") != null) {

                        if(imageHashMap.containsKey(document.get("imageId").toString())) {
                            Uri uri = imageHashMap.get(document.get("imageId").toString());
                            Glide.with(context).load(uri).into(imageView);
                        } else {
                            displayUsersImage(document.get("imageId").toString(), imageView);
                        }
                        }
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    private void displayMessageImage(String imageId, final ImageView imageView) {
        mStorageRef.child("message-pictures/"+ imageId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageView.getLayoutParams().height = 750;
                imageView.getLayoutParams().width = 750;
                imageView.requestLayout();
                Glide.with(context).load(uri).into(imageView);
                // Got the download URL'
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("ERROR", "" + exception);
            }
        });
    }

    private void displayUsersImage(final String imageId, final ImageView imageView) {
        mStorageRef.child("user-pictures/"+ imageId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL'
                imageHashMap.put(imageId,uri);
                Glide.with(context).load(uri).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("ERROR", "" + exception);
            }
        });
    }
}
