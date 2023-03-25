package com.example.encodemessagesapp;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ContactChatAdapter extends ArrayAdapter implements View.OnClickListener{

    private Context context;
    private int resource;
    private ArrayList<ContactChatModel> contactChatArrayList;
    //private static String SmsPrefix = "WOW";

    public ContactChatAdapter(@NonNull Context context, int resource, @NonNull ArrayList objects) {
        super(context, resource, objects);

        this.context = context;
        this.resource = resource;
        this.contactChatArrayList = objects;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.btnSmsDeliveryFailed:
                final int smsId1 = (Integer) v.getTag(R.string.sms_Id_ContactChatAdapter);
                final ViewHolder holder1 = (ViewHolder) v.getTag();

                holder1.btnSmsDeliveryFailed  = v.findViewById(R.id.btnSmsDeliveryFailed);

                final MySqliteDB mySqliteDB = new MySqliteDB(context);

                // SMS sent pending intent
                PendingIntent sentIntent = PendingIntent.getBroadcast(context, 15,
                        new Intent("SMS_SENT_ACTION"), 0);

                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        int resultCode = getResultCode();
                        switch (resultCode) {
                            case Activity.RESULT_OK:
                                Toast.makeText(context , "تم ارسال الرسالة" , Toast.LENGTH_SHORT).show();

                                mySqliteDB.UpdateDeliveredStatus(smsId1);

                                holder1.txtMessageBody.setTextColor(Color.BLACK);
                                holder1.btnSmsDeliveryFailed.clearAnimation();
                                holder1.btnSmsDeliveryFailed.setVisibility(View.INVISIBLE);

                                break;
                            default:
                                Toast.makeText(context , "فشل في ارسال الرسالة" , Toast.LENGTH_SHORT).show();

                                holder1.btnSmsDeliveryFailed.clearAnimation();
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

                Cursor c = mySqliteDB.GetOneSms(smsId1);
                if(c.getCount() > 0 ) {
                    String message = null;
                    String phoneNumber = null;
                    if (c.moveToFirst()) {
                        do {
                            message = c.getString(c.getColumnIndex("message"));
                            phoneNumber = c.getString(c.getColumnIndex("phoneNumber"));
                        } while (c.moveToNext());
                    }
                    c.close();

                    SharedPreferences sharedPref = context.getSharedPreferences("EncodeSmsSharedPrefereces", Context.MODE_PRIVATE);
                    String myPhoneNumber = sharedPref.getString("EncodeSmsPhone_SP", "notRegistered");
                    int simId = sharedPref.getInt("EncodeSmsSimId_SP", 0);
                    String cipherKeyOfSms = sharedPref.getString("EncodeSmsCipherKey_SP" , "HHHHHH");

                    //start rotate the resend image
                    Animation aniRotate = AnimationUtils.loadAnimation(context ,R.anim.rotateclockwise);
                    holder1.btnSmsDeliveryFailed.startAnimation(aniRotate);

                    //encrypt before resend
                    String smsBody_Encrypted = AES.encrypt(message , cipherKeyOfSms + myPhoneNumber + phoneNumber);//cipherKeyFromAdmin +myPhoneNumber+  receiverPhoneNumber
                    //smsBody_Encrypted = SmsPrefix + smsBody_Encrypted;

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.getSmsManagerForSubscriptionId(simId).sendTextMessage(phoneNumber, null, smsBody_Encrypted, sentIntent, null);
                }
                break;

            case R.id.txtMessageBody:
                final int smsId = (Integer) v.getTag(R.string.sms_Id_ContactChatAdapter);
                final ViewHolder holder = (ViewHolder) v.getTag();


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("تحذير")
                        .setMessage("هل ترغب بحذف الرسالة")
                        .setPositiveButton("حذف", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MySqliteDB mySqliteDB = new MySqliteDB(context);
                                mySqliteDB.DeleteOneSms(smsId);

                                holder.txtMessageBody  = v.findViewById(R.id.txtMessageBody);
                                holder.txtMessageBody.setText("الرسالة محذوفة");
                                holder.txtMessageBody.setTypeface(holder.txtMessageBody.getTypeface(), Typeface.BOLD_ITALIC);

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("الغاء", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
        }
    }


    class ViewHolder{
        TextView txtMessageBody;
        TextView txtMessageTime;
        Button btnSmsDeliveryFailed;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView;
        ViewHolder holder;
        ContactChatModel contactChatModel = contactChatArrayList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();

            holder.txtMessageTime = convertView.findViewById(R.id.txtMessageTime);
            holder.txtMessageBody = convertView.findViewById(R.id.txtMessageBody);
            holder.btnSmsDeliveryFailed = convertView.findViewById(R.id.btnSmsDeliveryFailed);

            itemView = convertView;
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            itemView = convertView;
        }

        try {
            Date smsDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(contactChatModel.getSmsTime());
            //Date smsDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(contactChatModel.getSmsTime());

            String day          = (String) DateFormat.format("dd",   smsDate);
            String month  = (String) DateFormat.format("MM",   smsDate);
            String year         = (String) DateFormat.format("yyyy", smsDate);
            String minute  = (String) DateFormat.format("mm",   smsDate);
            String hour         = (String) DateFormat.format("HH", smsDate);
            String sSmsDate = year + "-" + month + "-" + day + " " + hour + ":" + minute;

            holder.txtMessageTime.setText(contactChatModel.getSmsTime());
            holder.txtMessageBody.setText(contactChatModel.getSmsBody());

            if (contactChatModel.getSent_received().equals("sent")){

                holder.txtMessageBody.setBackgroundResource(R.drawable.shape_bg_outgoing_bubble);
                holder.txtMessageBody.setPadding(10 , 3 , 60 , 3);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.addRule(RelativeLayout.BELOW, R.id.txtMessageTime);
                Resources r = context.getResources();

                int leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,100 ,r.getDisplayMetrics());
                int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5 ,r.getDisplayMetrics());
                int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,3 ,r.getDisplayMetrics());

                params.setMargins(leftMargin, topMargin, rightMargin, 0);
                holder.txtMessageBody.setLayoutParams(params);

                if(contactChatModel.getDelivered() == 0){
                    holder.txtMessageBody.setTextColor(Color.RED);
                    holder.btnSmsDeliveryFailed.setVisibility(View.VISIBLE);

                    holder.btnSmsDeliveryFailed.setTag(R.string.sms_Id_ContactChatAdapter , contactChatModel.getSmsId());
                    holder.btnSmsDeliveryFailed.setTag(holder);
                    holder.btnSmsDeliveryFailed.setOnClickListener(this);
                }else{
                    holder.btnSmsDeliveryFailed.setVisibility(View.INVISIBLE);
                }

            }
            else{

                holder.txtMessageBody.setBackgroundResource(R.drawable.shape_bg_incoming_bubble);
                holder.txtMessageBody.setPadding(60 , 3 , 10 , 3);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.addRule(RelativeLayout.BELOW, R.id.txtMessageTime);
                Resources r = context.getResources();

                int leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,3 ,r.getDisplayMetrics());
                int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5 ,r.getDisplayMetrics());
                int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,100 ,r.getDisplayMetrics());

                params.setMargins(leftMargin, topMargin, rightMargin, 0);
                holder.txtMessageBody.setLayoutParams(params);

                holder.btnSmsDeliveryFailed.setVisibility(View.INVISIBLE);
            }

            holder.txtMessageBody.setTag(R.string.sms_Id_ContactChatAdapter , contactChatModel.getSmsId());
            holder.txtMessageBody.setTag(holder);
            holder.txtMessageBody.setOnClickListener(this);

        } catch (ParseException e) {
            e.printStackTrace();
        }


        return itemView;
    }
}
