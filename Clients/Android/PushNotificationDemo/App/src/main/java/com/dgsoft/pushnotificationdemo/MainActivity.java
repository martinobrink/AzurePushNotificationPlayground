package com.dgsoft.pushnotificationdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.dgsoft.pushnotificationdemo.backend.PushNotificationClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity {
    //SENDER_ID below: Please create your own senderid/projectnumber using google apis console instead of using mine :)
    private String SENDER_ID = "337436325121";
    //public static final String EXTRA_MESSAGE = "message";
    //public static final String PROPERTY_REG_ID = "registration_id";
    private static final String TAG = "PushNotificationDemo";
    private GoogleCloudMessaging gcm;
    private TextView regIdTextView;
    private Context context;
    private String registrationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regIdTextView = (TextView) findViewById(R.id.regIdTextView);
        context = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);

        new RegisterBackground(context).execute();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainFragment extends Fragment {

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private class RegisterBackground extends AsyncTask<String,String,String> {
        private Context context;

        public RegisterBackground(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... arg0) {
            String message = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                registrationId = gcm.register(SENDER_ID);
                message = "Device registered, registration id=" + registrationId;
                Log.d(TAG, message);
                sendRegistrationIdToBackend(registrationId);

            } catch (IOException ex) {
                message = "Error :" + ex.getMessage();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            //regIdTextView.append(msg + "\n");
        }

        private void sendRegistrationIdToBackend(String registrationId) {

            String backendBaseUrl = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString(SettingsActivity.SETTINGS_KEY_BACKEND_URL, "");
            PushNotificationClient client = new PushNotificationClient(backendBaseUrl);
            client.performSearch("hest", new Callback<String>() {
                @Override
                public void success(String s, Response response) {
                    Toast.makeText(context, "RESULT:" + s, Toast.LENGTH_LONG).show();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Toast.makeText(context, "ERROR:" + retrofitError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            Log.i(TAG, registrationId);
            // this code will send registration id of a device to our own server.
        }
    }
}
