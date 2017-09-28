package com.example.thuanpc.sdsupracticequestions;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.thuanpc.sdsupracticequestions.API.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DiscussionActivity extends AppCompatActivity
    implements View.OnClickListener{

    final String FROM = "From: ";

    RecyclerView messageRecyclerView;
    TextView questionTitleTextView;
    TextView questionTextTextView;
    EditText messageEditText;
    Button sendButton;

    String course;
    String department;
    String questionKey;
    String questionTitle;
    String questionText;
    String name;
    String email;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference discussionReference;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);
        setTitle("Discussion");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initializeFields();

    }

    private void initializeFields() {
        department = getIntent().getExtras().getString(SearchOrAddActivity.DEPARTMENT, "");
        course = getIntent().getExtras().getString(SearchOrAddActivity.COURSE, "");
        questionTitle = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_TITLE, "");
        questionText = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_TEXT, "");
        questionKey = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_KEY, "");

        SharedPreferences preferences = getSharedPreferences(MainActivity.APP_PREFERENCE, MODE_PRIVATE);
        name = preferences.getString(MainActivity.NAME, "");
        email = preferences.getString(MainActivity.EMAIL, "");

        messageAdapter = new MessageAdapter();
        messageRecyclerView = (RecyclerView) findViewById(R.id.message_recycler_view);
        messageRecyclerView.setLayoutManager((new LinearLayoutManager(this)));
        messageRecyclerView.setAdapter(messageAdapter);

        questionTitleTextView = (TextView) findViewById(R.id.question_title_text_view);
        questionTitleTextView.setText(questionTitle);

        questionTextTextView = (TextView) findViewById(R.id.question_text_text_view);
        questionTextTextView.setText(questionText);

        messageEditText = (EditText) findViewById(R.id.message_edit_text);
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
//        firebaseDatabase.setPersistenceEnabled(true);

        discussionReference = firebaseDatabase.getReference(SearchOrAddActivity.DISCUSSION).child(department).child(course).child(questionKey);
        discussionReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                displayMessage(message);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayMessage(Message message) {
        messageAdapter.addMessage(message);
        messageRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_button:
                handleSendButton();
                break;
        }
    }

    private void handleSendButton() {
        String text = messageEditText.getText().toString();
        if (text.equals("")) {return;}
        long createdAt = System.currentTimeMillis();
        Message message = new Message(text, email, name, createdAt);
        String messageKey = discussionReference.push().getKey();
        discussionReference.child(messageKey).setValue(message);
        messageEditText.setText("");
    }

    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder>{
        ArrayList<Message> messageArrayList;

        public MessageAdapter(){
            messageArrayList = new ArrayList<>();
        }

        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.custom_row_message_list,parent,false);
            return new MessageHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageHolder holder, int position) {
            Message message = messageArrayList.get(position);
            holder.bindMessageToViewHolder(message);
        }

        @Override
        public int getItemCount() {
            return messageArrayList.size();
        }

        public void addMessage(Message message){
            messageArrayList.add(message);
            notifyDataSetChanged();
        }

        public void clear(){
            messageArrayList.clear();
            notifyDataSetChanged();
        }
    }

    private class MessageHolder extends RecyclerView.ViewHolder{
        TextView messageTextTextView;
        TextView nameTextView;

        String name;
        String text;

        public MessageHolder(View itemView) {
            super(itemView);
            messageTextTextView = (TextView) itemView.findViewById(R.id.message_text_text_view);
            nameTextView = (TextView) itemView.findViewById(R.id.name_text_view);
        }

        public void bindMessageToViewHolder(Message message){
            name = FROM + message.getName();
            text = message.getText();
            messageTextTextView.setText(text);
            nameTextView.setText(name);
        }
    }

}
