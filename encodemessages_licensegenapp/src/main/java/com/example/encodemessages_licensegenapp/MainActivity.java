package com.example.encodemessages_licensegenapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSION_REQUEST_READ_PHONE_STATE = 88;

    private Spinner spinKeyExp_Year, spinKeyExp_Month, spinKeyExp_Day;
    private EditText etxtGeneratorKey;
    private TextView txtActivationKey, txtRemainedLicenses ,etxtCipherKeyOfSms;
    private Button btnGenerateKey;
    private Button btnActivateApplication;
    private Button btnSendKeyViaSms;

    int remainedLicenses;
    private String deviceId;
    private AlertDialog activationDialog;

    //activating the encodeMessagesApp keys
    private static String cipherOfActivationKey_withDate = "1234567891234561234567891212345612345678";   // imei(15) + phoneNumber(11) + cipherKeyOfSms(6) +Date(8)
    private static String cipherOfActivationKey_withoutDate = "12345678912345612345678912";   // imei(15) + phoneNumber(11)
    private static String cipherOfGeneratorKey = "98765432198765498765432198";  // imei(15) + phoneNumber(11)

    //activating the encodeMessages_licenseGenApp keys
    private static String cipherOfImei_LicenseGenApp = "1122334455667788"; //imei
    private static String cipherOfActivationKey_LicenseGenApp = "99887766554433221100998876"; //imei(15) + currentDate(8) + noOfLicenses(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinKeyExp_Year = findViewById(R.id.spinKeyExp_Year);
        spinKeyExp_Month = findViewById(R.id.spinKeyExp_Month);
        spinKeyExp_Day = findViewById(R.id.spinKeyExp_Day);
        etxtGeneratorKey = findViewById(R.id.etxtGeneratorKey);
        etxtCipherKeyOfSms = findViewById(R.id.etxtCipherKeyOfSms);
        txtActivationKey = findViewById(R.id.txtActivationKey);
        btnGenerateKey = findViewById(R.id.btnGenerateKey);
        btnSendKeyViaSms = findViewById(R.id.btnSendKeyViaSms);

        btnActivateApplication = findViewById(R.id.btnActivateApplication);
        txtRemainedLicenses = findViewById(R.id.txtRemainedLicenses);


        PopulateYears_InSpinner();
        PopulateMonths_InSpinner();
        PopulateDays_InSpinner();

        GenerateActivationKey();

        CheckLicensesRemained();

        EnterNewLicenses();

        MakeTextViewsCopied();

        SendGeneratorCodeViaSms();
    }


    private void MakeTextViewsCopied(){
        txtActivationKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(txtActivationKey.getText());
                Toast.makeText(MainActivity.this, "تم نسخ المحتوى", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void CheckLicensesRemained() {
        SharedPreferences sharedPref = this.getSharedPreferences("EncodeSms_LicensesGenPreferences", Context.MODE_PRIVATE);
        remainedLicenses = sharedPref.getInt("EncodeSms_LicensesGen_RemainedLicenses_SP", 0);

        txtRemainedLicenses.setText(remainedLicenses + "");

        if (remainedLicenses == 0) {
            spinKeyExp_Year.setEnabled(false);
            spinKeyExp_Month.setEnabled(false);
            spinKeyExp_Day.setEnabled(false);
            etxtGeneratorKey.setEnabled(false);
            txtActivationKey.setEnabled(false);
            btnGenerateKey.setEnabled(false);

            btnActivateApplication.setVisibility(View.VISIBLE);
        } else {

            btnActivateApplication.setVisibility(View.INVISIBLE);
        }
    }


    private void PopulateYears_InSpinner() {
        String[] yearArray = new String[]{"السنة", "2019", "2020", "2021", "2022", "2023"};
        ArrayList<String> yearArrayList = new ArrayList<>(Arrays.asList(yearArray));

        ArrayAdapter<String> yearSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, yearArrayList) {
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

        yearSpinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinKeyExp_Year.setAdapter(yearSpinnerAdapter);
    }


    private void PopulateMonths_InSpinner() {
        String[] monthArray = new String[]{"الشهر", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        ArrayList<String> monthArrayList = new ArrayList<>(Arrays.asList(monthArray));

        ArrayAdapter<String> monthSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, monthArrayList) {
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

        monthSpinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinKeyExp_Month.setAdapter(monthSpinnerAdapter);
    }


    private void PopulateDays_InSpinner() {
        String[] dayArray = new String[]{"اليوم", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"
                , "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
        ArrayList<String> dayArrayList = new ArrayList<>(Arrays.asList(dayArray));

        ArrayAdapter<String> daySpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, dayArrayList) {
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

        daySpinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinKeyExp_Day.setAdapter(daySpinnerAdapter);
    }


    private void GenerateActivationKey() {
        btnGenerateKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etxtGeneratorKey.getText().toString().trim().length() < 10) {
                    Toast.makeText(MainActivity.this, "ادخل مفتاح التوليد رجاءا", Toast.LENGTH_SHORT).show();
                    return;

                } else if (spinKeyExp_Year.getSelectedItem().toString().equals("السنة") ||
                        spinKeyExp_Month.getSelectedItem().toString().equals("الشهر") ||
                        spinKeyExp_Day.getSelectedItem().toString().equals("اليوم")) {

                    Toast.makeText(MainActivity.this, "ادخل تاريخ نفاذ صلاحية المفتاح رجاءا", Toast.LENGTH_SHORT).show();
                    return;

                } else if(etxtCipherKeyOfSms.getText().toString().replace(" ", "").length() != 6){

                    Toast.makeText(MainActivity.this, "رمز التشفير الرسائل يجب ان يكون 6 مراتب رجاءا", Toast.LENGTH_SHORT).show();
                    return;

                } else {

                    StringXORer stringXORer = new StringXORer();
                    String decoded_generatorKey = stringXORer.decode(etxtGeneratorKey.getText().toString(), cipherOfGeneratorKey);

                    String expYear = spinKeyExp_Year.getSelectedItem().toString();
                    String expMonth = spinKeyExp_Month.getSelectedItem().toString();
                    String expDay = spinKeyExp_Day.getSelectedItem().toString();
                    String cipherKeyOfSms = etxtCipherKeyOfSms.getText().toString();
                    String decoded_ActivationKey = decoded_generatorKey + cipherKeyOfSms + expDay + expMonth + expYear ;

                    String activationKey = stringXORer.encode(decoded_ActivationKey, cipherOfActivationKey_withDate);
                    activationKey = activationKey.replaceAll(System.getProperty("line.separator"), "");

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    final String finalActivationKey = activationKey;

                    builder.setTitle("تنويه")
                            .setMessage("سيتم توليد مفتاح جديد وطرح من المفاتيح المتبقية في نظامك, هل انت متاكد من المعلومات المدخلة؟")
                            .setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {


                                    txtActivationKey.setText(finalActivationKey);

                                    //subtract one licenses from the remained licenses
                                    remainedLicenses--;
                                    txtRemainedLicenses.setText(remainedLicenses+"");

                                    SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("EncodeSms_LicensesGenPreferences", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("EncodeSms_LicensesGen_RemainedLicenses_SP", remainedLicenses);
                                    editor.commit();

                                    if (remainedLicenses == 0) {

                                        spinKeyExp_Year.setEnabled(false);
                                        spinKeyExp_Month.setEnabled(false);
                                        spinKeyExp_Day.setEnabled(false);
                                        etxtGeneratorKey.setEnabled(false);
                                        btnGenerateKey.setEnabled(false);

                                        btnActivateApplication.setVisibility(View.VISIBLE);
                                    }
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
                }
            }
        });
    }



    private  void SendGeneratorCodeViaSms(){
        btnSendKeyViaSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtActivationKey.getText().toString().equals("")){
                    // Create the intent.
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                    // Set the data for the intent as the phone number.
                    smsIntent.setData(Uri.parse("sms:"));
                    // Add the message (sms) with the key ("sms_body").
                    smsIntent.putExtra("sms_body", txtActivationKey.getText().toString());
                    // If package resolves (target app installed), send intent.
                    if (smsIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(smsIntent);
                    } else {
                        Log.e("SendGeneratorCodeViaSms", "Can't resolve app for ACTION_SENDTO Intent");
                    }
                }else{
                    Toast.makeText(MainActivity.this , "انشئ رمز اولا" , Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void EnterNewLicenses() {
        btnActivateApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.activate_application_dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialogView);

                //activation dialog widgets
                final TextView txtKeyGeneratorCode_Dialog = dialogView.findViewById(R.id.txtKeyGeneratorCode);
                final EditText etxtActiviationKey_Dialog = dialogView.findViewById(R.id.etxtActiviationKey);
                Button btnConfirmActivationKey_Dialog = dialogView.findViewById(R.id.btnConfirmActivationKey);
                Button btnCancelActivationDialog = dialogView.findViewById(R.id.btnCancelActivationDialog);

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(MainActivity.this.TELEPHONY_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        deviceId = telephonyManager.getImei();
                    } else {
                        deviceId = telephonyManager.getDeviceId();
                    }

                    final StringXORer stringXORer = new StringXORer();
                    String cipheredImei = stringXORer.encode(deviceId, cipherOfImei_LicenseGenApp);
                    txtKeyGeneratorCode_Dialog.setText(cipheredImei);

                    //copy generator key on clicking the TextView
                    txtKeyGeneratorCode_Dialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                            cm.setText(txtKeyGeneratorCode_Dialog.getText());
                            Toast.makeText(MainActivity.this, "تم نسخ المحتوى", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //confirm the entered activation key on clicking the button
                    btnConfirmActivationKey_Dialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (etxtActiviationKey_Dialog.getText().toString().equals("")) {
                                Toast.makeText(MainActivity.this, "ادخل مفتاح التفعيل رجاءا", Toast.LENGTH_SHORT).show();
                            } else {
                                String enteredActivationKey = etxtActiviationKey_Dialog.getText().toString();
                                String decodedActivationKey = stringXORer.decode(enteredActivationKey, cipherOfActivationKey_LicenseGenApp);
                                if (decodedActivationKey.length() != 26) {

                                    Toast.makeText(MainActivity.this, "المفتاح المدخل غير صحيح", Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        Log.d("activationKey" , decodedActivationKey + " , " + decodedActivationKey.length());

                                        String imeiFromEnteredKey = decodedActivationKey.substring(0, 15);
                                        Log.d("activationKey" , imeiFromEnteredKey + " , " + imeiFromEnteredKey.length());

                                        String enteredKeyDate = decodedActivationKey.substring(15, 23);
                                        Log.d("activationKey" , enteredKeyDate + " , " + enteredKeyDate.length());
                                        String currentDay = (String) DateFormat.format("dd", Calendar.getInstance().getTime());
                                        String currentMonth = (String) DateFormat.format("MM", Calendar.getInstance().getTime());
                                        String currentYear = (String) DateFormat.format("yyyy", Calendar.getInstance().getTime());
                                        Date currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(currentYear + "-" + currentMonth + "-" + currentDay);

                                        String KeyDay = enteredKeyDate.substring(0, 2);
                                        String KeyMonth = enteredKeyDate.substring(2, 4);
                                        String KeyYear = enteredKeyDate.substring(4, enteredKeyDate.length());
                                        Date keyDate = new SimpleDateFormat("yyyy-MM-dd").parse(KeyYear + "-" + KeyMonth + "-" + KeyDay);

                                        String noOfLicensesInKey = decodedActivationKey.substring(23, decodedActivationKey.length());
                                        Log.d("activationKey" , noOfLicensesInKey + " , " + noOfLicensesInKey.length());

                                        if (!imeiFromEnteredKey.equals(deviceId)) {

                                            Toast.makeText(MainActivity.this, "المفتاح المدخل غير صحيح", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (currentDate.after(keyDate)) {

                                            Toast.makeText(MainActivity.this, "المفتاح المدخل منتهي الصلاحية", Toast.LENGTH_SHORT).show();
                                        }
                                        else{

                                            remainedLicenses = Integer.parseInt(noOfLicensesInKey);
                                            txtRemainedLicenses.setText(remainedLicenses+"");

                                            SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("EncodeSms_LicensesGenPreferences", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putInt("EncodeSms_LicensesGen_RemainedLicenses_SP", remainedLicenses);
                                            editor.commit();

                                            if (remainedLicenses > 0) {

                                                spinKeyExp_Year.setEnabled(true);
                                                spinKeyExp_Month.setEnabled(true);
                                                spinKeyExp_Day.setEnabled(true);
                                                etxtGeneratorKey.setEnabled(true);
                                                btnGenerateKey.setEnabled(true);
                                                txtActivationKey.setEnabled(true);

                                                btnActivateApplication.setVisibility(View.INVISIBLE);
                                            }

                                            Toast.makeText(MainActivity.this, "تم التفعيل بنجاح", Toast.LENGTH_SHORT).show();
                                            activationDialog.dismiss();
                                        }

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        Toast.makeText(MainActivity.this, "المفتاح المدخل غير صحيح", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                        }
                    });

                    btnCancelActivationDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activationDialog.dismiss();
                        }
                    });

                    activationDialog = builder.create();
                    activationDialog.show();
                } else {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE}
                            , MY_PERMISSION_REQUEST_READ_PHONE_STATE);
                }

            }
        });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {

                    Toast.makeText(this, "نعتذر, لا يمكنك تفعيل البرنامج اذا لم تسمح للبرنامج بالوصول الى حالة الهاتف", Toast.LENGTH_LONG).show();
                    if (activationDialog != null && activationDialog.isShowing()) {
                        activationDialog.dismiss();
                    }
                }
                break;
        }
    }
}


class StringXORer {

    public String encode(String s, String key) {
        return Base64.encodeToString(xorWithKey(s.getBytes(), key.getBytes()), Base64.DEFAULT);
    }

    public String decode(String s, String key) {
        return new String(xorWithKey(Base64.decode(s, Base64.DEFAULT), key.getBytes()));
    }

    private byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }
}