package cloud.artik.example.hellocloud;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class main_menu extends AppCompatActivity {
    private String mAccessToken;
    private Button btn_driving,btn_walking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mAccessToken = getIntent().getStringExtra("mytoken");
        btn_driving=(Button)findViewById(R.id.btn_driving);
        btn_walking=(Button)findViewById(R.id.btn_walking);

      //setting logic to change UI if the tracker is running in background
        if(human_service.status==true) {
        btn_walking.setEnabled(true);
        }
        else if(MyService.status==true){
            btn_driving.setEnabled(true);
        }
    }
    void msg(String a)
    {
        Toast.makeText(main_menu.this, a, Toast.LENGTH_SHORT).show();
    }
    public void click_driving(View view)
    {
        //turns on the driving mode
        Intent msgActivityIntent = new Intent(this,MyService.class);


        msgActivityIntent.putExtra("token", mAccessToken);

          startService(msgActivityIntent);
        btn_driving.setEnabled(true);


    }
    public void click_walking(View view)
    {
        //turns on the walking mode
        Intent msgActivityIntent = new Intent(this,human_service.class);

        msgActivityIntent.putExtra("token", mAccessToken);

        startService(msgActivityIntent);
        btn_walking.setEnabled(true);
    }
    public void stop_walking(View view)
    {
       // MyService.handler.removeCallbacksAndMessages(null);

        stopService(new Intent(main_menu.this,human_service.class));
        btn_walking.setEnabled(false);
    }
    public void stop_driving(View view)
    {
      //  human_service.handler.removeCallbacksAndMessages(null);
        stopService(new Intent(main_menu.this,MyService.class));
        btn_driving.setEnabled(false);
    }
    public void click_track(View view)
    {
        Intent a = new Intent(this,tracking.class);
        a.putExtra("token", mAccessToken);
        startActivity(a);
    }


}
