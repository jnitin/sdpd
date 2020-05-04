package id.ac.aknganjuk.apicovid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
import android.net.ConnectivityManager;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;

public class InternetStatusListener extends BroadcastReceiver {

    private static final String TAG = "INTERNET_STATUS";
    private static String ONLINE_STATUS = "onlinestatus";
    private static SharedPreferences prefs = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
            );
            if (noConnectivity) {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(ONLINE_STATUS, !noConnectivity);
            editor.commit();
        }
    }
}
