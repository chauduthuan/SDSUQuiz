package com.example.thuanpc.sdsupracticequestions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.thuanpc.sdsupracticequestions.API.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener{

    public static final String APP_PREFERENCE = "SDSU chat pref";
    public static final String ADMIN_STUDENT = "AdminStudents";
    public static final String STUDENTS = "Students";
    public static final String ADMIN = "admin";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";

    final int SIGN_UP_REQUEST_CODE = 1;

    EditText emailEditText;
    EditText passwordEditText;
    Button logInButton;
    Button signUpButton;

    String email;
    String password;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    boolean isGetStudentLoaded;
    boolean isGetAdminLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Log In");
        initializeFields();
    }

    private void initializeFields() {
        emailEditText =(EditText) findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);

        logInButton = (Button) findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(this);

        signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("rew", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("rew", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE).edit();
        editor.putString(EMAIL, email);
        editor.putString(PASSWORD, password);
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        SharedPreferences preferences = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE);
        if (preferences.contains(EMAIL)){
            Log.i("rew", ",load previous email and password ");
            email = preferences.getString(EMAIL, "");
            password = preferences.getString(PASSWORD, "");

            emailEditText.setText(email);
            passwordEditText.setText(password);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.log_in_button:
                handleLogIn();
                break;
            case R.id.sign_up_button:
                handleSignUp();
                break;
        }
    }

    private void handleLogIn() {
        collectInputs();
        if (!CheckNetwork.isInternetAvailable(this)){
            showToast("Please check internet");
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("rew", "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.i("rew", "signInWithEmail:failed", task.getException());
                            showToast("Failed to Log in");
                        } else {
                            getStudentInformation();
                        }
                    }
                });
    }


    private void handleSignUp() {
        Intent signUp = new Intent(this, SignUpActivity.class);
        startActivity(signUp);
    }

    private void getStudentInformation() {
        isGetStudentLoaded = false;
        isGetAdminLoaded = false;
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        if (firebaseUser != null){
            final String uid = firebaseUser.getUid();
            DatabaseReference studentReference = firebaseDatabase.getReference(STUDENTS).child(uid);
            studentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Student student = dataSnapshot.getValue(Student.class);
                    SharedPreferences.Editor editor = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE).edit();
                    editor.putString(EMAIL, student.getEmail());
                    Log.i("rew", "student email = " + student.getEmail());
                    editor.putString(NAME, student.getName());
                    editor.commit();
                    isGetStudentLoaded = true;
                    logIn();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i("rew", "failed to get student info");
                }
            });
            DatabaseReference adminStudentsReference = firebaseDatabase.getReference(ADMIN_STUDENT).child(uid);
            adminStudentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SharedPreferences.Editor editor = getSharedPreferences(APP_PREFERENCE, MODE_PRIVATE).edit();
                    if (dataSnapshot.exists()){
                        editor.putBoolean(ADMIN, true);
                    } else {
                        editor.putBoolean(ADMIN, false);
                    }
                    isGetAdminLoaded = true;
                    editor.commit();
                    logIn();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i("rew", "failed to get admin info");
                }
            });
        }
    }



    private void logIn(){
        if (isGetStudentLoaded && isGetAdminLoaded) {
            Intent searchOrAdd = new Intent(this, SearchOrAddActivity.class);
            startActivity(searchOrAdd);
        }
    }

    private void collectInputs(){
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
