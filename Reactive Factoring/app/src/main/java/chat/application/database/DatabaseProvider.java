package chat.application.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import chat.application.MyAppication;
import chat.application.model.Contact;
import chat.application.model.ContactObject;
import chat.application.model.Message;

/**
 * Created by Pbuhsoft on 06/07/2016.
 */
public class DatabaseProvider {
    private SQLiteDatabase database;
    private Context mContext;


    public DatabaseProvider(Context context){
        database=new MySQLiteOpenHelper(context).getWritableDatabase();
        mContext=context;
    }
    public void openConn(){
        database=new MySQLiteOpenHelper(mContext).getWritableDatabase();
    }
    public void openConnRead() { database = new MySQLiteOpenHelper(mContext).getReadableDatabase();}
    public void closeConn(){
        database.close();
    }




    static class  MySQLiteOpenHelper extends SQLiteOpenHelper {
        // Database Version
        private static final int DATABASE_VERSION = 1;

        // Database Name
        private static final String DATABASE_NAME = "db_chatapplication";

        // Table Names1
        private static final String TABLE_CONTACTS = "table_contacts";

        // Table Names2
        private static final String TABLE_MESSAGES = "table_messages";

        // column names
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_SUBJECT = "subject";
        private static final String COL_MSG_BODY = "msg_body";
        private static final String COL_LIVE_TIME = "live_time";

        private static final String COL_USER_IMAGE = "user_image";
        private static final String COL_PUBLIC_KEY = "public_key";

        // Table create statement1
        private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE " + TABLE_CONTACTS
                + "(" +
                COL_ID + " integer primary key autoincrement," +
                COL_USERNAME + " TEXT," + COL_USER_IMAGE + " blob,"+
                COL_PUBLIC_KEY +" text)";

        // Table create statement2
        private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES
                + "(" +
                COL_ID + " integer primary key autoincrement," +
                COL_USERNAME + " TEXT," +COL_SUBJECT +" text,"+
                COL_MSG_BODY +" text,"+COL_LIVE_TIME+" long)";


        public MySQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            // creating table
            db.execSQL(CREATE_TABLE_CONTACTS);
            db.execSQL(CREATE_TABLE_MESSAGES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // on upgrade drop older tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            // create new table
            onCreate(db);
        }
    }


    public void addNewMessage(Message message){
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteOpenHelper.COL_USERNAME,message.getUsername());
        cv.put(MySQLiteOpenHelper.COL_SUBJECT,message.getSubject());
        cv.put(MySQLiteOpenHelper.COL_MSG_BODY,message.getMsg_body());
        cv.put(MySQLiteOpenHelper.COL_LIVE_TIME,message.getTime_to_live());
        try {
            openConn();

            database.insert(MySQLiteOpenHelper.TABLE_MESSAGES, null, cv);
        }
        catch (SQLiteException ex){
            if (database!=null)
                closeConn();
            Log.v("wasim",ex.toString());
        }
        if (database!=null)
            closeConn();
    }
    public void updateMessage(Message message){
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteOpenHelper.COL_USERNAME,message.getUsername());
        cv.put(MySQLiteOpenHelper.COL_SUBJECT,message.getSubject());
        cv.put(MySQLiteOpenHelper.COL_MSG_BODY,message.getMsg_body());
        cv.put(MySQLiteOpenHelper.COL_LIVE_TIME,message.getTime_to_live());
        try {
            openConn();
            database.update(MySQLiteOpenHelper.TABLE_MESSAGES,cv,MySQLiteOpenHelper.COL_ID+"="+message.getId(),null);
        }
        catch (SQLiteException ex){
            if (database!=null)
                closeConn();
            Log.v("wasim",ex.toString());
        }
        if (database!=null)
            closeConn();
    }
    public void deleteMessage(int id) {
        String query="delete from "+MySQLiteOpenHelper.TABLE_MESSAGES
                +" where "
                +MySQLiteOpenHelper.COL_ID+"="+id+"";
        try {
            openConn();
            database.execSQL(query);
        }
        catch (SQLiteException ex){
            if (database!=null)
                closeConn();
            Log.v("wasim",ex.toString());
        }
        if (database!=null)
            closeConn();
    }
    public void addContact(Contact contactObject){
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteOpenHelper.COL_USERNAME,contactObject.getUsername());
        cv.put(MySQLiteOpenHelper.COL_USER_IMAGE,contactObject.getImage());
        cv.put(MySQLiteOpenHelper.COL_PUBLIC_KEY,contactObject.getPublic_key());
        try {
            openConn();
            //if(!IsDataInDB(MySQLiteOpenHelper.TABLE_CONTACTS,MySQLiteOpenHelper.COL_USERNAME,contactObject.getUsername()))

                Log.d("DB","writing to DB");
            database.insert(MySQLiteOpenHelper.TABLE_CONTACTS, null, cv);
            //closeConn();
        }
        catch (SQLiteException ex){
            if (database!=null)
                closeConn();
            Log.v("wasim",ex.toString());
        }
        if (database!=null)
            closeConn();
    }
    public void updateContact(Contact contact){
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteOpenHelper.COL_USERNAME,contact.getUsername());
        cv.put(MySQLiteOpenHelper.COL_USER_IMAGE,contact.getImage());
        cv.put(MySQLiteOpenHelper.COL_PUBLIC_KEY,contact.getPublic_key());

        try {
            openConn();
            database.update(MySQLiteOpenHelper.TABLE_CONTACTS,cv
                    ,MySQLiteOpenHelper.COL_ID+"="+contact.getId(),null);
        }
        catch (SQLiteException ex){
            if (database!=null)
                closeConn();
            Log.v("wasim",ex.toString());
        }
        if (database!=null)
            closeConn();
    }
    public void removeContact(int id) {
        String query="delete from "+MySQLiteOpenHelper.TABLE_CONTACTS
                +" where "
                +MySQLiteOpenHelper.COL_ID+"="+id+"";
        try {
            openConn();
            database.execSQL(query);
        }
        catch (SQLiteException ex){
            if (database!=null)
                closeConn();
            Log.v("wasim",ex.toString());
        }
        if (database!=null)
            closeConn();
    }
    public ArrayList<Contact> getContacts() throws SQLiteException {
        String query="select * from "+MySQLiteOpenHelper.TABLE_CONTACTS;
        ArrayList<Contact> arrayList=new ArrayList<>();
        try {
            openConn();
            Cursor cursor= database.rawQuery(query,new String[]{});
            while (cursor.moveToNext()){
                Contact contact=new Contact(cursor.getInt(0)
                        ,cursor.getString(1)
                        ,cursor.getBlob(2)
                        ,cursor.getString(3));

                arrayList.add(contact);
            }
            cursor.close();
        }
        catch (SQLiteException ex){
            Log.v("wasim",ex.toString());
            if (database!=null)
                closeConn();
        }
        if (database!=null)
            closeConn();
        return arrayList;
    }
    public ArrayList<Message> getMessages() throws SQLiteException {
        String query="select * from "+MySQLiteOpenHelper.TABLE_MESSAGES;
        ArrayList<Message> arrayList=new ArrayList<>();
        try {
            openConn();
            Cursor cursor= database.rawQuery(query,new String[]{});
            while (cursor.moveToNext()){
                Message message=new Message(cursor.getInt(0)
                        ,cursor.getString(1)
                        ,cursor.getString(2)
                        ,cursor.getString(3)
                        ,cursor.getLong(4));

                arrayList.add(message);
            }
            cursor.close();
        }
        catch (SQLiteException ex){
            Log.v("wasim",ex.toString());
            if (database!=null)
                closeConn();
        }
        if (database!=null)
            closeConn();
        return arrayList;
    }
}