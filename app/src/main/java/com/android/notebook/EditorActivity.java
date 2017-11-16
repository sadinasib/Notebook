package com.android.notebook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import static com.android.notebook.data.NotebookContract.NotebookEntry;

public class EditorActivity extends AppCompatActivity {
    private static final String TAG = EditorActivity.class.getSimpleName();
    private EditText mWordEdit;
    private EditText mTransEdit;
    private Uri mWordUri;
    private boolean mEditTextHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.android.notebook.R.layout.activity_editor);

        mWordEdit = findViewById(com.android.notebook.R.id.editor_edit_word);
        mTransEdit = findViewById(com.android.notebook.R.id.editor_edit_trans);

        Intent intent = getIntent();
        mWordUri = intent.getData();

        if (mWordUri == null) {
            setTitle(getString(com.android.notebook.R.string.editor_activity_title_add));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(com.android.notebook.R.string.editor_activity_title_edit));
            updateUI();
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mEditTextHasChanged = true;
            }
        };
        mWordEdit.addTextChangedListener(textWatcher);
        mTransEdit.addTextChangedListener(textWatcher);
    }

    private void updateUI() {
        Log.i(TAG, "updateUI");
        String[] projection = {
                NotebookEntry._ID,
                NotebookEntry.COLUMN_WORD,
                NotebookEntry.COLUMN_TRANSLATION};

        Cursor cursor = getContentResolver().query(mWordUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            mWordEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotebookEntry.COLUMN_WORD)));
            mTransEdit.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotebookEntry.COLUMN_TRANSLATION)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.android.notebook.R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mWordUri == null) {
            MenuItem menuItem = menu.findItem(com.android.notebook.R.id.action_editor_delete);
            menuItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.android.notebook.R.id.action_editor_save:
                saveWord();
                finish();
                return true;
            case com.android.notebook.R.id.action_editor_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (mEditTextHasChanged) {
                    showUnsavedChangesDialog();
                } else {
                    mEditTextHasChanged = false;
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveWord() {
        Log.i(TAG, "saveWord");
        String word = mWordEdit.getText().toString().trim();
        String trans = mTransEdit.getText().toString().trim();

        if (TextUtils.isEmpty(word)
                && TextUtils.isEmpty(trans)) {
            Toast.makeText(this, com.android.notebook.R.string.editor_activity_no_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (TextUtils.isEmpty(word)) {
            Toast.makeText(this, com.android.notebook.R.string.editor_activity_need_word, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (TextUtils.isEmpty(trans)) {
            Toast.makeText(this, com.android.notebook.R.string.editor_activity_need_trans, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (mWordUri == null) {
            if (isExist(word, trans)) {
                Toast.makeText(this, "Entered word already exists", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(NotebookEntry.COLUMN_WORD, word);
        values.put(NotebookEntry.COLUMN_TRANSLATION, trans);

        if (mWordUri == null) {
            Uri newUri = getContentResolver().insert(NotebookEntry.CONTENT_URI, values);
            if (newUri == null) {
                Log.e(TAG, "saveWord failed for uri " + mWordUri.toString());
                Toast.makeText(this, com.android.notebook.R.string.editor_activity_save_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, com.android.notebook.R.string.editor_activity_save_succ, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowId = getContentResolver().update(mWordUri, values, null, null);
            if (rowId == 0) {
                Log.e(TAG, "saveWord failed for uri " + mWordUri.toString());
                Toast.makeText(this, com.android.notebook.R.string.editor_activity_update_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, com.android.notebook.R.string.editor_activity_update_succ, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isExist(String word, String trans) {
        String[] projection = {NotebookEntry._ID};
        String selection = NotebookEntry.COLUMN_WORD + "=? OR " + NotebookEntry.COLUMN_TRANSLATION + " =?";
        String[] selectionArgs = new String[]{word, trans};
        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(
                NotebookEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        return cursor != null && cursor.getCount() > 0;
    }

    private void deleteWord() {
        int rowId = getContentResolver().delete(mWordUri, null, null);
        if (rowId == 0) {
            Log.e(TAG, "deleteWord failed for uri " + mWordUri.toString());
            Toast.makeText(this, com.android.notebook.R.string.editor_activity_delete_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, com.android.notebook.R.string.editor_activity_delete_succ, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnsavedChangesDialog() {
        Log.i(TAG, "showUnsavedChangesDialog");
        new AlertDialog.Builder(this)
                .setTitle(com.android.notebook.R.string.unsaved_changes_dialog_title)
                .setMessage(com.android.notebook.R.string.unsaved_changes_dialog_msg)
                .setPositiveButton(com.android.notebook.R.string.unsaved_changes_dialog_keep_editing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton(com.android.notebook.R.string.unsaved_changes_dialog_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                })
                .show();
    }

    private void showDeleteConfirmationDialog() {
        Log.i(TAG, "showDeleteConfirmationDialog");
        new AlertDialog.Builder(this)
                .setTitle(com.android.notebook.R.string.delete_confirm_dialog_title)
                .setMessage(com.android.notebook.R.string.delete_confirm_dialog_msg)
                .setPositiveButton(com.android.notebook.R.string.delete_confirm_dialog_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteWord();
                        finish();
                    }
                })
                .setNegativeButton(com.android.notebook.R.string.delete_confirm_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
