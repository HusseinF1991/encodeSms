package com.example.encodemessages_licensegenactivator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText etxtNoOfLicenses;
    private EditText etxtGeneratorKey;
    private Button btnGenerateKey;
    private TextView txtActivationKey;


    private static String cipherOfImei_LicenseGenApp = "1122334455667788";  //imei
    private static String cipherOfActivationKey_LicenseGenApp = "99887766554433221100998876";//imei(15) + currentDate(8) + noOfLicenses(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        etxtGeneratorKey = findViewById(R.id.etxtGeneratorKey);
        etxtNoOfLicenses = findViewById(R.id.etxtNoOfLicenses);
        btnGenerateKey = findViewById(R.id.btnGenerateKey);
        txtActivationKey = findViewById(R.id.txtActivationKey);


        txtActivationKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(txtActivationKey.getText());
                Toast.makeText(MainActivity.this, "تم نسخ المحتوى", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerateKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etxtNoOfLicenses.getText().toString().equals("") || etxtNoOfLicenses.getText().toString().equals("0")){
                    Toast.makeText(MainActivity.this , "ادخل عدد المفاتيح رجاءا" , Toast.LENGTH_SHORT).show();
                }
                else if(etxtGeneratorKey.getText().toString().trim().equals("")){
                    Toast.makeText(MainActivity.this , "ادخل مفتاح التوليد رجاءا" , Toast.LENGTH_SHORT).show();
                }
                else{
                    StringXORer stringXORer = new StringXORer();
                    String decodedGeneratorKey = stringXORer.decode(etxtGeneratorKey.getText().toString() , cipherOfImei_LicenseGenApp);

                    if(decodedGeneratorKey.length() != 15){
                        Toast.makeText(MainActivity.this , "مفتاح التوليد المدخل غير صحيح رجاءا" , Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String currentDay = (String) DateFormat.format("dd", Calendar.getInstance().getTime());
                        String currentMonth = (String) DateFormat.format("MM", Calendar.getInstance().getTime());
                        String currentYear = (String) DateFormat.format("yyyy", Calendar.getInstance().getTime());

                        String noOfLicenses = etxtNoOfLicenses.getText().toString();
                        if(noOfLicenses.length() == 1) noOfLicenses = "00" + noOfLicenses;
                        else if(noOfLicenses.length() == 2) noOfLicenses = "0" + noOfLicenses;

                        String activationKey = decodedGeneratorKey + currentDay + currentMonth + currentYear + noOfLicenses;
                        String encodedActivationKey = stringXORer.encode(activationKey , cipherOfActivationKey_LicenseGenApp);

                        txtActivationKey.setText(encodedActivationKey);
                    }
                }
            }
        });
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