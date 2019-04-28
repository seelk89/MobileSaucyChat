package com.example.mobilesaucychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSignIn();
            }
        });

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSignUpWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString());
            }
        });

        if(firebaseAuth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(), ChatRoomActivity.class));
        }
    }

    private void onClickSignIn() {
        Intent newActivity = new Intent(this, ChatRoomActivity.class);
        startActivity(newActivity);
    }

    private void onClickSignUpWithEmailAndPassword(String email, String password) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"Please fill in the required email field",Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please fill in the required password field",Toast.LENGTH_SHORT).show();
        }

        if(password.length() < 6){
            Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Successfully created account",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), ChatRoomActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"E-mail or password is wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
