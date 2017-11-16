package com.android.notebook.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

@SuppressWarnings("WeakerAccess")
public final class NotebookContract {
    public static final String CONTENT_AUTHORITY = "com.android.notebook";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WORD = "words";

    private NotebookContract() {
    }

    public static class NotebookEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_WORD);

        public static final String TABLE_NAME = "words";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_WORD = "word";
        public static final String COLUMN_TRANSLATION = "translation";
        public static final String SORT_ORDER = COLUMN_WORD + " ASC";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WORD;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WORD;
    }
}
