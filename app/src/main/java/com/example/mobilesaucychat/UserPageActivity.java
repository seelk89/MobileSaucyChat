package com.example.mobilesaucychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mobilesaucychat.Shared.Variables;
import com.example.mobilesaucychat.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserPageActivity extends AppCompatActivity {

    String email, password, displayName;
    Toolbar mToolbar;
    EditText etEmail, etPassword, etDisplayname;
    Button btnSave, btnLogout, btnDeleteUser;
    ImageView imgFriend;
    FirebaseAuth firebaseAuth;
    Variables variables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        variables = Variables.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();

        findViews();
        onClickListeners();
        setSupportActionBar(mToolbar);
        setTitle("");
        if (firebaseAuth.getCurrentUser() == null) {
            btnLogout.setVisibility(View.GONE);
            btnDeleteUser.setVisibility(View.INVISIBLE);
        }
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
            etEmail.setText(firebaseAuth.getCurrentUser().getEmail());
            etDisplayname.setText(firebaseAuth.getCurrentUser().getDisplayName());
        }
    }

    public void onClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }

    private void deleteUser() {
        firebaseAuth.getCurrentUser().delete();
        Toast.makeText(getApplicationContext(), "We're sad to see you leave...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuClass) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menuClass);
        return true;
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
}

