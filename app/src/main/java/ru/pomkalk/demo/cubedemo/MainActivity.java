package ru.pomkalk.demo.cubedemo;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    TextView tv;
    Button scanButton;
    String hash;

    int x,y,z;

    SensorManager sm;
    Sensor sn;

    long counter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode==1)&&(resultCode == RESULT_OK))
        {
            hash = data.getStringExtra("SCAN_RESULT");
            scanButton.setVisibility(View.INVISIBLE);

            sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            sn = (Sensor) sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, sn, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = (Button)findViewById(R.id.button);
        scanButton.setOnClickListener(this);
        tv = (TextView)findViewById(R.id.infoBox);
        counter = 0;
        x= 0;
        y=0;
        z=0;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button){
            try
            {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 1);
            }catch (Exception e)
            {
                Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                startActivity(marketIntent);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor s = event.sensor;
        if (s.getType() == Sensor.TYPE_ACCELEROMETER){
            int m = 20;
            int x = ((int)event.values[0])*m;
            int y = ((int)event.values[1])*m;
            int z = ((int)event.values[2])*m;
            String data = "0:0:"+y;
            tv.setText(data.replace(':','\n'));
            new SendData(hash, ++counter, data).execute();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }





    public class SendData extends AsyncTask<Void, Void, Void>{
        String data;
        long counter;
        String hash;

        public SendData(String hash, long counter, String data){
            this.hash = hash;
            this.counter = counter;
            this.data = data;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try
            {
                Log.d("MYLOG", "start request");
                String url = "http://cubedemo.pomkalk.ru/setup.php?hash="+hash+"&package="+counter+"&data="+data;
                Log.d("MYLOG", "REQ:"+url);
                URL u = new URL(url);
                HttpURLConnection http = (HttpURLConnection)u.openConnection();
                http.setRequestMethod("GET");

                Log.d("MYLOG", "Response code:" + http.getResponseCode());

                BufferedReader bf = new BufferedReader(new InputStreamReader(http.getInputStream()));
                String line = new String();
                StringBuffer sb = new StringBuffer();
                while ((line = bf.readLine())!=null)
                    sb.append(line);
                bf.close();
            }catch (Exception e){
                Log.d("MYLOG", "ERROR: " + e.getMessage());
            }

            return null;
        }
    }
}
