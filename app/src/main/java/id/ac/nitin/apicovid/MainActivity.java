package id.ac.nitin.apicovid;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.Intent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import androidx.preference.PreferenceManager;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private String TAG = MainActivity.class.getSimpleName();
    private ListView listView;
    private static String url = "https://api.covid19api.com/summary";
    private static String DATA_SET = "covidDataSetUrl";
    private static SharedPreferences prefs = null;
    private static String COUNTRY = "country";
    ArrayList<HashMap<String, String>> covidList;
    private FirebaseDatabase database = null;
    private DatabaseReference myRef = null;
    private GlobalCovidData data = null;
    private static String ONLINE_STATUS  = "onlinestatus";

    InternetStatusListener exampleBroadcastReceiver = new InternetStatusListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // set covid Data set url
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(DATA_SET, url);
        editor.commit();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        covidList = new ArrayList<>();
        listView = findViewById(R.id.listView);

        new GetSummary().execute();

        // Get Firebase Database instance and global covid data node
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("globalCovidData");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                DecimalFormat thousand = new DecimalFormat("#,###");
                TextView tvGlobalConfirmed = findViewById(R.id.tvGlobalConfirmed);
                TextView tvGlobalNewConfirmed = findViewById(R.id.tvGlobalNewConfirmed);
                TextView tvGlobalDeaths = findViewById(R.id.tvGlobalDeaths);
                TextView tvGlobalNewDeaths = findViewById(R.id.tvGlobalNewDeaths);
                TextView tvGlobalRecovered = findViewById(R.id.tvGlobalRecovered);
                TextView tvGlobalNewRecovered = findViewById(R.id.tvGlobalNewRecovered);
                TextView tvLastUpdate = findViewById(R.id.tvLastUpdate);


                data = dataSnapshot.getValue(GlobalCovidData.class);
                tvLastUpdate.setText(data.date);
                tvGlobalConfirmed.setText(thousand.format(Double.valueOf(data.globalConfirmerdCases)));
                tvGlobalDeaths.setText(thousand.format(Double.valueOf(data.globalDeaths)));
                tvGlobalRecovered.setText(thousand.format(Double.valueOf(data.globalRecovered)));
                tvGlobalNewConfirmed.setText("+ " + thousand.format(Double.valueOf(data.globalNewConfirmerdCases)));
                tvGlobalNewDeaths.setText("+ " + thousand.format(Double.valueOf(data.globalNewDeaths)));
                tvGlobalNewRecovered.setText("+ " + thousand.format(Double.valueOf(data.globalNewRecovered)));
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(exampleBroadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(exampleBroadcastReceiver);
    }

    /** Called when the user taps the Settings button */
    public void onChangeSettings(View view) {
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra(EXTRA_MESSAGE, "FromActivity1");
        startActivity(intent);
    }

    /** Called when the user taps the Load Button */
    public void onLoadData(View view) {
        boolean isOnline;
        // isOnline flag is set in Broadcast Receiver based on connectivity status
        isOnline =   prefs.getBoolean(ONLINE_STATUS, false);
        if (isOnline) {
            Log.e(TAG, "Device connected to internet");
            Toast.makeText(getApplicationContext(), "On-line.Fetched Latest Info", Toast.LENGTH_SHORT).show();
            new GetSummary().execute();
        }else{
            Log.e(TAG, "Device not connected to internet .");
            Toast.makeText(getApplicationContext(), "Off-line.Last Updated Info Shown", Toast.LENGTH_SHORT).show();
        }
    }

    // AsyncTask Function
    private class GetSummary extends AsyncTask<Void, Void, Void> {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        String currentdate = "";
        String globalConfirmed = "";
        String globalNewConfirmed = "";
        String globalDeaths = "";
        String globalNewDeaths = "";
        String globalRecovered = "";
        String globalNewRecovered = "";
        DecimalFormat thousand = new DecimalFormat("#,###");
        //format string  date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dt = null;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler handler = new HttpHandler();
            String defaulthttpurl = "https://api.covid19api.com/summary";
            String httpurl;
            String countryname;
            String defcountry= "India";


            //making request and getting response
            httpurl =   prefs.getString(DATA_SET, defaulthttpurl);
            Log.d("Service URL :", httpurl);
            countryname =   prefs.getString(COUNTRY, defcountry);
            Log.d("Country Name :", countryname);
            String jsonStr = handler.makeServiceCall(httpurl);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    JSONObject global = jsonObject.getJSONObject("Global");

                    // Get required information from received json object
                    currentdate = jsonObject.getString("Date");
                    globalConfirmed = global.getString("TotalConfirmed");
                    globalNewConfirmed = global.getString("NewConfirmed");
                    globalDeaths = global.getString("TotalDeaths");
                    globalNewDeaths = global.getString("NewDeaths");
                    globalRecovered = global.getString("TotalRecovered");
                    globalNewRecovered = global.getString("NewRecovered");

                    //array Countries List
                    JSONArray countries = jsonObject.getJSONArray("Countries");
                    Log.e(TAG, "JSON Country: " + countries);
                    //looping  countries
                    for (int i = 0; i < countries.length(); i++) {
                        JSONObject c = countries.getJSONObject(i);

                        String country = c.getString("Country");
                        if (country.equals(countryname)) {
                            String totalConfirmed = c.getString("TotalConfirmed");
                            String totalDeaths = c.getString("TotalDeaths");
                            String totalRecovered = c.getString("TotalRecovered");
                            HashMap<String, String> covid = new HashMap<>();

                            covid.put("country", country);
                            covid.put("totalConfirmed", thousand.format(Double.valueOf(totalConfirmed)) + " Cases");
                            covid.put("totalDeaths", thousand.format(Double.valueOf(totalDeaths)) + " Deaths");
                            covid.put("totalRecovered", thousand.format(Double.valueOf(totalRecovered)) + " Recovered");
                            covidList.clear();
                            covidList.add(covid);

                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error: " + e.getMessage());
                }
            }
            else {
                Log.e(TAG, "Couldn't get JSON from server. Check LogCat for possible errors!");
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                Date date = dateFormat.parse(currentdate);
                SimpleDateFormat dateLocal = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
                dateLocal.setTimeZone(TimeZone.getDefault());
                dt = dateLocal.format(date);
                Log.e("Current date", dt);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(progressBar.isShown()){
                progressBar.setVisibility(View.GONE);
                ListAdapter listAdapter = new SimpleAdapter(
                        MainActivity.this, covidList,
                        R.layout.list_item, new String[]{"country","totalConfirmed","totalDeaths","totalRecovered"},
                        new int[]{R.id.tvCountries, R.id.tvCases, R.id.tvDeaths, R.id.tvRecovered}
                );
                listView.setAdapter(listAdapter);
            }
            GlobalCovidData globalCovidData = new GlobalCovidData(globalConfirmed,globalNewConfirmed,
                    globalDeaths,globalNewDeaths,
                    globalRecovered,globalNewRecovered,currentdate);
            myRef = database.getReference().child("globalCovidData");
            myRef.setValue(globalCovidData);
        }
    }
}