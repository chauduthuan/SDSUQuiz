package com.example.thuanpc.sdsupracticequestions;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class PracticeActivity extends AppCompatActivity
    implements View.OnClickListener{

    TextView questionTitleTextView;
    TextView questionTextTextView;
    LinearLayout questionMultipleChoiceLinearLayout;
    EditText questionAnswerEditText;
    Button doneButton;

    String questionTitle;
    String questionText;
    String questionType;
    String questionCorrectAnswer;
    String questionExplanation;
    HashMap<String, String> questionAnswers;
    String answer;
    int questionPosition;
    String department;
    String course;
    String questionKey;

    ArrayList<String> choiceTextArrayList;
    ArrayList<RadioButton> choiceRadioButtonArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);
        setTitle("Practice");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initializeFields();
    }

    private void initializeFields() {
        questionTitleTextView = (TextView) findViewById(R.id.question_title_text_view);
        questionTextTextView = (TextView) findViewById(R.id.question_text_text_view);
        questionMultipleChoiceLinearLayout = (LinearLayout) findViewById(R.id.question_multiple_choice_linear_layout);
        questionAnswerEditText = (EditText) findViewById(R.id.question_answer_edit_text);

        doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(this);

        questionTitle = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_TITLE, "");
        questionTitleTextView.setText(questionTitle);
        questionText = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_TEXT, "");
        questionTextTextView.setText(questionText);

        questionCorrectAnswer = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_CORRECT_ANSWER, "");
        questionExplanation = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_EXPLANATION,"");
        questionType = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_TYPE, "");
        department = getIntent().getExtras().getString(SearchOrAddActivity.DEPARTMENT, "");
        course = getIntent().getExtras().getString(SearchOrAddActivity.COURSE, "");
        questionPosition = getIntent().getExtras().getInt(SearchOrAddActivity.QUESTION_POSITION, 0);
        questionKey = getIntent().getExtras().getString(SearchOrAddActivity.QUESTION_KEY, "");
        if (isMultipleChoiceType()){
            questionAnswers = (HashMap<String, String>) getIntent().getSerializableExtra(SearchOrAddActivity.QUESTION_ANSWERS);
            displayMultipleChoice();
        } else if (isFillInType()){
            displayFillIn();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.practice_menu, menu);
        return true;
    }


    private void displayMultipleChoice() {
        questionAnswerEditText.setVisibility(View.INVISIBLE);
        choiceTextArrayList = new ArrayList<>();
        choiceRadioButtonArrayList = new ArrayList<>();
        for (int i = 0; i < questionAnswers.size(); i++){
            String choice = SearchOrAddActivity.CHOICE + " " + (new Integer(i)).toString();
            String choiceText = questionAnswers.get(choice);
            addChoiceToView(choiceText);
            Log.i("rew", questionAnswers.get(choice));
        }
    }

    private void displayFillIn() {
        questionMultipleChoiceLinearLayout.setVisibility(View.INVISIBLE);
    }

    private void addChoiceToView(String choiceText){
        LinearLayout choiceLinearLayout = new LinearLayout(this);
        TextView choiceTextView = new TextView(this);
        RadioButton choiceRadioButton = new RadioButton(this);

        choiceLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        choiceLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 3;
        choiceTextView.setLayoutParams(params);
        choiceTextView.setText(choiceText);

        choiceRadioButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        choiceRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkChoiceRadioButton(v);
            }
        });

        choiceLinearLayout.addView(choiceRadioButton);
        choiceLinearLayout.addView(choiceTextView);

        choiceTextArrayList.add(choiceText);
        choiceRadioButtonArrayList.add(choiceRadioButton);

        questionMultipleChoiceLinearLayout.addView(choiceLinearLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                handleHomeMenu();
                return true;
            case R.id.discussion_menu:
                handleDiscussionMenu();
                return true;
            case R.id.next_menu:
                handleNextMenu();
                return true;
            case R.id.previous_menu:
                handlePreviousMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isMultipleChoiceType(){
        return questionType.equals(getResources().getString(R.string.multiple_choice));
    }

    private boolean isFillInType(){
        return questionType.equals(getResources().getString(R.string.fill_in));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.done_button:
                handleDoneButton();
                break;
        }

    }

    private void handleDoneButton() {
        Log.i("rew", "Done button clicked");
        collectAnswer();
        if (isAnswerValid()){
            if(answer.equals(questionCorrectAnswer)){
                showToast("Congratulation! You are correct");
            } else {
                showToast("You are wrong.\n" + questionExplanation);
            }
        }
    }

    private void handleHomeMenu() {
        Intent searchOrAdd = new Intent(this, SearchOrAddActivity.class);
        setResult(RESULT_CANCELED, searchOrAdd);
        finish();
    }

    private void handlePreviousMenu() {
        if (questionPosition == 0){
            showToast("No more question");
            return;
        }
        Intent searchOrAdd = new Intent(this, SearchOrAddActivity.class);
        searchOrAdd.putExtra(SearchOrAddActivity.QUESTION_POSITION, questionPosition - 1);
        setResult(RESULT_OK, searchOrAdd);
        finish();
    }

    private void handleNextMenu() {
        if (SearchOrAddActivity.isLastQuestion(questionPosition)){
            showToast("No more questions");
            return;
        }
        Intent searchOrAdd = new Intent(this, SearchOrAddActivity.class);
        searchOrAdd.putExtra(SearchOrAddActivity.QUESTION_POSITION, questionPosition + 1);
        setResult(RESULT_OK, searchOrAdd);
        finish();
    }

    private void handleDiscussionMenu() {
        Log.i("rew", "Discussion menu clicked");
        Intent discussion = new Intent(this, DiscussionActivity.class);
        discussion.putExtra(SearchOrAddActivity.DEPARTMENT, department);
        discussion.putExtra(SearchOrAddActivity.COURSE, course);
        discussion.putExtra(SearchOrAddActivity.QUESTION_KEY, questionKey);
        discussion.putExtra(SearchOrAddActivity.QUESTION_TITLE, questionTitle);
        discussion.putExtra(SearchOrAddActivity.QUESTION_TEXT, questionText);
        startActivity(discussion);

    }

    private void collectAnswer() {
        if (isMultipleChoiceType()) {
            for (int i = 0; i < choiceRadioButtonArrayList.size(); i++){
                RadioButton choiceRadioButton = choiceRadioButtonArrayList.get(i);
                if (choiceRadioButton.isChecked()){
                    answer = choiceTextArrayList.get(i);
                }
            }
        } else if (isFillInType()) {
            answer = questionAnswerEditText.getText().toString();
        }
    }

    private boolean isAnswerValid(){
        if (answer == null || answer.equals("")){
            showToast("Please select or enter your answer");
            return false;
        }
        return true;
    }

    private void checkChoiceRadioButton(View v) {
        for (RadioButton choiceRadioButton : choiceRadioButtonArrayList){
            if (choiceRadioButton == v){
                choiceRadioButton.setChecked(true);
            } else {
                choiceRadioButton.setChecked(false);
            }
        }
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
