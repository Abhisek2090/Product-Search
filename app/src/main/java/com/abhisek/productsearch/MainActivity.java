package com.abhisek.productsearch;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private  Context mContext;
    ListView listView;
    DatabaseTable db;
    String [] emptyArray = new String [0];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        final SearchView searchView = (SearchView)findViewById(R.id.searchWords);
        searchView.setQueryHint("Enter first 2 letters of item");
        listView = (ListView) findViewById(R.id.listView);

        db = new DatabaseTable(mContext);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

              String item = (String) adapterView.getAdapter().getItem(i);
              searchView.setQuery(item, false);
              Log.i(TAG, item);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                if(newText.length()>=2) {

                    Cursor c = db.getWordMatches(newText, null);
                    if (c != null) {
                        String [] array = new String[c.getCount()];

                         c.moveToFirst();

                        for( int i = 0;i<10;i++) {

                            while (!c.isAfterLast()) {
                                array[i] = c.getString(0);
                                i++;
                                c.moveToNext();

                            }
                        }



                        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                            @Override
                            public boolean onClose() {

                                listView.setAdapter(new ArrayAdapter<String>(mContext,android.R.layout.simple_dropdown_item_1line, emptyArray));
                                return false;
                            }
                        });

                        listView.setAdapter(new ArrayAdapter<String>(mContext,android.R.layout.simple_dropdown_item_1line, array));


                    }
                }
                return false;
            }
        });
    }


    public class DatabaseTable {

        private static final String TAG = "SearchDatabase";
        //The columns we'll include in the search table
        public static final String COL_WORD = "WORD";
        private static final String DATABASE_NAME = "WordSearch";
        private static final String FTS_VIRTUAL_TABLE = "FTS";
        private static final int DATABASE_VERSION = 1;
        private final DatabaseOpenHelper mDatabaseOpenHelper;

        public DatabaseTable(Context context) {
            mDatabaseOpenHelper = new DatabaseOpenHelper(context);
        }

        private class DatabaseOpenHelper extends SQLiteOpenHelper {
            private final Context mHelperContext;
            private SQLiteDatabase mDatabase;

            private static final String FTS_TABLE_CREATE =
                    "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                            " USING fts4 (" +
                            COL_WORD + ")";

            DatabaseOpenHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_VERSION);
                mHelperContext = context;
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                mDatabase = db;
                mDatabase.execSQL(FTS_TABLE_CREATE);
                loadKeywords();
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int i, int i1) {

                Log.w(TAG, "Upgrading database from version " + i + " to "
                        + i1 + ", which will destroy all old data");
                db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
                onCreate(db);

            }

            private void loadKeywords() {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            loadWords();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }

            private void loadWords() throws IOException {
                final Resources resources = mHelperContext.getResources();
                InputStream inputStream = resources.openRawResource(R.raw.keys123);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                //Read text from file
                StringBuilder text = new StringBuilder();

                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.replaceAll("\"", "");
                        line  = line.replace("?", "");
                        line.replace("?", "");

                        String [] array = TextUtils.split(line, ",");

                       for(int j=0;j<array.length;j++) {
                           addWord(array[j]);
                       }

                    }
                } finally {
                    reader.close();
                }
            }

            public long addWord(String word) {
                ContentValues initialValues = new ContentValues();
                initialValues.put(COL_WORD, word);
                return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
            }
        }

        public Cursor getWordMatches(String query, String[] columns) {
            Log.i("Method","getWordMatches");
            String selection = COL_WORD + " MATCH ?";
            String[] selectionArgs = new String[]{query +"*"};

            return query(selection, selectionArgs, columns);
        }

        private Cursor query(String selection, String[] selectionArgs, String[] columns) {
            Log.i("Method","query");
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(FTS_VIRTUAL_TABLE);

            Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                    columns, selection, selectionArgs, null, null, null);

            if (cursor == null) {
                //Log.i("cursor", "null1");
                return null;
            } else if (!cursor.moveToFirst()) {
              //  Log.i("cursor", "null2");
                cursor.close();
                return null;
            }
            return cursor;
        }

    }
}



