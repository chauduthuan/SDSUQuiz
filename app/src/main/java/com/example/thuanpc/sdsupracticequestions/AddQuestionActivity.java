package com.example.thuanpc.sdsupracticequestions;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thuanpc.sdsupracticequestions.API.Question;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class AddQuestionActivity extends AppCompatActivity
    implements AdapterView.OnItemSelectedListener, View.OnClickListener{

    Spinner questionTypeSpinner;
    LinearLayout multipleChoicesLinearLayout;
    TextView departmentTextView;
    TextView courseTextView;
    EditText questionTitleEditText;
    EditText questionTextEditText;
    EditText correctAnswerEditText;
    EditText explanationEditText;
    Button addButton;
    Button addChoiceButton;
    Button clearChoicesButton;

    ArrayList<EditText> choiceEditTextArrayList;
    ArrayList<RadioButton> choiceRadioButtonArrayList;
    ArrayList<Button> choiceDeleteButtonArrayList;
    String department;
    String course;
    String departmentFullName;
    String[] questionTypes;
    String questionTitle;
    String questionText;
    String questionType;
    HashMap<String,String> answers;
    String correctAnswer;
    String explanation;
    long createdAt;

    FirebaseDatabase firebaseDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        setTitle("Add Question");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initializeFields();
    }

    private void initializeFields() {
        department = getIntent().getExtras().getString(SearchOrAddActivity.DEPARTMENT);
        departmentFullName = getIntent().getExtras().getString(SearchOrAddActivity.DEPARTMENT_FULL_NAME);
        course = getIntent().getExtras().getString(SearchOrAddActivity.COURSE);

        departmentTextView = (TextView) findViewById(R.id.department_text_view);
        departmentTextView.setText(departmentFullName);
        courseTextView = (TextView) findViewById(R.id.course_text_view);
        courseTextView.setText(course);

        questionTitleEditText = (EditText) findViewById(R.id.question_title_edit_text);
        questionTextEditText = (EditText) findViewById(R.id.question_text_edit_text) ;
        correctAnswerEditText = (EditText) findViewById(R.id.correct_answer_edit_text);
        explanationEditText = (EditText) findViewById(R.id.explanation_edit_text);

        addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(this);
        addChoiceButton = (Button) findViewById(R.id.add_choice_button);
        addChoiceButton.setOnClickListener(this);
        clearChoicesButton = (Button) findViewById(R.id.clear_choices_button);
        clearChoicesButton.setOnClickListener(this);

        questionTypeSpinner = (Spinner) findViewById(R.id.question_type_spinner);
        questionTypes = getResources().getStringArray(R.array.question_types);
        ArrayAdapter<String> questionTypeArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, questionTypes);
        questionTypeSpinner.setAdapter(questionTypeArrayAdapter);
        questionTypeSpinner.setOnItemSelectedListener(this);

        choiceEditTextArrayList = new ArrayList<>();
        choiceRadioButtonArrayList = new ArrayList<>();
        choiceDeleteButtonArrayList = new ArrayList<>();
        answers = new HashMap<>();
        multipleChoicesLinearLayout = (LinearLayout) findViewById(R.id.multiple_choices_linear_layout);

        firebaseDatabase = FirebaseDatabase.getInstance();
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
            case R.id.add_button:
                handleAddButton();
                break;
            case R.id.add_choice_button:
                handleAddChoiceButton();
                break;
            case R.id.clear_choices_button:
                handleClearChoicesButton();
                break;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.question_type_spinner:
                handleQuestionTypeItemSelected(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void handleQuestionTypeItemSelected(int position) {
        questionType = questionTypes[position];
        Log.i("rew", questionType + " is selected");
        if (isMultipleChoiceType()){
            handleMultipleChoiceType();
        } else if (isFillInType()){
            handleFillInType();
        }
    }

    private void handleFillInType() {
        showFillInFields();
        hideMultipleChoiceFields();
    }

    private void handleMultipleChoiceType() {
        hideFillInFields();
        showMultipleChoiceFields();
    }

    private void handleAddButton() {
        Log.i("rew", "add button clicked");
        collectDataForQuestion();
        if (!CheckNetwork.isInternetAvailable(this)){
            showToast("Cannot add question. Please check internet");
            return;
        }
        if (isMultipleChoiceType()){
            uploadMultipleChoiceQuestion();
        } else if (isFillInType()){
            uploadFillInQuestion();
        }
    }

    private void collectDataForQuestion() {
        questionTitle = questionTitleEditText.getText().toString();
        questionText = questionTextEditText.getText().toString();
        explanation = explanationEditText.getText().toString();
        createdAt = System.currentTimeMillis();
        answers.clear();
        if (isMultipleChoiceType()){
            for (int i = 0; i < choiceEditTextArrayList.size(); i ++){
                EditText choiceEditText = choiceEditTextArrayList.get(i);
                String key = SearchOrAddActivity.CHOICE + " " + (new Integer(i)).toString();
                String value = choiceEditText.getText().toString();
                if (value.equals("")){
                    showToast("Empty filed of multiple choice");
                    return;
                }
                answers.put(key, value);
            }
            correctAnswer = null;
            for (int i = 0; i < choiceRadioButtonArrayList.size(); i++){
                RadioButton choiceRadioButton = choiceRadioButtonArrayList.get(i);
                if (choiceRadioButton.isChecked()){
                    correctAnswer = choiceEditTextArrayList.get(i).getText().toString();
                }
            }
        } else if (isFillInType()){
            correctAnswer = correctAnswerEditText.getText().toString();
        }
    }

    private void uploadMultipleChoiceQuestion() {
        if (choiceEditTextArrayList.size() == 0){
            showToast("Please add multiple choice answer");
            return;
        }
        if (correctAnswer == null || correctAnswer.equals("")){
            showToast("Please select correct choice");
            return;
        }
        Question question = generateQuestion();
        uploadQuestion(question);
    }

    private void uploadFillInQuestion() {
        if (correctAnswer.equals("")){
            showToast("Please enter correct answer");
            return;
        }
        Question question = generateQuestion();
        uploadQuestion(question);
    }

    private Question generateQuestion(){
        Question question = new Question();
        question.setTitle(questionTitle);
        question.setText(questionText);
        question.setType(questionType);
        question.setAnswers(answers);
        question.setCorrectAnswer(correctAnswer);
        question.setExplanation(explanation);
        question.setCreatedAt(createdAt);
        return question;
    }

    private void uploadQuestion(Question question){
        DatabaseReference questionReference = firebaseDatabase.getReference(SearchOrAddActivity.QUESTIONS).child(department).child(course);
        String key = questionReference.push().getKey();
        questionReference.child(key).setValue(question);
        finish();
    }

    private void handleAddChoiceButton() {
        Log.i("rew", "add choice clicked");
        addChoiceToView();
    }

    private void handleClearChoicesButton() {
        Log.i("rew", "clear choice clicked");
        clearChoices();
    }

    private void clearChoices(){
        choiceRadioButtonArrayList.clear();
        choiceEditTextArrayList.clear();
        choiceDeleteButtonArrayList.clear();
        multipleChoicesLinearLayout.removeAllViews();
    }

    private void addChoiceToView(){
        LinearLayout choiceLinearLayout = new LinearLayout(this);
        EditText choiceEditText = new EditText(this);
        RadioButton choiceRadioButton = new RadioButton(this);
        Button choiceDeleteButton = new Button(this);

        choiceLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        choiceLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 3;
        choiceEditText.setLayoutParams(params);
        choiceEditText.setHint(getResources().getString(R.string.enter_answer));

        choiceRadioButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        choiceRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkChoiceRadioButton(v);
            }
        });

        choiceDeleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        choiceDeleteButton.setText(getResources().getString(R.string.delete));
        choiceDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteChoice(v);
            }
        });

        choiceLinearLayout.addView(choiceRadioButton);
        choiceLinearLayout.addView(choiceEditText);
        choiceLinearLayout.addView(choiceDeleteButton);

        choiceEditTextArrayList.add(choiceEditText);
        choiceRadioButtonArrayList.add(choiceRadioButton);
        choiceDeleteButtonArrayList.add(choiceDeleteButton);

        multipleChoicesLinearLayout.addView(choiceLinearLayout);
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

    private void deleteChoice(View v) {
        int position = 0;
        for (Button choiceDeleteButton : choiceDeleteButtonArrayList){
            if (choiceDeleteButton == v){
                break;
            }
            position++;
        }

        Button choiceDeleteButton = choiceDeleteButtonArrayList.get(position);
        LinearLayout choiceLinearLayout = (LinearLayout) choiceDeleteButton.getParent();
        multipleChoicesLinearLayout.removeView(choiceLinearLayout);
        choiceDeleteButtonArrayList.remove(position);
        choiceEditTextArrayList.remove(position);
        choiceRadioButtonArrayList.remove(position);
    }


    private void hideMultipleChoiceFields(){
        addChoiceButton.setVisibility(View.INVISIBLE);
        clearChoicesButton.setVisibility(View.INVISIBLE);
        multipleChoicesLinearLayout.setVisibility(View.INVISIBLE);
    }

    private void hideFillInFields(){
        correctAnswerEditText.setVisibility(View.INVISIBLE);
    }

    private void showMultipleChoiceFields(){
        addChoiceButton.setVisibility(View.VISIBLE);
        clearChoicesButton.setVisibility(View.VISIBLE);
        multipleChoicesLinearLayout.setVisibility(View.VISIBLE);
    }

    private void showFillInFields(){
        correctAnswerEditText.setVisibility(View.VISIBLE);
    }

    private boolean isMultipleChoiceType(){
        return questionType.equals(getResources().getString(R.string.multiple_choice));
    }

    private boolean isFillInType(){
        return questionType.equals(getResources().getString(R.string.fill_in));
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
