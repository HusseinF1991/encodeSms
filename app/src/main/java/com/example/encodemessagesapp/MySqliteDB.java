package com.example.encodemessagesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MySqliteDB extends SQLiteOpenHelper {


    private static final String sqliteDbName = "EncodeMessagesApp";

    public MySqliteDB(@Nullable Context context) {
        super(context, sqliteDbName , null, 1);


        Log.d("sqlite_db" ,"called");

        SQLiteDatabase mySqlite = this.getWritableDatabase();
        //mySqlite.execSQL("DROP TABLE messages");
        //onCreate(mySqlite);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("sqlite_db" ,"Created");

        db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (" +
                "Id INTEGER NOT NULL primary key AUTOINCREMENT , " +
                "phoneNumber varchar(25) NOT NULL, " +
                "message varchar(255) NOT NULL, " +
                "sent_received varchar(20) NOT NULL, " +
                "read INTEGER DEFAULT 0 NOT NULL, " +
                "messageDate DateTime DEFAULT NULL," +
                "delivered INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d("sqlite_db" ,"Upgraded");
    }



    public boolean SaveReceivedSms(String senderNumber ,String sms , String sent_received , int readStatus , int delivered){

        Log.d("sqlite_db" , "InsertToMessagesTbl_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phoneNumber" , senderNumber);
        contentValues.put("message" ,sms );
        contentValues.put("sent_received" , sent_received);
        contentValues.put("read" , readStatus);
        contentValues.put("delivered" , delivered);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sdf.format(new Date());
        contentValues.put("messageDate" , date);

        Long result = sqLiteDatabase.insert( "messages" , null , contentValues);

        Log.d("sqlite_db" , "InsertNewSms" + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public boolean UpdateSmsReadStatus(String senderNumber){

        Log.d("sqlite_db" , "UpdateToMessagesTbl_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("read" , 1);

        int result = sqLiteDatabase.update("messages" , contentValues , "phoneNumber = ?" , new String[]{senderNumber});

        Log.d("sqlite_db" , "Update Read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public Cursor GetAvailableChats(){
        Log.d("sqlite_db" , "GetAvailableChats_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From messages t INNER JOIN (SELECT MAX(messageDate) as max_date, phoneNumber  FROM messages GROUP BY phoneNumber) a ON a.phoneNumber = t.phoneNumber AND a.max_date = t.messageDate"  , null);
        //Cursor cursor = sqLiteDatabase.rawQuery("Select * From messages WHERE phoneNumber = ? order by messageDate DESC limit 1"  , new String[]{"07818650366"});


        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetAvailableChats_Completed");

        return cursor;
    }



    public Cursor GetContactHistoryChat(String phoneNumber){
        Log.d("sqlite_db" , "GetContactHistoryChat_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From messages WHERE phoneNumber = ? "  , new String[]{phoneNumber});


        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetContactHistoryChat_Completed");

        return cursor;
    }



    public boolean DeleteOneSms(int smsId){

        Log.d("sqlite_db" , "DeleteFromMessagesTbl_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        int result = sqLiteDatabase.delete("messages" , "Id = "+ smsId , null);

        Log.d("sqlite_db" , "Delete Read status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public boolean UpdateDeliveredStatus(int smsId){
        Log.d("sqlite_db" , "UpdateDeliveredStatus_started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("delivered" , 1);

        int result = sqLiteDatabase.update("messages" , contentValues , "Id = " + smsId , null);

        Log.d("sqlite_db" , "Update delivered status " + result);

        if(result == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public Cursor GetOneSms(int smsId){

        Log.d("sqlite_db" , "GetOneSms_Started");

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("Select * From messages WHERE Id = " + smsId  , null);


        //sqLiteDatabase.close();
        Log.d("sqlite_db", "row:"+cursor.getCount());
        Log.d("sqlite_db" , "GetOneSms_Completed");

        return cursor;
    }
}
