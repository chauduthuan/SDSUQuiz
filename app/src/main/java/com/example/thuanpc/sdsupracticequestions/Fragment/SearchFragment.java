package com.example.thuanpc.sdsupracticequestions.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.thuanpc.sdsupracticequestions.API.Question;
import com.example.thuanpc.sdsupracticequestions.DiscussionActivity;
import com.example.thuanpc.sdsupracticequestions.PracticeActivity;
import com.example.thuanpc.sdsupracticequestions.R;
import com.example.thuanpc.sdsupracticequestions.SearchOrAddActivity;


import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    RecyclerView questionRecyclerView;
    QuestionAdapter questionAdapter;

    String department;
    String course;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        questionRecyclerView = (RecyclerView) view.findViewById(R.id.question_recycler_view);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (questionAdapter == null) {
            questionAdapter = new QuestionAdapter();
        }
        questionRecyclerView.setAdapter(questionAdapter);

        return view;
    }

    public void addQuestion(String questionKey, Question question){
        questionAdapter.addQuestion(questionKey, question);
    }

    public void clear(){
        questionAdapter.clear();
    }

    public void setCourse(String course){
        this.course = course;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public void selectQuestionToPractice(int position){
        if (hasQuestionPosition(position)){
            Log.i("rew", "select question to practice");
            View view = questionRecyclerView.getChildAt(position);
            Button practiceButton = (Button) view.findViewById(R.id.practice_button);
            practiceButton.performClick();
        }
    }

    public boolean hasQuestionPosition(int position){
        Log.i("rew", "" + position + " vs " + questionAdapter.getItemCount());
        return position >= 0 && position < questionAdapter.getItemCount();
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionHolder>{
        ArrayList<Question> questionArrayList;
        ArrayList<String> questionKeyArrayList;
        public QuestionAdapter(){
            questionArrayList = new ArrayList<>();
            questionKeyArrayList = new ArrayList<>();
        }

        @Override
        public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.custom_row_question_list,parent,false);
            return new QuestionHolder(view);
        }

        @Override
        public void onBindViewHolder(QuestionHolder holder, int position) {
            Question question= questionArrayList.get(position);
            String questionKey = questionKeyArrayList.get(position);
            holder.bindQuestionToViewHolder(questionKey, question, position);
        }

        @Override
        public int getItemCount() {
            return questionArrayList.size();
        }

        public void addQuestion(String questionKey, Question question){
            questionArrayList.add(question);
            questionKeyArrayList.add(questionKey);
            notifyDataSetChanged();
        }

        public void clear(){
            questionArrayList.clear();
            questionKeyArrayList.clear();
            notifyDataSetChanged();
        }
    }

    private class QuestionHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView questionTitleTextView;
        TextView questionTypeTextView;
        Button practiceButton;
        Button discussionButton;

        String questionKey;
        Question question;
        int questionPosition;

        public QuestionHolder(View itemView) {
            super(itemView);
            questionTitleTextView = (TextView) itemView.findViewById(R.id.question_title_text_view);
            questionTypeTextView = (TextView) itemView.findViewById(R.id.question_type_text_view);

            practiceButton = (Button) itemView.findViewById(R.id.practice_button);
            practiceButton.setOnClickListener(this);

            discussionButton = (Button) itemView.findViewById(R.id.discussion_button);
            discussionButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.practice_button:
                    handlePracticeButton();
                    break;
                case R.id.discussion_button:
                    handleDiscussionButton();
                    break;
            }
        }

        private void handlePracticeButton() {
            Log.i("rew", "Practice button clicked");
            Intent practice = new Intent(getActivity(), PracticeActivity.class);
            practice.putExtra(SearchOrAddActivity.QUESTION_TITLE, question.getTitle());
            practice.putExtra(SearchOrAddActivity.QUESTION_TEXT, question.getText());
            practice.putExtra(SearchOrAddActivity.QUESTION_TYPE, question.getType());
            practice.putExtra(SearchOrAddActivity.QUESTION_CORRECT_ANSWER, question.getCorrectAnswer());
            practice.putExtra(SearchOrAddActivity.QUESTION_EXPLANATION, question.getExplanation());
            if (isMultipleChoice()){
                practice.putExtra(SearchOrAddActivity.QUESTION_ANSWERS, question.getAnswers());
            }
            practice.putExtra(SearchOrAddActivity.QUESTION_POSITION, questionPosition);
            practice.putExtra(SearchOrAddActivity.DEPARTMENT, department);
            practice.putExtra(SearchOrAddActivity.COURSE, course);
            practice.putExtra(SearchOrAddActivity.QUESTION_KEY, questionKey);
            getActivity().startActivityForResult(practice, SearchOrAddActivity.PRACTICE_REQUEST_CODE);
        }

        private void handleDiscussionButton() {
            Log.i("rew", "Discussion button clicked");
            Intent discussion = new Intent(getActivity(), DiscussionActivity.class);
            discussion.putExtra(SearchOrAddActivity.DEPARTMENT, department);
            discussion.putExtra(SearchOrAddActivity.COURSE, course);
            discussion.putExtra(SearchOrAddActivity.QUESTION_KEY, questionKey);
            discussion.putExtra(SearchOrAddActivity.QUESTION_TITLE, question.getTitle());
            discussion.putExtra(SearchOrAddActivity.QUESTION_TEXT, question.getText());
            startActivity(discussion);
        }

        public void bindQuestionToViewHolder(String questionKey, Question question, int position){
            this.questionKey = questionKey;
            this.question = question;
            this.questionPosition = position;
            questionTitleTextView.setText(question.getTitle());
            questionTypeTextView.setText(question.getType());

        }

        private boolean isMultipleChoice(){
            return question.getType().equals(getResources().getString(R.string.multiple_choice));
        }

        private boolean isFillInChoice(){
            return question.getType().equals(getResources().getString(R.string.fill_in));
        }
    }


}
