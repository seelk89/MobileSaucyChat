package com.example.mobilesaucychat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mobilesaucychat.Shared.Variables;
import com.example.mobilesaucychat.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserPageActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private FirebaseFirestore firebaseFirestore;
    private File mFile;
    private String email, password, displayName;
    private Toolbar mToolbar;
    private EditText etEmail, etDisplayname;
    private Button btnSave, btnLogout, btnDeleteUser;
    private ImageView imgFriend;
    private FirebaseAuth firebaseAuth;
    private Variables variables;
    private User currentUser;

    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        firebaseFirestore = FirebaseFirestore.getInstance();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, Variables.getInstance().CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        variables = Variables.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        findViews();
        onClickListeners();
        setSupportActionBar(mToolbar);
        setTitle("");
    }

    public void findViews() {
        mToolbar = findViewById(R.id.toolbar);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
        etEmail = findViewById(R.id.etEmail);
        etDisplayname = findViewById(R.id.etDisplayName);
        imgFriend = findViewById(R.id.imgViewUser);

        if (firebaseAuth.getCurrentUser() == null) {
            displayName = etDisplayname.getText().toString().trim();
            email = getIntent().getSerializableExtra(variables.EMAIL_INFO).toString().trim();
            password = getIntent().getSerializableExtra(variables.PASSWORD_INFO).toString().trim();

            etEmail.append(email);
        } else {
            getCurrentUserData();
            etEmail.setText(firebaseAuth.getCurrentUser().getEmail());
        }
        if (firebaseAuth.getCurrentUser() == null) {
            btnLogout.setVisibility(View.GONE);
            btnDeleteUser.setVisibility(View.GONE);
        }
    }

    public void onClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null) {
                    updateUser();
                } else
                    signUp();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        imgFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuClass) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menuClass);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                showPictureTaken(mFile);

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Canceled...", Toast.LENGTH_LONG).show();
                return;

            } else
                Toast.makeText(this, "Picture NOT taken - unknown error...", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * open camera intent
     */
    private void openCamera() {
        mFile = getOutputMediaFile(); // create a file to save the image
        if (mFile == null) {
            Toast.makeText(this, "Could not create file...", Toast.LENGTH_LONG).show();
            return;
        }
        // create Intent to take a picture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));

        Log.d(variables.LOGTAG, "file uri = " + Uri.fromFile(mFile).toString());

        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(variables.LOGTAG, "camera app will be started");
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } else
            Log.d(variables.LOGTAG, "camera app could NOT be started");
    }

    /**
     * Create a File for saving an image
     */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Camera01");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("xyz", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String postfix = "jpg";
        String prefix = "IMG";

        File mediaFile = new File(mediaStorageDir.getPath() +
                File.separator + prefix +
                "_" + timeStamp + "." + postfix);

        return mediaFile;
    }

    /**
     * upload user picture to the firebase storage
     */
    public void uploadPicture(File newFile) {
        Uri file = Uri.fromFile(newFile);
        //generate new id for the image
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String generatedId = database.getReference("users").push().getKey();

        StorageReference riversRef = mStorageRef.child("user-pictures/" + generatedId);

        // create metadata for the image
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("originalName", newFile.getName())
                .setCustomMetadata("userId", firebaseAuth.getCurrentUser().getUid())
                .build();

        // upload the image with metadata to the firestore
        riversRef.putFile(file,metadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Handle successful uploads
                        Log.d(variables.LOGTAG, "onSuccess: You just uploaded a picture to firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d(variables.LOGTAG, "onFailure: Something went wrong: " + exception);
                    }
                });
    }

    /**
     * display taken picture
     *
     */
    private void showPictureTaken(File f) {
        imgFriend.setImageURI(Uri.fromFile(f));
    }

    /**
     * remove user from auth0 and "users" db
     */
    private void deleteUser() {
        //remove user from db
        firebaseFirestore.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .delete();
        Toast.makeText(getApplicationContext(), "We're sad to see you leave...", Toast.LENGTH_SHORT).show();

        // remove user from auth0

        firebaseAuth.getCurrentUser().delete();
        firebaseAuth.signOut();

        //close previous activities and open main
        Intent intents = new Intent(getApplicationContext(), MainActivity.class);
        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intents);
        finish();
    }

    /**
     * create new user
     */
    public void signUp() {
        // add user to the auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(
                                    email,
                                    displayName = etDisplayname.getText().toString().trim()
                            );
                            //add user to the db
                            firebaseFirestore.collection("users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .set(user);
                            Toast.makeText(getApplicationContext(), "Successfully created account", Toast.LENGTH_SHORT).show();

                            // check if users image was changed
                            if(mFile != null) {
                                // change users profile picture
                                uploadPicture(mFile);
                            }

                            // close previous activity and open chatRoom
                            Intent intents = new Intent(getApplicationContext(), ChatRoomActivity.class);
                            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intents);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Something is wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Update current user
     */
    public void updateUser() {
        User user = new User(
                email = etEmail.getText().toString().trim(),
                displayName = etDisplayname.getText().toString().trim()
        );
        Log.d("TAG", "" + currentUser);
        // get users current image
        if(!currentUser.getImageId().equals("")) {
            user.setImageId(currentUser.getImageId());
        }

        // check if displayName was changed
        if(user.getDisplayName().equals("")) {
            // get users displayName
            user.setDisplayName(currentUser.getDisplayName());
        }

        // update old user in the firebase
        firebaseFirestore.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(user);

        // check if users image was changed
        if(mFile != null) {
            // change users profile picture
            uploadPicture(mFile);
        }
        Toast.makeText(getApplicationContext(), "Successfully updated account", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), ChatRoomActivity.class));
    }

    /**
     * get users data from database
     */
    private void getCurrentUserData() {
        String email = firebaseAuth.getCurrentUser().getEmail();
        firebaseFirestore.collection("users").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Log.d(variables.LOGTAG, "isSuccessful: You received user information");

                        //check for displayName
                        if(document.get("displayName") != null) {
                            currentUser = new User(document.get("email").toString(),document.get("displayName").toString());
                        } else  {
                            currentUser = new User(document.get("email").toString(),"");
                        }

                        //if user has display name
                        if(document.get("displayName") != null) {
                            etDisplayname.setText(document.get("displayName").toString());
                        }
                        //if user has image
                        if(document.get("imageId") != null) {
                            currentUser.setImageId(document.get("imageId").toString());
                            //display users image
                            displayUsersImage();
                        } else {
                            currentUser.setImageId("");
                        }
                    }
                } else {
                    Log.d(variables.LOGTAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * display profile picture
     */
    private void displayUsersImage() {
        mStorageRef.child("user-pictures/"+ currentUser.getImageId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(variables.LOGTAG, "onSuccess: You got a picture from firestore");

                //display image on imageView
                Glide.with(getApplicationContext()).load(uri).into(imgFriend);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(variables.LOGTAG, "onFailure: Something went wrong: " + exception);
            }
        });
    }
}

