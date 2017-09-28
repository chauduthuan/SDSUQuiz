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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddCourseActivity extends AppCompatActivity
    implements AdapterView.OnItemSelectedListener, View.OnClickListener{

    Spinner departmentSpinner;
    Spinner semesterSpinner;
    EditText yearEditText;
    EditText courseNumberEditText;
    EditText courseTitleEditText;
    Button addButton;

    String department = "";
    String semester = "";
    String year = "";
    String courseNumber = "";
    String courseTitle = "";
    ArrayList<String> departmentAbbreviationArrayList;
    ArrayList<String> departmentArrayList;
    ArrayList<String> semesterArrayList;
    FirebaseDatabase firebaseDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        setTitle("Add Course");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initializeFields();
    }

    private void initializeFields() {
        departmentSpinner = (Spinner) findViewById(R.id.department_spinner);
        departmentSpinner.setOnItemSelectedListener(this);
        semesterSpinner = (Spinner) findViewById(R.id.semester_spinner);
        semesterSpinner.setOnItemSelectedListener(this);

        yearEditText = (EditText) findViewById(R.id.year_edit_text);
        courseTitleEditText = (EditText) findViewById(R.id.course_title_edit_text);
        courseNumberEditText = (EditText) findViewById(R.id.course_number_edit_text);

        addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        departmentAbbreviationArrayList = new ArrayList<>();
        departmentArrayList = new ArrayList<>();
        semesterArrayList = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();

        loadDepartmentList();
        loadSemesterList();
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.department_spinner:
                handleDepartmentSpinnerItemSelected(position);
                break;
            case R.id.semester_spinner:
                handleSemesterSpinnerItemSelected(position);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_button:
                handleAddCourseButton();
                break;
        }
    }

    private void handleDepartmentSpinnerItemSelected(int position) {
        department = departmentAbbreviationArrayList.get(position);
    }

    private void handleSemesterSpinnerItemSelected(int position) {
        semester = semesterArrayList.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void handleAddCourseButton() {
        collectInputs();
        if (!CheckNetwork.isInternetAvailable(this)){
            showToast("Cannot add course. Please check internet");
            return;
        }
        if (isInputValid()){
            String course = semester + "-" + year + "-" + department + courseNumber + "-" + courseTitle;
            Log.i("rew", "Course = " + course);
            DatabaseReference courseReference = firebaseDatabase.getReference(SearchOrAddActivity.COURSES).child(department);
            courseReference.child(course).setValue("");
            finish();
        }
    }

    private void collectInputs() {
        year = yearEditText.getText().toString();
        courseTitle = courseTitleEditText.getText().toString();
        courseNumber = courseNumberEditText.getText().toString();
    }

    private boolean isInputValid(){
        boolean isValid = true;
        if (courseTitle.equals("")) {
            isValid = false;
            showToast("Course title must not empty");
        }
        try {
            int yearInt = Integer.parseInt(year);
            if (yearInt < 1000){
                isValid = false;
                showToast("Invalid year");
            }
        } catch (Exception e){
            isValid = false;
            showToast("Invalid year");
        }
        try {
            int courseNumberInt = Integer.parseInt(courseNumber);
            if (courseNumberInt < 100 || courseNumberInt >= 1000){
                isValid = false;
                showToast("Invalid Course number");
            }
        } catch (Exception e){
            isValid = false;
            showToast("Invalid Course Number");
        }
        return isValid;
    }

    private void loadDepartmentList() {
        final DatabaseReference departmentReference = firebaseDatabase.getReference(SearchOrAddActivity.DEPARTMENTS);
        Log.i("rew", "load department " + departmentReference.toString());
        departmentReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void loadSemesterList() {
        final DatabaseReference departmentReference = firebaseDatabase.getReference(SearchOrAddActivity.SEMESTERS);
        Log.i("rew", "load semester " + departmentReference.toString());
        departmentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int size = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    size++;
                }
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    semesterArrayList.add(key);
                    count++;
                    if (count == size){
                        setSpinner(semesterSpinner, semesterArrayList.toArray(new String[semesterArrayList.size()]));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setSpinner(Spinner spinner, String[] values){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
