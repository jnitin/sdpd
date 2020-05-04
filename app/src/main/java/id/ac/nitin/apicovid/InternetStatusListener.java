package id.ac.nitin.apicovid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.net.ConnectivityManager;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.util.Log;

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
                Log.e(TAG, "Device disconnected .");
            } else {
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Device connected .");
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(ONLINE_STATUS, !noConnectivity);
            editor.commit();
        }
    }
}
