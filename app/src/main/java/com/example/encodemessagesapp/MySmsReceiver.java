package com.example.encodemessagesapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class MySmsReceiver extends BroadcastReceiver {

    private static final int requestCode_SmsReceived = 314;

    private static final String TAG = MySmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";

    //private static String SmsPrefix = "WOW";

    private String myPhoneNumber;
    private int simId;
    private String cipherKeyOfSms;
    private Context myContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        myContext = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("channel1", "myChannel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("notificationForNewSms");
            NotificationManager manager = (NotificationManager) myContext.getSystemService(myContext.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }


        SharedPreferences sharedPref = context.getSharedPreferences("EncodeSmsSharedPrefereces", Context.MODE_PRIVATE);
        myPhoneNumber = sharedPref.getString("EncodeSmsPhone_SP", "notRegistered");
        simId = sharedPref.getInt("EncodeSmsSimId_SP", 0);
        cipherKeyOfSms = sharedPref.getString("EncodeSmsCipherKey_SP" , "HHHHHH");

        Log.d(TAG , "checkMsgReceived true");

        // Get the SMS message.
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage_Sender = "";
        String strMessage_Body = "";
        String format = bundle.getString("format");


        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);

        if (pdus != null) {

            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                // Build the message to show.
                strMessage_Sender = msgs[i].getOriginatingAddress();
                strMessage_Body =  msgs[i].getMessageBody() ;
            }
            // Log and display the SMS message.
            Log.d(TAG, strMessage_Sender +" , " + strMessage_Body);

            //trim the +964 prefix of the sender number
            if(!strMessage_Sender.startsWith("07")){

                strMessage_Sender = strMessage_Sender.substring(4 , strMessage_Sender.length());
                strMessage_Sender = "0" + strMessage_Sender;
            }

            strMessage_Sender = strMessage_Sender.replace(" ", "");
            Log.d(TAG, strMessage_Sender +" , " + strMessage_Body);

            //if(strMessage_Body.startsWith(SmsPrefix)){


                //Log.d(TAG, strMessage_Sender +" , " + strMessage_Body);

                //String decryptedSms = AES.decrypt(strMessage_Body.substring(3 , strMessage_Body.length()) , cipherKeyOfSms + strMessage_Sender + myPhoneNumber); //cipherKeyFromAdmin + myPhoneNumber + receiverPhoneNumber
                String decryptedSms = AES.decrypt(strMessage_Body , cipherKeyOfSms + strMessage_Sender + myPhoneNumber); //cipherKeyFromAdmin + myPhoneNumber + receiverPhoneNumber

                if(decryptedSms != null){


                    //check if the sender is allowed to request location from this user(have connection) from sqlite DB
                    MySqliteDB mySqliteDB = new MySqliteDB(context);

                    Log.d(TAG, strMessage_Sender +" , " + decryptedSms);

                    //if(decryptedSms.endsWith("true")){
                    String senderNumber = strMessage_Sender;
                    String senderName = null;

                    //check if the number is saved in contacts
                    String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
                    Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                    String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

                    ContentResolver contentResolver = myContext.getContentResolver();

                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, null, null, null);

                    while (phoneCursor.moveToNext()) {
                        String number = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)).replace(" ", "");
                        if
                        (
                                senderNumber.equals(number) ||
                                        ("00964"+senderNumber).equals(number) ||
                                        ("+964"+senderNumber).equals(number)
                        ) {
                            senderName = phoneCursor.getString(phoneCursor.getColumnIndex(DISPLAY_NAME));
                            break;
                        }
                    }
                    phoneCursor.close();


                    //save the received sms in the sqlite db
                    //String trimmedDecryptedSms = decryptedSms.substring(0  ,decryptedSms.length() - 4 );
                    String trimmedDecryptedSms = decryptedSms;
                    mySqliteDB.SaveReceivedSms(senderNumber , trimmedDecryptedSms  , "received" , 0 , 1);

                    //make notification for the new received sms
                    //make broadcast received by the activity that displays the sms
                    notification_broadcast_SmsReceived(senderName , senderNumber);
                    //}
                }
            //}
        }
    }






    //make notification and broadcast for the received sms
    private void notification_broadcast_SmsReceived(String senderName , String senderNumber) {
        String sender;
        if(senderName == null) sender = senderNumber;
        else sender = senderName;

        //send  broadcast intent to notify mainActivity that new sms received
        Intent intent1 = new Intent("newSmsReceived_SmsChatsActivity");
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent1);

        //send broadcast to the sender activity to show the sms
        Intent intent2 = new Intent("newSmsReceived_ContactChatActivity");
        intent2.putExtra("sender" , senderNumber);
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent2);

        //intent for notification pending intent
        Intent intent3 = new Intent(myContext , SmsChatsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (myContext , requestCode_SmsReceived , intent3 , PendingIntent.FLAG_CANCEL_CURRENT);

        String notificationTitle = "رسالة جديدة";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(myContext, "channel1")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentTitle(notificationTitle)
                .setSmallIcon(R.drawable.smscode)
                .setContentText(sender)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(myContext);
        // notificationId is a unique int for each notification that you must define

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}
