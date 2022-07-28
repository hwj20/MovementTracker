package com.example.movementtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private SensorManager mSensorManager;
    private Sensor sensor;
    private Display mDisplay;
    private WindowManager mWindowManager;
    private TextView textViewMsg;
    private boolean frozen;
    private Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        // linear acceleration
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        textViewMsg = (TextView) findViewById(R.id.textViewMsg);
        buttonStart = (Button) findViewById(R.id.buttonStart) ;
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frozen = false;
            }
        });

        SensorEventListener sensorEventListener = new UpdateSensorEventListener();
        mSensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        textViewMsg.setText("registered listener");

    }
    class UpdateSensorEventListener implements SensorEventListener{
        private float mSensorX, mSensorY, mSensorZ;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION)
                return;
            final long now = System.currentTimeMillis();
            if(frozen)
                return;
            /*
            screen upwards
            move left--- x+ right---x-
            move forward---y- backward---y+
            move upward ---z-  downward---z+
             */
            mSensorX = event.values[0];
            mSensorY = event.values[1];
            mSensorZ = event.values[2];

            String msg = new String("");
            msg += String.valueOf(now)+";";
            msg += String.valueOf(mSensorX)+";";
            msg += String.valueOf(mSensorY)+";";
            msg += String.valueOf(mSensorZ)+";";
            textViewMsg.setText(msg);

            // lock down
            frozen = true;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}