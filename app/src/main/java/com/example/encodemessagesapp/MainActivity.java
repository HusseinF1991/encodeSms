package com.example.encodemessagesapp;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSION_REQUEST_READ_CONTACTS = 98;
    public static final int MY_PERMISSION_REQUEST_Read_SMS = 97;
    public static final int MY_PERMISSION_REQUEST_SEND_SMS = 96;
    public static final int MY_PERMISSION_REQUEST_READ_PHONE_STATE = 95;
    public static final int MY_PERMISSION_REQUEST_RECEIVE_SMS = 94;
    public static final int MY_PERMISSION_For_5_REQUESTS = 90;

    private String myPhoneNumber;
    private int simId;

    private EditText etxtPhoneNumber;
    private EditText etxtConfirmPhoneNumber;
    private Spinner simIdSpinner;
    private Button btnRegister;

    private static String deviceId;

    private static String cipherOfActivationKey_withDate = "1234567891234561234567891212345612345678";  //imei(15) + phoneNumber(11) + cipherKeyOfSms(6) + Date(8)
    private static String cipherOfActivationKey_withoutDate = "12345678912345612345678912";   //imei(15) + phoneNumber(11)
    private static String cipherOfGeneratorKey = "98765432198765498765432198";   //imei(15) + phoneNumber(11)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        etxtPhoneNumber = findViewById(R.id.etxtPhoneNumber);
        etxtConfirmPhoneNumber = findViewById(R.id.etxtConfirmPhoneNumber);
        simIdSpinner = findViewById(R.id.simIdSpinner);
        btnRegister = findViewById(R.id.btnRegister);

        if(!CheckUserActivation()){
            //get permissions from the user
            Check5Permissions();

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    deviceId = telephonyManager.getImei();
                } else {
                    deviceId = telephonyManager.getDeviceId();
                }
            }


            GetSimCards();
            RegisterUser();
        }


    }



    //check if user activated or expired
    private boolean CheckUserActivation(){
        SharedPreferences sharedPref = this.getSharedPreferences("EncodeSmsSharedPrefereces", Context.MODE_PRIVATE);
        myPhoneNumber = sharedPref.getString("EncodeSmsPhone_SP", "notRegistered");
        simId = sharedPref.getInt("EncodeSmsSimId_SP", 0);
        String expDateS = sharedPref.getString("EncodeSmsCodeExpire_SP", "notRegistered");
        //sharedPref.edit().clear().commit();

        try {

            Log.d("CheckUserActivation" , expDateS);
            Date expDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(expDateS);

            Log.d("CheckUserActivation" , expDate.toString());
            if (myPhoneNumber != "notRegistered" && Calendar.getInstance().getTime().before(expDate)) {
                this.finish();
                Intent intent = new Intent(getBaseContext(), SmsChatsActivity.class);
                startActivity(intent);

                return true;
            }
            else if(myPhoneNumber != "notRegistered" && !Calendar.getInstance().getTime().before(expDate)){
                Toast.makeText(MainActivity.this , "لقد انتهت صلاحية المفتاح المدخل للتفعيل , يرجى ادخال مفتاح جديد..", Toast.LENGTH_LONG).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();

            Log.d("CheckUserActivation" , "true");
        }
        return false;
    }


    //get sim cards in the phone and populate them in spinner
    private void GetSimCards() {

        //get sim cards info
        final List<SimInfo> simInfoList = new ArrayList<>();
        Uri URI_TELEPHONY = Uri.parse("content://telephony/siminfo/");
        Cursor c = this.getContentResolver().query(URI_TELEPHONY, null, "sim_id != -1", null, null);


        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex("_id"));
                String display_name = c.getString(c.getColumnIndex("display_name"));
                String icc_id = c.getString(c.getColumnIndex("icc_id"));
                int sim_id = c.getInt(c.getColumnIndex("sim_id"));
                SimInfo simInfo = new SimInfo(id, display_name, icc_id, sim_id);
                Log.d("apipas_sim_info", simInfo.toString());
                simInfoList.add(simInfo);
            } while (c.moveToNext());
        }

        c.close();

        ArrayList<String> simList = new ArrayList<>();
        simList.add("اختر الشريحة");
        for (SimInfo simInfo : simInfoList) {
            simList.add(simInfo.getDisplay_name() + " " + simInfo.getSim_id());
        }

        ArrayAdapter<String> AgeSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, simList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                View view = super.getDropDownView(position, convertView, parent);
                TextView TV = (TextView) view;
                if (position == 0) {
                    TV.setTextColor(Color.GRAY);
                } else {
                    TV.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        AgeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        simIdSpinner.setAdapter(AgeSpinnerAdapter);
    }


    //register user
    private void RegisterUser() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etxtPhoneNumber.getText().toString().length() != 11) {
                    Toast.makeText(MainActivity.this, "رجاءا تأكد من ادخال رقم الهاتف الصحيح", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!etxtPhoneNumber.getText().toString().trim().equals(etxtConfirmPhoneNumber.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, "ادخل رقم الهاتف مرتين صحيحا", Toast.LENGTH_SHORT).show();
                    return;
                } else if (simIdSpinner.getSelectedItem().toString().equals("اختر الشريحة")) {
                    Toast.makeText(MainActivity.this, "اختر فتحة الشريحة لهذا الرقم", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("تأكيد")
                            .setMessage("هل انت متاكد من ادخالك الصحيح لرقم هاتفك وفتحة الشريحة" + "\n" + etxtPhoneNumber.getText().toString() + "\n" + simIdSpinner.getSelectedItem().toString())
                            .setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    simId = Integer.parseInt(simIdSpinner.getSelectedItem().toString().substring(simIdSpinner.getSelectedItem().toString().length() - 1));
                                    myPhoneNumber = etxtPhoneNumber.getText().toString();
                                    OpenDialogForRegCodeEntry();
                                }
                            })
                            .setNegativeButton("تعديل", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }


    //open dialog for user to enter the activation code
    private void OpenDialogForRegCodeEntry() {

        //generate the activation key
        StringXORer stringXORer = new StringXORer();
        final String activationKey = stringXORer.encode(deviceId + myPhoneNumber , cipherOfActivationKey_withoutDate);

        Log.d("OpenDialogForRegCodeEntry" , "ciphered activation key without date " +activationKey);
        Log.d("OpenDialogForRegCodeEntry" , "decrypted activation key " +stringXORer.decode(activationKey , cipherOfActivationKey_withoutDate));

        //for example : activation key has expiration date = 11/11/2019 , Cipher Key = HHHHHH
        //String exampleActivationKey = stringXORer.encode(deviceId + myPhoneNumber +"HHHHHH" + "11112019" , cipherOfActivationKey_withDate);
        //Log.d("OpenDialogForRegCodeEntry" , "encrypted activation key with date " +exampleActivationKey);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.registeration_code_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(view);

        final TextView txtKeyGeneratorCode = view.findViewById(R.id.txtKeyGeneratorCode);
        final EditText etxtActiviationKey = view.findViewById(R.id.etxtActiviationKey);
        Button btnConfirmRegistrationKey = view.findViewById(R.id.btnConfirmRegistrationKey);
        Button btnCancelRegistrationDialog = view.findViewById(R.id.btnCancelRegistrationDialog);
        Button btnSendKeyViaSms = view.findViewById(R.id.btnSendKeyViaSms);

        //encode first with xor
        String generatorKey = stringXORer.encode(deviceId + myPhoneNumber , cipherOfGeneratorKey);
        generatorKey = generatorKey.replaceAll(System.getProperty("line.separator"), "");
        txtKeyGeneratorCode.setText(generatorKey);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        txtKeyGeneratorCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(txtKeyGeneratorCode.getText());
                Toast.makeText(MainActivity.this, "تم نسخ المحتوى", Toast.LENGTH_SHORT).show();
            }
        });


        //send the generator by sms (start sms application intent)
        btnSendKeyViaSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendGeneratorCodeViaSms(txtKeyGeneratorCode.getText().toString());
            }
        });

        btnConfirmRegistrationKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etxtActiviationKey.getText().toString().length() > 25){

                    //decode it first with aes
                    StringXORer stringXORer = new StringXORer();
                    String resolvedActivationKey = stringXORer.decode(etxtActiviationKey.getText().toString() , cipherOfActivationKey_withDate);
                    Log.d("OpenDialogForRegCodeEntry" , "decryptedKeyWithDate "+resolvedActivationKey );

                    //get expiration date of the activation key
                    String expDate = resolvedActivationKey.substring(resolvedActivationKey.length()-8 , resolvedActivationKey.length());
                    String sDate =  expDate.substring(0 , 2) +"/"+ expDate.substring(2 , 4)+ "/" +expDate.substring(4 , expDate.length());
                    Log.d("OpenDialogForRegCodeEntry" ,"expDate " +sDate );

                    resolvedActivationKey = resolvedActivationKey.substring(0 , resolvedActivationKey.length() -8);
                    Log.d("OpenDialogForRegCodeEntry" , "decryptedKeyWithoutDate "+resolvedActivationKey );

                    String cipherKeyOfSms = resolvedActivationKey.substring(resolvedActivationKey.length()-6 , resolvedActivationKey.length());
                    Log.d("OpenDialogForRegCodeEntry" ,"cipherKeyOfSms " +cipherKeyOfSms );

                    resolvedActivationKey = resolvedActivationKey.substring(0 , resolvedActivationKey.length() -6);
                    Log.d("OpenDialogForRegCodeEntry" , "decryptedKeyWithoutDate&Cipher "+resolvedActivationKey );

                    //encode the key without exp date
                    String enteredKey_withoutExp =  stringXORer.encode(resolvedActivationKey , cipherOfActivationKey_withoutDate);
                    Log.d("OpenDialogForRegCodeEntry" , "keyWithoutDate "+enteredKey_withoutExp );
                    if(enteredKey_withoutExp.equals(activationKey)){


                        Date date = null;
                        try {
                            date =new SimpleDateFormat("dd/MM/yyyy").parse(sDate);
                            Log.d("OpenDialogForRegCodeEntry" ,"expDate " +date );

                            //check if the key is expired
                            if(Calendar.getInstance().getTime().before(date)){

                                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("EncodeSmsSharedPrefereces", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("EncodeSmsPhone_SP", myPhoneNumber);
                                editor.putInt("EncodeSmsSimId_SP", simId);
                                editor.putString("EncodeSmsCodeExpire_SP", String.valueOf(date));
                                editor.putString("EncodeSmsCipherKey_SP" , cipherKeyOfSms);
                                editor.commit();

                                finish();
                                Toast.makeText(MainActivity.this , "تم التسجيل في النظام بنجاح, تنتهي صلاحية المفتاح بتاريخ :" +sDate, Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(MainActivity.this , SmsChatsActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(MainActivity.this , "المفتاح المدخل منتهي الصلاحية , تاكد من ادخالك مفتاح صحيح" , Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this , "المفتاح غير صحيح , تاكد من ادخالك المفتاح صحيحا" , Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this , "المفتاح غير صحيح , تاكد من ادخالك المفتاح صحيحا" , Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this , "المفتاح غير صحيح , تاكد من ادخالك المفتاح صحيحا" , Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnCancelRegistrationDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }



    private  void SendGeneratorCodeViaSms(String generatorKey){

        // Create the intent.
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        // Set the data for the intent as the phone number.
        smsIntent.setData(Uri.parse("sms:"));
        // Add the message (sms) with the key ("sms_body").
        smsIntent.putExtra("sms_body", generatorKey);
        // If package resolves (target app installed), send intent.
        if (smsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(smsIntent);
        } else {
            Log.e("SendGeneratorCodeViaSms", "Can't resolve app for ACTION_SENDTO Intent");
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
                ActivityCompat.requestPermissions(MainActivity.this
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
                    return;
                } else {
                    /*new AlertDialog.Builder(this)
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
                            .show();*/
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

                        if(permissions[i].equals("android.permission.READ_PHONE_STATE")){

                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){

                                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
                                deviceId = telephonyManager.getDeviceId();
                                Log.d("deviceId", deviceId);
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
}


