package com.android.notebook.adapter;

import android.content.Context;
import android.database.Cursor;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.notebook.R;

import java.util.Locale;

import static com.android.notebook.data.NotebookContract.NotebookEntry;

public class NotebookAdapter
        extends CursorAdapter
        implements SectionIndexer {
    private static final String TAG = NotebookAdapter.class.getSimpleName();
    private TextToSpeech mTextToSpeech;
    private AlphabetIndexer mAlphabetIndexer;

    public NotebookAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_items, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final long row_id = cursor.getLong(cursor.getColumnIndex(NotebookEntry._ID));

        final TextView tvWord = (TextView) view.findViewById(R.id.list_items_text_word);
        final TextView tvTrans = (TextView) view.findViewById(R.id.list_items_text_trans);

        String word = cursor.getString(cursor.getColumnIndexOrThrow(NotebookEntry.COLUMN_WORD));
        final String trans = cursor.getString(cursor.getColumnIndexOrThrow(NotebookEntry.COLUMN_TRANSLATION));

        tvWord.setText(word);
        tvTrans.setText(trans);

        ImageButton buttonPlay = (ImageButton) view.findViewById(R.id.list_items_button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak(context, trans);
            }
        });
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor != null) {
            final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            mAlphabetIndexer = new AlphabetIndexer(newCursor, newCursor.getColumnIndex(NotebookEntry.COLUMN_WORD), alphabet);
            mAlphabetIndexer.setCursor(newCursor);
        }
        return super.swapCursor(newCursor);
    }

    @Override
    public int getCount() {
        if (getCursor() == null) {
            return 0;
        }
        return super.getCount();
    }

    /**
     * Starts Text-to-speech and pronounce word
     * @param context
     * @param word
     */
    private void speak(Context context, final String word) {
        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTextToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, " speak onInit: Language is not supported");
                    }
                    mTextToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    Log.e(TAG, "speak onInit: Initialization failed");
                }
            }
        });
    }


    //SectionIndexer methods
    @Override
    public Object[] getSections() {
        return mAlphabetIndexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mAlphabetIndexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return mAlphabetIndexer.getSectionForPosition(position);
    }
}
