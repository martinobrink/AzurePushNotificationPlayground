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
import com.dgsoft.pushnotificationdemo.model.Device;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

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

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        new RegisterBackground(context).execute();
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

            String backendBaseUrl = readStringFromSharedPreferences(SettingsActivity.SETTINGS_KEY_BACKEND_URL);
            if (backendBaseUrl == null || backendBaseUrl == "")
            {
                //Toast.makeText(context, "Please set backend url in settings in order to register with backend!", Toast.LENGTH_LONG).show();
                return;
            }

            PushNotificationClient client = new PushNotificationClient(backendBaseUrl);
            Device device = createDevice(registrationId);
            client.registerDevice(device, new Callback<Device>() {
                @Override
                public void success(Device device, Response response) {
                    writeStringToSharedPreferences(SettingsActivity.SETTINGS_KEY_DEVICEGUID, device.DeviceGuid);
                    Toast.makeText(context, "Successfully registered with backend! Received GUID:" + device.DeviceGuid, Toast.LENGTH_LONG).show();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Toast.makeText(context, "ERROR:" + retrofitError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            Log.i(TAG, registrationId);
        }

        private Device createDevice(String registrationId) {
            Device device = new Device();
            device.Platform = "Android";
            device.Token = registrationId;
            device.UserName = readStringFromSharedPreferences(SettingsActivity.SETTINGS_KEY_USERNAME);
            device.DeviceGuid = readStringFromSharedPreferences(SettingsActivity.SETTINGS_KEY_DEVICEGUID);
            //todo set device.PlatformDescription based on Android version
            device.SubscriptionCategories = new ArrayList<String>();
            device.SubscriptionCategories.add("hest");
            device.SubscriptionCategories.add("hund");
            device.SubscriptionCategories.add("hippo");
            return device;
        }

        private String readStringFromSharedPreferences(String preferenceKey) {
            return PreferenceManager
                            .getDefaultSharedPreferences(context)
                            .getString(preferenceKey, "");
        }

        private void writeStringToSharedPreferences(String preferenceKey, String value) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(preferenceKey, value);
            editor.commit();
        }
    }
}
