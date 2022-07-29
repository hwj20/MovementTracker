package com.example.movementtracker;

import com.example.movementtracker.UploadUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    // internet set
    private String requestUrl = "http://10.252.135.198/";
    private int port = 8000;

    private SensorManager mSensorManager;
    private Sensor sensor_linear_acc, sensor_gyroscope;
    private Display mDisplay;
    private WindowManager mWindowManager;
    private TextView textViewMsg;
    private boolean frozen;
    private int delay_times = 100;
    private Button buttonStart;
    private String ip_address;
    private EditText editTextIP;
    private EditText editTextPort;
    private EditText editTextDelay;

    //TODO 加个UI填IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) !=  PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 2);
        }

        // lock down
        frozen = true;

        SharedPreferences ip_info = getSharedPreferences("enableInfo", MODE_PRIVATE);
        ip_address = ip_info.getString("ip_address","0.0.0.0");
        port = ip_info.getInt("port",8000);
        delay_times = ip_info.getInt("delay_multiple", 100);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        // linear acceleration
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_linear_acc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensor_gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        textViewMsg = (TextView) findViewById(R.id.textViewMsg);

        editTextIP = (EditText) findViewById(R.id.editTextNumberIP);
        editTextIP.setText(ip_address.toString());
        editTextPort = (EditText) findViewById(R.id.editTextNumberPort);
        editTextPort.setText(String.valueOf(port));
        editTextDelay = (EditText) findViewById(R.id.editTextDelay);
        editTextDelay.setText(String.valueOf(delay_times));

        buttonStart = (Button) findViewById(R.id.buttonStart) ;
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save input
                ip_address = editTextIP.getText().toString();
                port = Integer.parseInt(editTextPort.getText().toString());
                delay_times = Integer.parseInt(editTextDelay.getText().toString());
                SharedPreferences.Editor editor = ip_info.edit();
                editor.putInt("port",port);
                editor.putString("ip_address", ip_address);
                editor.putInt("delay_multiple", delay_times);
                editor.apply();

                frozen = !frozen;
                if(frozen){
                    buttonStart.setText("开始");
                }else {
                    buttonStart.setText("停止");
                }
            }
        });

        SensorEventListener sensorEventListener = new UpdateSensorEventListener();
        mSensorManager.registerListener(sensorEventListener, sensor_gyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(sensorEventListener, sensor_linear_acc, SensorManager.SENSOR_DELAY_GAME);
        textViewMsg.setText("registered listener successfully!");

    }
    class UpdateSensorEventListener implements SensorEventListener{
        private float mSensorX, mSensorY, mSensorZ;
        private String msg = new String("");
        private int delay = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            // for frozen test
            if(frozen)
                return;
            if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION
                    && event.sensor.getType() != Sensor.TYPE_GYROSCOPE)
                return;

            if (delay < delay_times){
                delay++;
                return;
            }
            else{
                delay = 0;
            }


            final long now = System.currentTimeMillis();
            /*
            screen upwards
            move left--- x+ right---x-
            move forward---y- backward---y+
            move upward ---z-  downward---z+
             */
            mSensorX = event.values[0];
            mSensorY = event.values[1];
            mSensorZ = event.values[2];

            /*
            in csv line format
             */
            msg = "";
            msg += String.valueOf(event.sensor.getType())+",";
            msg += String.valueOf(now)+",";
            msg += String.valueOf(mSensorX)+",";
            msg += String.valueOf(mSensorY)+",";
            msg += String.valueOf(mSensorZ)+"\n";

            requestUrl = "http://"+ip_address+"/";

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    UploadUtils.uploadMessage(msg,requestUrl,port);
                }
            });
            thread.start();
            textViewMsg.setText(msg);
            // lock down
//            frozen = true;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}