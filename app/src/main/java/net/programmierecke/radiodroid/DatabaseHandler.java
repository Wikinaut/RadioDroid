package net.programmierecke.radiodroid;
 
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.DatabaseUtils;
import android.util.Log;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
    // for our logs
    public static final String TAG = "DatabaseHandler.java";
 
    // database version
    private static final int DATABASE_VERSION = 4;
 
    // database name
    protected static final String DATABASE_NAME = "StationList";
 
    // table details
    public String tableName = "stations";
    public String fieldObjectId = "id";
    public String fieldObjectName = "name";
 
    // constructor
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    /**
     * This method acts as an alias for the sqlEscapeString(str) method in
     * DatabaseUtils.
     * 
     * @param str
     * @return
     */
    private static String sant(String str) {
        return DatabaseUtils.sqlEscapeString(str);
    }
    
    // creating table
    @Override
    public void onCreate(SQLiteDatabase db) {
 
        String sql = "";
 
        sql += "CREATE TABLE " + tableName;
        sql += " ( ";
        sql += fieldObjectId + " INTEGER PRIMARY KEY AUTOINCREMENT, ";
        sql += fieldObjectName + " TEXT ";
        sql += " ) ";
 
        db.execSQL(sql);
 
    }
 
    // When upgrading the database, it will drop the current table and recreate.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
        String sql = "DROP TABLE IF EXISTS " + tableName;
        db.execSQL(sql);
 
        onCreate(db);
    }
 
    // create new record
    // @param myObj contains details to be added as single row.
    public boolean create( RadioStation station) {
 
        boolean createSuccessful = false;
 
        if(true /* !checkIfExists(station.name) */ ){
                     
            SQLiteDatabase db = this.getWritableDatabase();
             
            ContentValues values = new ContentValues();
            values.put( fieldObjectName, station.name );
            createSuccessful = db.insert(tableName, null, values) > 0;
             
            db.close();
             
            // if(createSuccessful){
            //    Log.e(TAG, station.name + " created.");
            // }
        }
         
        return createSuccessful;
    }
     
    // check if a record exists so it won't insert the next time you run this code
    public boolean checkIfExists(String name){
         
        boolean recordExists = false;
		Log.e(TAG, "checking " + name );
         
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + fieldObjectId + " FROM " + tableName + " WHERE " + fieldObjectName + " = " + sant( name ), null);
         
        if(cursor!=null) {
             
            if(cursor.getCount()>0) {
                recordExists = true;
            }
        }
 
        cursor.close();
        db.close();
         
        return recordExists;
    }
 
    // Read records related to the search term
    public List<MyObject> read(String searchTerm) {
 
        List<MyObject> recordsList = new ArrayList<MyObject>();
 
        // select query
        String sql = "";
        sql += "SELECT * FROM " + tableName;
        sql += " WHERE " + fieldObjectName + " LIKE " + sant( "%" + searchTerm + "%" );
        sql += " ORDER BY " + fieldObjectId + " DESC";
        sql += " LIMIT 0,10";
 
        SQLiteDatabase db = this.getWritableDatabase();
 
        // execute the query
        Cursor cursor = db.rawQuery( sql, null );
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
 
                // int productId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(fieldProductId)));
                String name = cursor.getString(cursor.getColumnIndex(fieldObjectName));
                MyObject myObject = new MyObject(name);
 
                // add to list
                recordsList.add(myObject);
 
            } while (cursor.moveToNext());
        }
 
        cursor.close();
        db.close();
 
        // return the list of records
        return recordsList;
    }

}
