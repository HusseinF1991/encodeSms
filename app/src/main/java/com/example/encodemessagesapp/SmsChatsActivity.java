package com.example.encodemessagesapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;

public class SmsChatsActivity extends AppCompatActivity {


    public static final int MY_PERMISSION_REQUEST_READ_CONTACTS = 98;
    public static final int MY_PERMISSION_REQUEST_Read_SMS = 97;
    public static final int MY_PERMISSION_REQUEST_SEND_SMS = 96;
    public static final int MY_PERMISSION_REQUEST_READ_PHONE_STATE = 95;
    public static final int MY_PERMISSION_REQUEST_RECEIVE_SMS = 94;
    public static final int MY_PERMISSION_For_5_REQUESTS = 90;

    private ListView chatsListView;
    private ArrayList<CurrentChatsModel> currentChatsArrayList;

    private BroadcastReceiver newSmsReceivedReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_chats);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_SmsChatsActivity);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null

        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        chatsListView = findViewById(R.id.listContactsChatsTitles);
        currentChatsArrayList = new ArrayList<>();

        NewSmsReceived();

        //register receiver for the broadcast intent
        LocalBroadcastManager.getInstance(this).registerReceiver(
                newSmsReceivedReceiver, new IntentFilter("newSmsReceived_SmsChatsActivity"));
    }


    @Override
    protected void onStart() {
        super.onStart();


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){

            currentChatsArrayList.clear();
            chatsListView.setAdapter(null);
            DisplayCurrentChats();
        }
        else {
            Check5Permissions();
        }
    }




    private void Check5Permissions() {
        if
        (
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(SmsChatsActivity.this
                        , new String[]{
                                Manifest.permission.READ_SMS,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_PHONE_STATE}
                        , MY_PERMISSION_For_5_REQUESTS);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_SMS,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_PHONE_STATE}
                        , MY_PERMISSION_For_5_REQUESTS);
            }
        }
    }



    //if user didn't grant the application access to the GPS then close the application
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_CONTACTS:   //checking contacts access permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    currentChatsArrayList.clear();
                    chatsListView.setAdapter(null);
                    DisplayCurrentChats();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("تنويه")
                            .setMessage("نعتذر , لا يمكنك استخدام البرنامج اذا لم تسمح له بالوصول الى الاسماء")
                            .setPositiveButton("اغلاق البرنامج", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    finish();
                                    System.exit(0);
                                }
                            })
                            .create()
                            .show();
                }
                break;
            case MY_PERMISSION_REQUEST_Read_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("تنويه")
                            .setMessage("نعتذر , لا يمكنك استخدام البرنامج اذا لم تسمح له بالوصول الى الرسائل")
                            .setPositiveButton("اغلاق البرنامج", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    finish();
                                    System.exit(0);
                                }
                            })
                            .create()
                            .show();
                }
                break;
            case MY_PERMISSION_REQUEST_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("تنويه")
                            .setMessage("نعتذر , لا يمكنك استخدام البرنامج اذا لم تسمح له بالوصول الى الرسائل")
                            .setPositiveButton("اغلاق البرنامج", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    finish();
                                    System.exit(0);
                                }
                            })
                            .create()
                            .show();
                }
                break;
            case MY_PERMISSION_REQUEST_RECEIVE_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("تنويه")
                            .setMessage("نعتذر , لا يمكنك استخدام البرنامج اذا لم تسمح له بارسال الرسائل")
                            .setPositiveButton("اغلاق البرنامج", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    finish();
                                    System.exit(0);
                                }
                            })
                            .create()
                            .show();
                }
                break;
            case MY_PERMISSION_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("تنويه")
                            .setMessage("نعتذر , لا يمكنك استخدام البرنامج اذا لم تسمح له بالوصول الى حالة الهاتف")
                            .setPositiveButton("اغلاق البرنامج", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    finish();
                                    System.exit(0);
                                }
                            })
                            .create()
                            .show();
                }
                break;
            case MY_PERMISSION_For_5_REQUESTS:
                for (int i = 0 ; i< permissions.length ; i++) {
                    Log.d("myPermission", permissions[i] + " ," + grantResults[i]);
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        if(permissions[i].equals("android.permission.READ_CONTACTS")){

                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){

                                currentChatsArrayList.clear();
                                chatsListView.setAdapter(null);
                                DisplayCurrentChats();
                            }
                        }

                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("تنويه")
                                .setMessage("نعتذر , لا يمكنك استخدام البرنامج اذا لم توافق على طلبات النظام")
                                .setPositiveButton("اغلاق البرنامج", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        finish();
                                        System.exit(0);
                                    }
                                })
                                .create()
                                .show();
                    }
                }
                break;
        }
    }




    private void NewSmsReceived(){
        newSmsReceivedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("smsChatActivity" , "new Sms received");

                if(ActivityCompat.checkSelfPermission(SmsChatsActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){

                    currentChatsArrayList.clear();
                    chatsListView.setAdapter(null);
                    DisplayCurrentChats();
                }
                else {
                    Check5Permissions();
                }
            }
        };
    }



    private void DisplayCurrentChats(){

        MySqliteDB mySqliteDB = new MySqliteDB(this);
        Cursor c = mySqliteDB.GetAvailableChats();

        //check if the numbers are saved in contacts
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver contentResolver = this.getContentResolver();

        if(c.getCount() > 0 ){
            if (c.moveToFirst()) {
                do {
                    String contact = c.getString(c.getColumnIndex("phoneNumber"));
                    String contactNumber = c.getString(c.getColumnIndex("phoneNumber"));

                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, null, null, null);

                    while (phoneCursor.moveToNext()) {
                        String number = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)).replace(" ", "");

                        if(contact.equals(number) || ("00964"+contact).equals(number) || ("+964"+contact).equals(number)) {
                            contact = phoneCursor.getString(phoneCursor.getColumnIndex(DISPLAY_NAME));
                            break;
                        }
                    }
                    phoneCursor.close();

                    String lastSmsDate = c.getString(c.getColumnIndex("messageDate"));
                    String lastSms = c.getString(c.getColumnIndex("message"));
                    int isRead = c.getInt(c.getColumnIndex("read"));
                    CurrentChatsModel currentChatsModel = new CurrentChatsModel( contactNumber ,contact, lastSmsDate, lastSms ,isRead);
                    Log.d("SmsChatsActivity", currentChatsModel.toString());
                    currentChatsArrayList.add(currentChatsModel);
                } while (c.moveToNext());
            }
            c.close();

            CurrentChatsAdapter currentChatAdapter = new CurrentChatsAdapter(this ,
                    R.layout.chats_list_layout , currentChatsArrayList);
            chatsListView.setAdapter(currentChatAdapter);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.smschats_menu , menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.newSms_ChatsSmsActivity){
            Intent intent = new Intent(this , NewSmsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}


class CurrentChatsModel{

    private String contactNumber , contact , lastSmsDate , lastSms ;
    private int isRead;

    public CurrentChatsModel(String contactNumber, String contact, String lastSmsDate, String lastSms,int  isRead) {
        this.contact = contact;
        this.contactNumber = contactNumber;
        this.lastSmsDate = lastSmsDate;
        this.lastSms = lastSms;
        this.isRead = isRead;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public int getIsRead() {
        return isRead;
    }

    public String getContact() {
        return contact;
    }

    public String getLastSmsDate() {
        return lastSmsDate;
    }

    public String getLastSms() {
        return lastSms;
    }

    public String toString(){
        return contactNumber + " " + contact + " " + lastSmsDate + " " + lastSms + " " + isRead;
    }
}