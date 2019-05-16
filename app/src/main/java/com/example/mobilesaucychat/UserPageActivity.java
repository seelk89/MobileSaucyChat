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

    private final static String LOGTAG = "Camtag";
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int PHOTO_REQUEST_CODE = 1;
    private StorageReference mStorageRef;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseFirestore firebaseFirestore ;
    private String path;
    String currentUserId;
    File mFile;
    String email, password, displayName;
    Toolbar mToolbar;
    EditText etEmail, etPassword, etDisplayname;
    Button btnSave, btnLogout, btnDeleteUser;
    ImageView imgFriend;
    FirebaseAuth firebaseAuth;
    Variables variables;
    User currentUser;

    private static final int REQUEST_TAKE_PHOTO = 1;

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
        etPassword = findViewById(R.id.etPassword);
        etDisplayname = findViewById(R.id.etDisplayName);
        imgFriend = findViewById(R.id.imgViewUser);

        if (firebaseAuth.getCurrentUser() == null) {
            displayName = etDisplayname.getText().toString().trim();
            email = getIntent().getSerializableExtra(variables.EMAIL_INFO).toString().trim();
            password = getIntent().getSerializableExtra(variables.PASSWORD_INFO).toString().trim();

            etEmail.append(email);
            etPassword.append(password);
        } else {
            getCurrentUserData();
            etEmail.setText(firebaseAuth.getCurrentUser().getEmail());
            etDisplayname.setText(firebaseAuth.getCurrentUser().getDisplayName());
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

    private void openCamera() {
        mFile = getOutputMediaFile(); // create a file to save the image
        if (mFile == null) {
            Toast.makeText(this, "Could not create file...", Toast.LENGTH_LONG).show();
            return;
        }
        // create Intent to take a picture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));

        Log.d(LOGTAG, "file uri = " + Uri.fromFile(mFile).toString());

        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(LOGTAG, "camera app will be started");
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } else
            Log.d(LOGTAG, "camera app could NOT be started");
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

    public void uploadPicture(File newFile) {
        Uri file = Uri.fromFile(newFile);
        String generatedId = database.getReference("users").push().getKey();

        StorageReference riversRef = mStorageRef.child("user-pictures/" + generatedId);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("originalName", newFile.getName())
                .setCustomMetadata("userId", currentUserId)
                .build();


        riversRef.putFile(file,metadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                         //Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Log.d("xyz", "onSuccess: You just uploaded a picture to firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d("xyz", "onFailure: Something went wrong...");
                    }
                });
    }

    private void showPictureTaken(File f) {
        imgFriend.setImageURI(Uri.fromFile(f));
    }

    //remove user from auth0 and "users" db.
    private void deleteUser() {
        firebaseAuth.getCurrentUser().delete();
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .delete();
        Toast.makeText(getApplicationContext(), "We're sad to see you leave...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    public void signUp() {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(
                                    email,
                                    displayName = etDisplayname.getText().toString().trim()
                            );
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .set(user);
                            Toast.makeText(getApplicationContext(), "Successfully created account", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), ChatRoomActivity.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "Something is wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void updateUser() {
        User user = new User(
                email = etEmail.getText().toString().trim(),
                displayName = etDisplayname.getText().toString().trim()
        );
        user.setImageId(currentUser.getImageId());
        if(user.getDisplayName().equals("")) {
            user.setDisplayName(currentUser.getDisplayName());
        }
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(user);

        if(mFile != null) {
            uploadPicture(mFile);
        }
        Toast.makeText(getApplicationContext(), "Successfully updated account", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), ChatRoomActivity.class));
    }

    private void getCurrentUserData() {
        String email = firebaseAuth.getCurrentUser().getEmail();
        firebaseFirestore.collection("users").whereEqualTo("email",email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        currentUserId = document.getId();
                        currentUser = new User(document.get("email").toString(),document.get("displayName").toString());
                        if(document.get("imageId") != null) {
                            currentUser.setImageId(document.get("imageId").toString());

                            //display users image
                            displayUsersImage();
                        }
                    }
                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void displayUsersImage() {
        mStorageRef.child("user-pictures/"+ currentUser.getImageId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(imgFriend);
                // Got the download URL'
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("ERROR", "" + exception);
            }
        });
    }
}

