package cloud.artik.example.hellocloud;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.model.NormalizedMessagesEnvelope;
import cloud.artik.model.UserEnvelope;

public class tracking extends AppCompatActivity {
    private String mAccessToken;
    private UsersApi mUsersApi = null;
    private MessagesApi mMessagesApi = null;
    public static  String DEVICE_ID ;
    public static String final_data="No Data";
    RelativeLayout car,dv;
    TextView txt_location,txt_heart,txt_steps,txt_speed,link;
    String the_link="";
View view_steps,view_car;
    String final_result[];

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    public static Handler handler;
    public static int timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        mAccessToken = getIntent().getStringExtra("token");
        setupArtikCloudApi();
        getUserInfo();
        car=(RelativeLayout)findViewById(R.id.layout_speed);
        dv=(RelativeLayout)findViewById(R.id.layout_steps);
txt_location=(TextView)findViewById(R.id.txt_location);
        txt_heart=(TextView)findViewById(R.id.txt_beat);
        txt_steps=(TextView)findViewById(R.id.txt_steps);
        txt_speed=(TextView)findViewById(R.id.txt_speed);
        link=(TextView)findViewById(R.id.link_web);
        view_car=(View)findViewById(R.id.view_car);
        view_steps=(View)findViewById(R.id.view_steps);
        link.setClickable(true);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        try {

            getLatestMsg();//gets the latest message broadcasted to cloud
        } catch (Exception e) {
            return;
        }
handler= new Handler();

        everysecond();




    }
    public void everysecond() {
        handler.postDelayed(new Runnable() {
            public void run() {
                try {

getLatestMsg();//gets the latest message broadcasted to cloud
                } catch (Exception e) {
                    return;
                }


                handler.postDelayed(this, timer*1000);
            }
        }, timer*1000);
    }




    private void setupArtikCloudApi() {
        ApiClient mApiClient = new ApiClient();
        mApiClient.setAccessToken(mAccessToken);
        mApiClient.setDebugging(true);

        mUsersApi = new UsersApi(mApiClient);
        mMessagesApi = new MessagesApi(mApiClient);
    }
    private static final String TAG = "Activity";
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
    private void processFailure(final String context, ApiException exc) {
        String errorDetail = " onFailure with exception" + exc;
        Log.w(context, errorDetail);
        exc.printStackTrace();
        //    showErrorOnUIThread(context+errorDetail, ge);
    }
    private void getLatestMsg() {
        final String tag = TAG + " getLastNormalizedMessagesAsync";
        try {
            int messageCount = 1;
            mMessagesApi.getLastNormalizedMessagesAsync(messageCount, DEVICE_ID, null,
                    new ApiCallback<NormalizedMessagesEnvelope>() {
                        @Override
                        public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                            processFailure(tag, exc);
                        }

                        @Override
                        public void onSuccess(NormalizedMessagesEnvelope result, int i, Map<String, List<String>> stringListMap) {
                            Log.v(tag, " onSuccess latestMessage = " + result.getData().toString());
                            updateGetResponseOnUIThread(result.getData().get(0).getMid(), result.getData().get(0).getData().toString());



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


        public void msg(String a){Toast.makeText(this, a,
            Toast.LENGTH_LONG).show();}

    private void updateGetResponseOnUIThread(final String mid, final String msgData) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
             function_tosplit(msgData);


            }
        });
    }
    public boolean car_check=false;

    public void function_tosplit(String a)
    {
        //CHANGING UI

            a=a.replace("{","");
            a=a.replace("}","");






            String res_loc[]=a.split("=");



            String result[]=a.split(",");
            final_result=result.clone();
            String var[] = final_result[0].split("=");
            if(var[0].equals("car_speed"))
            {
                view_steps.setVisibility(View.GONE);
                car.setVisibility(View.VISIBLE);
                dv.setVisibility(View.GONE);
                car_check=true;
            }
            else if(var[0].equals("heartRate")) {
                view_car.setVisibility(View.GONE);
                car.setVisibility(View.GONE);
                dv.setVisibility(View.VISIBLE);
                car_check=false;
            }
            if(car_check==true){
                String res[] = final_result[0].split("=");
                txt_speed.setText(res[1]);
                String res2[] = final_result[1].split("=");
                txt_heart.setText(res2[1]);
                 the_link=res_loc[3];
                txt_location.setText(  res_loc[3]);
            }
            else
            {
                String res[] = final_result[0].split("=");

                txt_heart.setText(res[1]);
                String myn[]=res_loc[2].split("step_count");
                the_link=myn[0];

                txt_location.setText(  myn[0]);
                String res3[] = final_result[4].split("=");

                txt_steps.setText(res3[1]);





            }










    }
    public void linker(View view)
    {
        //function to open the location in google map
        try{
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?q="+the_link));
        startActivity(browserIntent);
        }
        catch (Exception e){msg("Error opening location");return;}
    }


}
