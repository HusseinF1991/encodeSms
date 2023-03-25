package com.example.encodemessagesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactChatActivity extends AppCompatActivity {

    String contactNumber;
    String contactName;

    private String myPhoneNumber;
    private int simId;
    private String cipherKeyOfSms;

    //private static String SmsPrefix = "WOW";

    private ListView contactChatListView;
    private EditText etxtInputMessage;
    private Button btnSendMessage;
    private ProgressBar pBarSmsSent;

    private BroadcastReceiver newSmsReceivedReceiver = null;

    private ArrayList<ContactChatModel> contactChatArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_chat);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        contactName = intent.getStringExtra("contactName");
        contactNumber = intent.getStringExtra("contactNumber");

        contactChatListView = findViewById(R.id.contactChatListView);
        etxtInputMessage = findViewById(R.id.etxtInputMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        pBarSmsSent = findViewById(R.id.pBarSmsSent);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ContactChatActivity);
        toolbar.setTitle(contactName);
        toolbar.setTitleTextColor(Color.WHITE);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);



        SharedPreferences sharedPref = this.getSharedPreferences("EncodeSmsSharedPrefereces", Context.MODE_PRIVATE);
        myPhoneNumber = sharedPref.getString("EncodeSmsPhone_SP", "notRegistered");
        simId = sharedPref.getInt("EncodeSmsSimId_SP", 0);
        cipherKeyOfSms = sharedPref.getString("EncodeSmsCipherKey_SP" , "HHHHHH");

        GetContactChatHistory();
        SendNewMessage();
        NewSmsReceived();
        OnTextChangeInSmsBody();


        //register receiver for the broadcast intent
        LocalBroadcastManager.getInstance(this).registerReceiver(
                newSmsReceivedReceiver, new IntentFilter("newSmsReceived_ContactChatActivity"));
    }



    private void OnTextChangeInSmsBody(){
        etxtInputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String smsBody_Encrypted = AES.encrypt(etxtInputMessage.getText().toString() , cipherKeyOfSms +myPhoneNumber + contactNumber);//cipherKeyFromAdmin +myPhoneNumber+  receiverPhoneNumber
                //smsBody_Encrypted = SmsPrefix + smsBody_Encrypted;

                //give warning if the ciphered message length passed 156 character
                if(smsBody_Encrypted.length() > 156){
                    Toast.makeText(ContactChatActivity.this , "وصلت الحد الاعلى لحجم الرسالة الواحدة", Toast.LENGTH_SHORT).show();
                }

                //trim from entered sms until the ciphered sms reaches 156 character
                while (smsBody_Encrypted.length() > 156){

                    etxtInputMessage.setText(etxtInputMessage.getText().toString().substring(0 ,etxtInputMessage.getText().toString().length() -1 ));
                    smsBody_Encrypted = AES.encrypt(etxtInputMessage.getText().toString() , cipherKeyOfSms + myPhoneNumber + contactNumber);//cipherKeyFromAdmin +myPhoneNumber+  receiverPhoneNumber
                    //smsBody_Encrypted = SmsPrefix + smsBody_Encrypted;

                    etxtInputMessage.setSelection(etxtInputMessage.getText().length());
                }


                Log.d("Smslength " , "before encryption "+ etxtInputMessage.getText().toString()+ " L:" +etxtInputMessage.getText().toString().length());
                Log.d("Smslength " , "after encryption " + smsBody_Encrypted+ " L:" +smsBody_Encrypted.length()+"");

            }
        });
    }



    private void GetContactChatHistory(){
        MySqliteDB mySqliteDB = new MySqliteDB(this);
        Cursor c = mySqliteDB.GetContactHistoryChat(contactNumber);


        if(c.getCount() > 0 ){
            if (c.moveToFirst()) {
                do {
                    int smsId = c.getInt(c.getColumnIndex("Id"));
                    String smsDate = c.getString(c.getColumnIndex("messageDate"));
                    String message = c.getString(c.getColumnIndex("message"));
                    String sent_Received = c.getString(c.getColumnIndex("sent_received"));
                    int delivered = c.getInt(c.getColumnIndex("delivered"));
                    ContactChatModel contactChatModel = new ContactChatModel(smsId, message, smsDate, sent_Received , delivered);

                    Log.d("ContactChatActivity", contactChatModel.toString());
                    contactChatArrayList.add(contactChatModel);
                } while (c.moveToNext());
            }
            c.close();

            ContactChatAdapter contactChatAdapter = new ContactChatAdapter(this ,
                    R.layout.chat_message_list_item , contactChatArrayList);
            contactChatListView.setAdapter(contactChatAdapter);
            contactChatListView.setSelection(contactChatAdapter.getCount() - 1);
        }
    }



    private void SendNewMessage(){
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etxtInputMessage.getText().toString().trim().equals("")){

                    final String receiverPhoneNumber = contactNumber;
                    final String smsBody_Plain = etxtInputMessage.getText().toString().trim();
                    //String smsBody_beforeEncryption = smsBody_Plain + "true";
                    String smsBody_beforeEncryption = smsBody_Plain;

                    String smsBody_Encrypted = AES.encrypt(smsBody_beforeEncryption , cipherKeyOfSms + myPhoneNumber + receiverPhoneNumber);//cipherKeyFromAdmin +myPhoneNumber+  receiverPhoneNumber
                    //smsBody_Encrypted = SmsPrefix + smsBody_Encrypted;


                    pBarSmsSent.setVisibility(View.VISIBLE);
                    etxtInputMessage.setEnabled(false);
                    btnSendMessage.setEnabled(false);


                    final MySqliteDB mySqliteDB = new MySqliteDB(ContactChatActivity.this);

                    // SMS sent pending intent
                    PendingIntent sentIntent = PendingIntent.getBroadcast(ContactChatActivity.this, 15,
                            new Intent("SMS_SENT_ACTION"), 0);

                    registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            int resultCode = getResultCode();
                            switch (resultCode) {
                                case Activity.RESULT_OK:
                                    Toast.makeText(ContactChatActivity.this , "تم ارسال الرسالة" , Toast.LENGTH_SHORT).show();


                                    mySqliteDB.SaveReceivedSms(receiverPhoneNumber , smsBody_Plain  , "sent" , 1 ,1 );

                                    contactChatArrayList.clear();
                                    contactChatListView.setAdapter(null);
                                    GetContactChatHistory();


                                    pBarSmsSent.setVisibility(View.INVISIBLE);
                                    etxtInputMessage.setEnabled(true);
                                    btnSendMessage.setEnabled(true);
                                    break;
                                default:
                                    Toast.makeText(ContactChatActivity.this , "فشل في ارسال الرسالة" , Toast.LENGTH_SHORT).show();

                                    mySqliteDB.SaveReceivedSms(receiverPhoneNumber , smsBody_Plain  , "sent" , 1 ,0 );

                                    contactChatArrayList.clear();
                                    contactChatListView.setAdapter(null);
                                    GetContactChatHistory();

                                    pBarSmsSent.setVisibility(View.INVISIBLE);
                                    etxtInputMessage.setEnabled(true);
                                    btnSendMessage.setEnabled(true);
                                    break;
                                  /*
                                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                    Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_LONG).show();
                                    break;
                                case SmsManager.RESULT_ERROR_NO_SERVICE:
                                    Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_LONG).show();
                                    break;
                                case SmsManager.RESULT_ERROR_NULL_PDU:
                                    Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_LONG).show();
                                    break;
                                case SmsManager.RESULT_ERROR_RADIO_OFF:
                                    Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_LONG).show();
                                    break;*/
                            }
                        }
                    }, new IntentFilter("SMS_SENT_ACTION"));

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.getSmsManagerForSubscriptionId(simId).sendTextMessage(receiverPhoneNumber, null, smsBody_Encrypted, sentIntent, null);

                    etxtInputMessage.setText("");
                }
            }
        });
    }




    private void NewSmsReceived(){
        newSmsReceivedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String senderNumber = intent.getStringExtra("sender");
                if(contactNumber.equals(senderNumber)){

                    contactChatArrayList.clear();
                    contactChatListView.setAdapter(null);
                    GetContactChatHistory();
                }
            }
        };
    }
}


class ContactChatModel{

    String smsBody , smsTime , sent_received ;
    int smsId , delivered;

    public ContactChatModel(int smsId ,String smsBody, String smsTime , String sent_received , int delivered) {
        this.smsBody = smsBody;
        this.smsTime = smsTime;
        this.sent_received = sent_received;
        this.smsId = smsId;
        this.delivered = delivered;
    }

    public String getSmsBody() {
        return smsBody;
    }

    public String getSmsTime() {
        return smsTime;
    }

    public String getSent_received() {
        return sent_received;
    }

    public int getSmsId() { return smsId;   }

    public int getDelivered() {   return delivered;
    }
}