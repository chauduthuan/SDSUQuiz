package com.example.thuanpc.sdsupracticequestions.Fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.thuanpc.sdsupracticequestions.MainActivity;
import com.example.thuanpc.sdsupracticequestions.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment
        implements View.OnClickListener {

    Button addCourseButton;
    Button addQuestionButton;

    boolean isAdmin;
    public AddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_add, container, false);

        addCourseButton = (Button) view.findViewById(R.id.add_course_button);
        addCourseButton.setOnClickListener(this);

        addQuestionButton = (Button) view.findViewById(R.id.add_question_button);
        addQuestionButton.setOnClickListener(this);

        SharedPreferences preferences = getActivity().getSharedPreferences(MainActivity.APP_PREFERENCE, MODE_PRIVATE);
        isAdmin = preferences.getBoolean(MainActivity.ADMIN, false);
        if (!isAdmin){
            addCourseButton.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_course_button:
                handleAddSubjectButton();
                break;

            case R.id.add_question_button:
                handleAddQuestionButton();
                break;
        }
    }

    private void handleAddSubjectButton() {
        Log.i("rew", "add course clicked");
        AddListener listener = (AddListener) getActivity();
        listener.addCourse();

    }

    private void handleAddQuestionButton() {
        Log.i("rew", "add question clicked");
        AddListener listener = (AddListener) getActivity();
        listener.addQuestion();
    }

    public interface AddListener{
        public void addCourse();
        public void addQuestion();
    }
}
