package com.example.thuanpc.sdsupracticequestions;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity
    implements View.OnClickListener{

    EditText emailEditText;
    EditText passwordEditText;
    EditText nameEditText;
    EditText redidEditText;
    Button signUpButton;

    String email;
    String password;
    String name;
    String redid;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initializeFields();

    }

    private void initializeFields() {
        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        redidEditText = (EditText) findViewById(R.id.redid_edit_text);

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
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
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
            case R.id.sign_up_button:
                handleSignUp();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSignUp() {
        collectInput();
        if (!CheckNetwork.isInternetAvailable(this)){
            showToast("Please check internet");
            return;
        }
        if (isInputValid()){
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("rew", "createUserWithEmail:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Log.i("rew","Failed to Sign up new user to firebase " + task.getException().toString());
                                showToast("Failed to Sign up new user");
                            } else {
                                createStudentrOnFirebase();
                            }
                        }
                    });
        }
    }

    private void createStudentrOnFirebase() {
        long createdAt = System.currentTimeMillis();
        Student student = new Student(email, name, redid, createdAt);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference students = database.getReference(MainActivity.STUDENTS);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            students.child(uid).setValue(student);
            Log.i("rew", "fire base user added");
            goBackToLogInScreen();
        }


    }

    private void collectInput() {
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        name = nameEditText.getText().toString();
        redid = redidEditText.getText().toString();
        Log.i("rew", email + "  " + name);
    }

    private boolean isInputValid(){
        boolean isValid = true;
        if (email.equals("")){
            showToast("Email is empty");
            isValid = false;
        }
        if (password.length() < 6){
            showToast("Password must have 6 or more characters");
            isValid = false;
        }
        if (name.equals("")){
            showToast("name is empty");
            isValid = false;
        }
        if (redid.equals("")){
            showToast("RedID is empty");
            isValid = false;
        }
        return isValid;
    }

    private void goBackToLogInScreen() {
//        Intent logInActivity = new Intent(this, MainActivity.class);
//        logInActivity.putExtra(MainActivity.EMAIL, email);
//        logInActivity.putExtra(MainActivity.PASSWORD, password);
//        setResult(RESULT_OK, logInActivity);
        SharedPreferences.Editor editor = getSharedPreferences(MainActivity.APP_PREFERENCE, MODE_PRIVATE).edit();
        editor.putString(MainActivity.EMAIL, email);
        editor.putString(MainActivity.PASSWORD, password);
        editor.putString(MainActivity.NAME, name);
        editor.commit();
        finish();
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
