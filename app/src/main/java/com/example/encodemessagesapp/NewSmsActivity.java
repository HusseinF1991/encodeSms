package com.example.encodemessagesapp;

import androidx.annotation.Nullable;
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
import android.hardware.camera2.TotalCaptureResult;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewSmsActivity extends AppCompatActivity {

    private Button btnSendSms;
    private Button btnGetContact;
    private EditText etxtNewSmsBody;
    private EditText etxtReceiverNumber;

    private String myPhoneNumber;
    private int simId;
    private String cipherKeyOfSms;

    //private static String SmsPrefix = "WOW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sms);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_NewSmsActivity);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        btnSendSms = findViewById(R.id.btnSendSms);
        btnGetContact = findViewById(R.id.btnGetContact);
        etxtNewSmsBody = findViewById(R.id.etxtNewSmsBody);
        etxtReceiverNumber = findViewById(R.id.etxtReceiverNumber);



        SharedPreferences sharedPref = this.getSharedPreferences("EncodeSmsSharedPrefereces", Context.MODE_PRIVATE);
        myPhoneNumber = sharedPref.getString("EncodeSmsPhone_SP", "notRegistered");
        simId = sharedPref.getInt("EncodeSmsSimId_SP", 0);
        cipherKeyOfSms = sharedPref.getString("EncodeSmsCipherKey_SP" , "HHHHHH");

        SendNewSms();
        GetContact();

        OnTextChangeInSmsBody();
    }



    private void OnTextChangeInSmsBody(){
        etxtNewSmsBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String smsBody_Encrypted = AES.encrypt(etxtNewSmsBody.getText().toString() , cipherKeyOfSms + myPhoneNumber +  "98765432198");//cipherKeyFromAdmin + myPhoneNumber + receiverPhoneNumber
                //smsBody_Encrypted = SmsPrefix + smsBody_Encrypted;

                //give warning if the ciphered message length passed 156 character
                if(smsBody_Encrypted.length() > 156){
                    Toast.makeText(NewSmsActivity.this , "وصلت الحد الاعلى لحجم الرسالة الواحدة", Toast.LENGTH_SHORT).show();
                }

                //trim from entered sms until the ciphered sms reaches 156 character
                while (smsBody_Encrypted.length() > 156){

                    etxtNewSmsBody.setText(etxtNewSmsBody.getText().toString().substring(0 ,etxtNewSmsBody.getText().toString().length() -1 ));
                    smsBody_Encrypted = AES.encrypt(etxtNewSmsBody.getText().toString() , cipherKeyOfSms + myPhoneNumber + "98765432198");//cipherKeyFromAdmin + myPhoneNumber + receiverPhoneNumber
                    //smsBody_Encrypted = SmsPrefix + smsBody_Encrypted;

                    etxtNewSmsBody.setSelection(etxtNewSmsBody.getText().length());
                }


                Log.d("Smslength" , "before encryption "+ etxtNewSmsBody.getText().toString()+ " L:" +etxtNewSmsBody.getText().toString().length());
                Log.d("Smslength" , "after encryption " + smsBody_Encrypted+ " L:" +smsBody_Encrypted.length()+"");
            }
        });
    }



    private void SendNewSms(){
        btnSendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etxtReceiverNumber.getText().toString().equals("")){
                    Toast.makeText(NewSmsActivity.this , "أدخل رقم المستلم رجاءا", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(etxtNewSmsBody.getText().toString().trim().equals("")){
                    Toast.makeText(NewSmsActivity.this , "لا يمكنك ارسال رسالة فارغة", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{

                    final String receiverPhoneNumber = etxtReceiverNumber.getText().toString();
                    final String smsBody_Plain = etxtNewSmsBody.getText().toString().trim();
                    String smsBody_beforeEncryption =  smsBody_Plain;

                    String smsBody_Encrypted = AES.encrypt(smsBody_beforeEncryption , cipherKeyOfSms + myPhoneNumber + receiverPhoneNumber);//cipherKeyFromAdmin + myPhoneNumber + receiverPhoneNumber
                    //smsBody_Encrypted = SmsPrefix + smsBody_Encrypted;

                    final MySqliteDB mySqliteDB = new MySqliteDB(NewSmsActivity.this);

                    // SMS sent pending intent
                    PendingIntent sentIntent = PendingIntent.getBroadcast(NewSmsActivity.this, 15,
                            new Intent("SMS_SENT_ACTION"), 0);

                    registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            int resultCode = getResultCode();
                            switch (resultCode) {
                                case Activity.RESULT_OK:
                                    Toast.makeText(NewSmsActivity.this , "تم ارسال الرسالة" , Toast.LENGTH_SHORT).show();


                                    mySqliteDB.SaveReceivedSms(receiverPhoneNumber , smsBody_Plain  , "sent" , 1 ,1 );
                                    break;
                                default:
                                    Toast.makeText(NewSmsActivity.this , "فشل في ارسال الرسالة" , Toast.LENGTH_SHORT).show();

                                    mySqliteDB.SaveReceivedSms(receiverPhoneNumber , smsBody_Plain  , "sent" , 1 ,0 );
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

                    etxtNewSmsBody.setText("");
                }
            }
        });
    }



    private void GetContact(){
        btnGetContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etxtReceiverNumber.setText("");

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 7);
            }
        });
    }



    //on activityResult for fetched contact
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (7):
                if (resultCode == Activity.RESULT_OK) {

                    Uri uri;
                    Cursor cursor1, cursor2;
                    String TempNameHolder, TempNumberHolder, TempContactID, IDresult = "" ;
                    int IDresultHolder ;

                    uri = data.getData();

                    cursor1 = getContentResolver().query(uri, null, null, null, null);

                    if (cursor1.moveToFirst()) {

                        String selectedContactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        TempContactID = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));

                        IDresult = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        IDresultHolder = Integer.valueOf(IDresult) ;

                        if (IDresultHolder == 1) {

                            cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + TempContactID, null, null);

                            while (cursor2.moveToNext()) {

                                TempNumberHolder = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                TempNumberHolder = TempNumberHolder.replace(" ", "");

                                //remove +964 from the prefix of the number
                                if(TempNumberHolder.startsWith("+964")){

                                    TempNumberHolder = TempNumberHolder.substring(4 , TempNumberHolder.length());
                                    TempNumberHolder = "0" + TempNumberHolder;
                                }
                                else if(TempNumberHolder.startsWith("00964")){

                                    TempNumberHolder = TempNumberHolder.substring(5 , TempNumberHolder.length());
                                    TempNumberHolder = "0" + TempNumberHolder;
                                }
                                etxtReceiverNumber.setText(TempNumberHolder);
                                break;
                            }
                        }

                    }
                }
                break;
        }
    }
}
