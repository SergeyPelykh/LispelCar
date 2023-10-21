package com.example.lispelcar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.TextView;
import android.content.BroadcastReceiver;

public class MainActivity extends AppCompatActivity {
    public static final String START_SMS_DELIVERED_ACTION = "startDelivered";
    public static final String STOP_SMS_DELIVERED_ACTION = "stopDelivered";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String HEATER_NUMBER = "+79940710896";
    //+79940710896
    final int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatButton button = findViewById(R.id.button);
        TextView textView = findViewById(R.id.textView);
        AppCompatButton button2 = findViewById(R.id.button2);
        TextView textView2 = findViewById(R.id.textView2);
        button2.setEnabled(false);
        button.setEnabled(true);
        textView.setText("Отопитель на HONDA FREED");
        Uri smsUri = Uri.parse("content://sms/inbox");

        MutableLiveData<String> liveData = new MutableLiveData<>();


        liveData.observe(MainActivity.this, x -> {
            textView2.setText(x);
            if (x.contains("Ignition")) {
                textView2.setText("отопитель запущен");
                button.setEnabled(false);
                button2.setEnabled(true);
            } else if (x.contains("Blowing")) {
                textView2.setText("отопитель остановлен");
                button.setEnabled(true);
                button2.setEnabled(false);
            }
        });


        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getExtras();
                if (data != null) {
                    Object[] pdus = (Object[]) data.get("pdus");
                    SmsMessage smsMessage = null;
                    for (int i = 0; i < pdus.length; i++) {
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        if (smsMessage.getOriginatingAddress().equals(HEATER_NUMBER)) {
                            liveData.postValue(smsMessage.getMessageBody());
                        }
                    }
                }
            }
        }, new IntentFilter(SMS_RECEIVED));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                textView2.setText("отопитель получил команду на запуск");
                button.setEnabled(false);
                button2.setEnabled(false);
            }
        }, new IntentFilter(START_SMS_DELIVERED_ACTION));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                textView2.setText("отопитель получил команду на остановку");
                button2.setEnabled(false);
                button.setEnabled(false);
            }
        }, new IntentFilter(STOP_SMS_DELIVERED_ACTION));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    // request permission (see result in onRequestPermissionsResult() method)
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            123);
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECEIVE_SMS},
                            1233);
                } else {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(HEATER_NUMBER, null, "*1.P1,E1,T40#", null, PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(START_SMS_DELIVERED_ACTION), flag));
                    textView2.setText("отопителю отправлен запрос на запуск");
                    button.setEnabled(false);
                    button2.setEnabled(false);
                }

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    // request permission (see result in onRequestPermissionsResult() method)
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS},
                            123);
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECEIVE_SMS},
                            1233);
                } else {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(HEATER_NUMBER, null, "*3.P1#", null, PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(STOP_SMS_DELIVERED_ACTION), flag));
                    textView2.setText("отопителю отправлен запрос на остановку");
                    button.setEnabled(false);
                    button2.setEnabled(false);
                }
            }

        });


    }

}