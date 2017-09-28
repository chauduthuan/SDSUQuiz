package com.example.thuanpc.sdsupracticequestions;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.thuanpc.sdsupracticequestions.API.Question;
import com.example.thuanpc.sdsupracticequestions.Fragment.AddFragment;
import com.example.thuanpc.sdsupracticequestions.Fragment.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchOrAddActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
            AdapterView.OnItemSelectedListener, View.OnClickListener,
            AddFragment.AddListener{

    public static final String DEPARTMENTS = "Departments";
    public static final String DEPARTMENT = "department";
    public static final String DEPARTMENT_FULL_NAME = "department full name";
    public static final String DISCUSSION = "Discussion";
    public static final String SEMESTERS = "Semesters";
    public static final String COURSES = "Courses";
    public static final String COURSE = "course";
    public static final String QUESTIONS = "Questions";
    public static final String CHOICE = "Choice";
    public static final String QUESTION_KEY = "Question key";
    public static final String QUESTION_TITLE = "Question title";
    public static final String QUESTION_TEXT = "Question text";
    public static final String QUESTION_TYPE = "Question type";
    public static final String QUESTION_ANSWERS = "Question answer";
    public static final String QUESTION_CORRECT_ANSWER = "Question correct answer";
    public static final String QUESTION_EXPLANATION = "Question explanation";
    public static final String QUESTION_POSITION = "Question position";
    public static final int PRACTICE_REQUEST_CODE = 0;

    final int NO_CONTENT = 0;
    final int SEARCH_CONTENT = 1;
    final int ADD_CONTENT = 2;
    final int TIME_INTERVAL = 3000;

    AddFragment addFragment;
    SearchFragment searchFragment;
    BottomNavigationView bottomNavigationView;
    Spinner departmentSpinner;
    Spinner courseSpinner;
    Button showQuestionsButton;
    Button reloadDepartmentsButton;
    Button reloadCoursesButton;

    static long questionCount;
    int currentContent;
    String department = "";
    String departmentFullName = "";
    String course = "";
    ArrayList<Question> questionArrayList;
    ArrayList<String> questionKeyArrayList;
    ArrayList<String> departmentAbbreviationArrayList;
    ArrayList<String> departmentArrayList;
    ArrayList<String> courseArrayList;
    FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_or_add);
        setTitle("Practice Question");
        initializeFields();
    }

    private void initializeFields() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        departmentSpinner = (Spinner) findViewById(R.id.department_spinner);
        departmentSpinner.setOnItemSelectedListener(this);
        courseSpinner = (Spinner) findViewById(R.id.course_spinner);
        courseSpinner.setOnItemSelectedListener(this);

        currentContent = NO_CONTENT;
        questionArrayList = new ArrayList<>();
        questionKeyArrayList = new ArrayList<>();
        departmentAbbreviationArrayList = new ArrayList<>();
        departmentArrayList = new ArrayList<>();
        departmentArrayList = new ArrayList<>();
        courseArrayList = new ArrayList<>();

        showQuestionsButton = (Button) findViewById(R.id.show_questions_button);
        showQuestionsButton.setOnClickListener(this);
        reloadDepartmentsButton = (Button) findViewById(R.id.reload_department_button);
        reloadDepartmentsButton.setOnClickListener(this);
        reloadCoursesButton = (Button) findViewById(R.id.reload_course_button);
        reloadCoursesButton.setOnClickListener(this);

        firebaseDatabase = FirebaseDatabase.getInstance();

        loadDepartmentList();
        loadSearchFragment();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_of_add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_log_out:
                logOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.navigation_search:
                loadSearchFragment();
                return true;
            case R.id.navigation_add:
                loadAddFragment();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.show_questions_button:
                handleShowQuestionsButton();
                break;
            case R.id.reload_department_button:
                loadDepartmentList();
                break;
            case R.id.reload_course_button:
                loadCourseList();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.department_spinner:
                handleDepartmentSpinnerItemSelected(position);
                break;
            case R.id.course_spinner:
                handleCourseSpinnerItemSelected(position);
                break;
        }
    }


    private void handleDepartmentSpinnerItemSelected(int position) {
        if (!department.equals(departmentAbbreviationArrayList.get(position))){
            department = departmentAbbreviationArrayList.get(position);
            departmentFullName = departmentArrayList.get(position);
            loadCourseList();
        }

        Log.i("rew", department + " is selected");

    }

    private void handleCourseSpinnerItemSelected(int position) {
        course = courseArrayList.get(position);
        Log.i("rew", course + " is selected");
    }

    private void handleShowQuestionsButton() {
        Log.i("rew", "search button clicked");
        if (isSearchValid()){
            loadQuestionList();
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void loadSearchFragment() {
//        setTitle("Search");
        showQuestionsButton.setVisibility(View.VISIBLE);
        if (currentContent == SEARCH_CONTENT) {return;}
        currentContent = SEARCH_CONTENT;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (searchFragment == null){
            searchFragment = new SearchFragment();
        }
        fragmentTransaction.replace(R.id.content, searchFragment);
        fragmentTransaction.commit();
        Log.i("rew","load Search Fragment");
    }

    private void loadAddFragment(){
//        setTitle("Add");
        showQuestionsButton.setVisibility(View.INVISIBLE);
        if (currentContent == ADD_CONTENT) {return;}
        currentContent = ADD_CONTENT;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (addFragment == null){
            addFragment = new AddFragment();
        }
        fragmentTransaction.replace(R.id.content, addFragment);
        fragmentTransaction.commit();
        Log.i("rew","load Add Fragment");
    }

    private void logOut() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        finish();
    }

    private void loadDepartmentList(){
        departmentArrayList.clear();
        departmentAbbreviationArrayList.clear();
        setSpinner(departmentSpinner, new String[0]);
        department = "";

        if (!CheckNetwork.isInternetAvailable(getApplicationContext())){
            showToast("Cannot load department list. Please check internet");
            return;
        }
        DatabaseReference departmentReference = firebaseDatabase.getReference(DEPARTMENTS);
        Log.i("rew", "load department " + departmentReference.toString());
        departmentReference.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int size = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    size++;
                }
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String value = snapshot.getValue(String.class);
                    departmentAbbreviationArrayList.add(key);
                    departmentArrayList.add(key + " : " + value);

                    count++;
                    if (count == size){
                        setSpinner(departmentSpinner, departmentArrayList.toArray(new String[departmentArrayList.size()]));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadCourseList(){
        courseArrayList.clear();
        setSpinner(courseSpinner, new String[0]);
        course = "";
        if (!CheckNetwork.isInternetAvailable(getApplicationContext())){
            showToast("Cannot load course list. Please check internet");
            return;
        }
        final DatabaseReference courseReference = firebaseDatabase.getReference(COURSES).child(department);
        Log.i("rew", "load Course List ");
        courseReference.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long size = dataSnapshot.getChildrenCount();
                if (size == 0){
                    showToast("There is no course");
                }
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    courseArrayList.add(key);

                    count++;
                    if (count == size){
                        setSpinner(courseSpinner, courseArrayList.toArray(new String[courseArrayList.size()]));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadQuestionList(){
        if (!CheckNetwork.isInternetAvailable(getApplicationContext())){
            showToast("Cannot load questions. Please check internet");
            return;
        }
        DatabaseReference questionReference = firebaseDatabase.getReference(QUESTIONS).child(department).child(course);
        Log.i("rew", "load question list " + questionReference.toString());
        questionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long size = dataSnapshot.getChildrenCount();
                if (size == 0){
                    showToast("There is no question");
                }

                searchFragment.clear();
                searchFragment.setCourse(course);
                searchFragment.setDepartment(department);
                questionArrayList.clear();
                questionKeyArrayList.clear();

                questionCount = dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String questionKey = snapshot.getKey();
                    questionKeyArrayList.add(questionKey);
                    Question question = snapshot.getValue(Question.class);
                    questionArrayList.add(question);

                    searchFragment.addQuestion(questionKey, question);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (!CheckNetwork.isInternetAvailable(getApplicationContext())){
                    showToast("Please check internet");
                    return;
                }
            }
        });
    }

    private void setSpinner(Spinner spinner, String[] values){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void addCourse() {
        Log.i("rew", "start add course activity");
        Intent addCourse = new Intent(this, AddCourseActivity.class);
        startActivity(addCourse);
    }

    @Override
    public void addQuestion() {
        if (department.equals("")){
            showToast("No department is selected");
            return;
        }
        if (course.equals("")){
            showToast("No course is selected");
            return;
        }
        Log.i("rew", "start add question activity");
        Intent addQuestion = new Intent(this, AddQuestionActivity.class);
        addQuestion.putExtra(DEPARTMENT, department);
        addQuestion.putExtra(DEPARTMENT_FULL_NAME, departmentFullName);
        addQuestion.putExtra(COURSE, course);
        startActivity(addQuestion);
    }

    private boolean isSearchValid(){
        boolean isValid = true;
        if (department == null || department.equals("")){
            isValid = false;
            showToast("Please select department");
        }
        if (course == null || course.equals("")){
            isValid = false;
            showToast("Please select course");
        }

        return isValid;
    }


    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("rew", "search or add onActivityResult");
        if (requestCode == PRACTICE_REQUEST_CODE && resultCode == RESULT_OK){
            int questionPosition = data.getExtras().getInt(QUESTION_POSITION, 0);
            searchFragment.selectQuestionToPractice(questionPosition);
        }
    }

    public static boolean isLastQuestion(int position){
        return position == questionCount - 1;
    }

    public class ReloadDepertmentList implements Runnable{
        @Override
        public void run() {
            loadDepartmentList();
        }
    }

    public class ReloadCourseList implements Runnable{
        @Override
        public void run() {
            loadCourseList();
        }
    }
}
