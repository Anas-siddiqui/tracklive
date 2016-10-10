package cloud.artik.example.hellocloud;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.model.MessageAction;
import cloud.artik.model.MessageIDEnvelope;
import cloud.artik.model.UserEnvelope;

public class human_service extends Service implements LocationListener,SensorEventListener {
    public human_service() {
    }
    private SensorManager mSensorManager;

    private static human_service our = new human_service();

    private Sensor mStepDetectorSensor,mHeartRateSensor;
    private Context context=null;
    public Geocoder cls;
    private static final String TAG = "Activity";


    public static  String DEVICE_ID ;


int count=0;
    private UsersApi mUsersApi = null;
    private MessagesApi mMessagesApi = null;

    private String mAccessToken;
    public static double lat, longi,sp;
    int heart_rate=0;

    public static boolean status = false;
    public static human_service getInstance() {
        return our;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
         status=true;


        mAccessToken = intent.getStringExtra("token");
        Toast.makeText(this, "Started in background", Toast.LENGTH_LONG).show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //     new LongOperation().execute("");
        context=this;
        notification();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);


       mHeartRateSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_HEART_RATE);
      mSensorManager.registerListener(this, mStepDetectorSensor , SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeartRateSensor , SensorManager.SENSOR_DELAY_NORMAL);
       //SETTING ALL THE SENSORS FOR LISTEN TO NEW CHANGES

        setupArtikCloudApi();//setup artik to send data

        getUserInfo();

        everysecond();


        return START_STICKY;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void everysecond() {
        handler.postDelayed(new Runnable() {
            public void run() {
                try {

                    String loc=fun(lat,longi);
                    //gets location by geocoding from json

                    postMsg(loc,count,heart_rate);
                    //send data to artik cloud



                } catch (Exception e) {
                    return;
                }


                handler.postDelayed(this, 30000);
            }
        }, 30000);
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        lat = location.getLatitude();

        longi = location.getLongitude();
        sp= location.getSpeed();


    }

   public static Handler handler;

    public void onCreate() {
        // Handler will get associated with the current thread,
        // which is the main thread.
        handler = new Handler();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        status=false;
        stopSelf();
        handler.removeCallbacksAndMessages(null);


    }

    void notification() {
        Intent intent2 = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent2, 0);
        Uri g = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification n = new Notification.Builder(this)
                .setContentTitle("Track live")
                .setTicker("Walk mode initiated")
                .setContentText("Walk mode initiated")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setSound(g)

                .build();

        startForeground(13376166,
                n);
    }

    private void setupArtikCloudApi() {
        ApiClient mApiClient = new ApiClient();
        mApiClient.setAccessToken(mAccessToken);
        mApiClient.setDebugging(true);

        mUsersApi = new UsersApi(mApiClient);
        mMessagesApi = new MessagesApi(mApiClient);
    }

    private void getUserInfo() {
        final String tag = TAG + " getSelfAsync";
        try {
            mUsersApi.getSelfAsync(new ApiCallback<UserEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int statusCode, Map<String, List<String>> map) {
                    processFailure(tag, exc);
                }

                @Override
                public void onSuccess(UserEnvelope result, int statusCode, Map<String, List<String>> map) {
                    Log.v(TAG, "getSelfAsync::setupArtikCloudApi self name = " + result.getData().getFullName());
                    status = true;

                }

                @Override
                public void onUploadProgress(long bytes, long contentLen, boolean done) {
                }

                @Override
                public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                }
            });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;
        int value = -1;


        if (values.length > 0) {
            value = (int) values[0];
        }

       if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // For test only. Only allowed value is 1.0 i.e. for step taken
         count++;

        }
        if(sensor.getType()==Sensor.TYPE_HEART_RATE) {
          heart_rate= (int) event.values[0];

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void processFailure(final String context, ApiException exc) {
        String errorDetail = " onFailure with exception" + exc;
        Log.w(context, errorDetail);
        exc.printStackTrace();
        //    showErrorOnUIThread(context+errorDetail, ge);
    }

    private void postMsg(String location,int step_count,int rate_heart) {
        final String tag = TAG + " sendMessageActionAsync";

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("location", location);
        data.put("step_count", step_count);
        data.put("heartRate", rate_heart);


        MessageAction msg = new MessageAction();
        msg.setSdid(DEVICE_ID);
        msg.setData(data);

        try {
            mMessagesApi.sendMessageActionAsync(msg, new ApiCallback<MessageIDEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                    processFailure(tag, exc);
                }

                @Override
                public void onSuccess(MessageIDEnvelope result, int i, Map<String, List<String>> stringListMap) {
                    Log.v(tag, " onSuccess response to sending message = " + result.getData().toString());

                }

                @Override
                public void onUploadProgress(long bytes, long contentLen, boolean done) {
                }

                @Override
                public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                }
            });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }

    public void msg(String a) {
        Toast.makeText(human_service.this, a,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }


    //send data to google map api and get json response
    String fun(double lat1,double lng1) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng="+lat1+","+lng1+"&sensor=true");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            JSONObject jsonObject = new JSONObject(buffer.toString());
            JSONArray results = jsonObject.getJSONArray("results");
            JSONObject r = results.getJSONObject(0);

            return r.getString("formatted_address");




        } catch (Exception e) {
            return null;
        }
    }













}

