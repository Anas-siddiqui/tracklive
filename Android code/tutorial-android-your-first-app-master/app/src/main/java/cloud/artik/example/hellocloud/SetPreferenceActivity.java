package cloud.artik.example.hellocloud;

/**
 * Created by Anas on 7/23/2016.
 */
import android.app.Activity;
import android.os.Bundle;

public class SetPreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

}