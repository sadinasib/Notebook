package com.android.notebook;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.android.notebook.data.NotebookContract.NotebookEntry;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = QuizActivity.class.getSimpleName();

    private TextView mQuestion;
    private TextView mInsufficientWords;
    private Button mButtonA;
    private Button mButtonB;
    private int mCount = 0;
    private Cursor mCursor;
    private String[] mWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.android.notebook.R.layout.activity_quiz);

        mQuestion = findViewById(com.android.notebook.R.id.textQuestion);
        mInsufficientWords = findViewById(com.android.notebook.R.id.textInsufficent);
        mButtonA = findViewById(com.android.notebook.R.id.buttonA);
        mButtonB = findViewById(com.android.notebook.R.id.buttonB);
        mCursor = getCursor();
        mCount = mCursor.getCount();
        startQuiz();
        mButtonA.setOnClickListener(this);
        mButtonB.setOnClickListener(this);
    }

    private void startQuiz() {
        Log.i(TAG, "startQuiz");
        if (mCount > 1) {
            mInsufficientWords.setVisibility(View.GONE);
            ArrayList<Integer> tableIndex = getTableIndex();
            mWords = getWords(tableIndex);

            mQuestion.setText(String.format("What is the translation of %s?", mWords[0]));
            int randomButtonAssign = ThreadLocalRandom.current().nextInt(0, 2);
            switch (randomButtonAssign) {
                case 0:
                    mButtonA.setText(mWords[1]);
                    mButtonB.setText(mWords[2]);
                    break;
                case 1:
                    mButtonA.setText(mWords[2]);
                    mButtonB.setText(mWords[1]);
            }
        } else {
            mQuestion.setVisibility(View.GONE);
            mButtonA.setVisibility(View.GONE);
            mButtonB.setVisibility(View.GONE);
            mInsufficientWords.setVisibility(View.VISIBLE);
        }
    }

    private String[] getWords(ArrayList<Integer> tableIndex) {
        Log.i(TAG, "getWord");
        String[] words = new String[3];

        int randomIndex = getRandomInt(tableIndex.size());
        int randomDummyIndex = getRandomInt(tableIndex.size());
        if (randomIndex == randomDummyIndex) {
            randomDummyIndex = getRandomInt(tableIndex.size());
        }

        Cursor cursorWord = getCursorById(tableIndex.get(randomIndex));
        if (cursorWord != null && cursorWord.moveToFirst()) {
            words[0] = cursorWord.getString(cursorWord.getColumnIndex(NotebookEntry.COLUMN_WORD));
            words[1] = cursorWord.getString(cursorWord.getColumnIndex(NotebookEntry.COLUMN_TRANSLATION));
        }

        Cursor cursorDummy = getCursorById(tableIndex.get(randomDummyIndex));
        if (cursorDummy != null && cursorDummy.moveToFirst()) {
            words[2] = cursorDummy.getString(cursorDummy.getColumnIndex(NotebookEntry.COLUMN_TRANSLATION));
        }

        return words;
    }

    private Cursor getCursorById(int id) {
        Log.i(TAG, "getCursorById: " + id);
        String[] projection = new String[]{
                NotebookEntry.COLUMN_WORD,
                NotebookEntry.COLUMN_TRANSLATION
        };
        String selection = NotebookEntry._ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return getContentResolver().query(
                NotebookEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null,
                null
        );
    }

    private int getRandomInt(int size) {
        Log.i(TAG, "getRandomInt: bound = " + size);
        return ThreadLocalRandom.current().nextInt(0, size);
    }

    private Cursor getCursor() {
        Log.i(TAG, "getCursor");
        String[] projection = new String[]{
                NotebookEntry._ID,
                NotebookEntry.COLUMN_WORD,
                NotebookEntry.COLUMN_TRANSLATION};
        return getContentResolver().query(
                NotebookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null,
                null
        );
    }

    private ArrayList<Integer> getTableIndex() {
        Log.i(TAG, "getTableIndex");
        ArrayList<Integer> tableIndex = new ArrayList<>();

        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                tableIndex.add(mCursor.getInt(mCursor.getColumnIndex(NotebookEntry._ID)));
            } while (mCursor.moveToNext());
        }
        return tableIndex;
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick: ");
        int id = view.getId();
        switch (id) {
            case com.android.notebook.R.id.buttonA:
                checkAnswer(mButtonA.getText());
                break;
            case com.android.notebook.R.id.buttonB:
                checkAnswer(mButtonB.getText());
                break;
        }
    }

    private void checkAnswer(CharSequence text) {
        Log.i(TAG, "checkAnswer: ");
        if (TextUtils.equals(text, mWords[1])) {
            Toast.makeText(this, com.android.notebook.R.string.quiz_activity_congrats_right_answer, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, com.android.notebook.R.string.quiz_activity_sorry_wrong_answer, Toast.LENGTH_SHORT).show();
        }
        startQuiz();
    }
}
