package com.dgsoft.pushnotificationdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
import android.widget.Toast;

import com.dgsoft.pushnotificationdemo.backend.PushNotificationClient;
import com.dgsoft.pushnotificationdemo.model.Device;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity {
    //SENDER_ID below: Please create your own senderid/projectnumber using google apis console instead of using mine :)
    private String SENDER_ID = "337436325121";
    private static final String TAG = "PushNotificationDemo";
    private GoogleCloudMessaging gcm;
    private Context context;
    private String registrationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new RegisterInBackgroundTask(context).execute();
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

    private class RegisterInBackgroundTask extends AsyncTask<String,String,String> {
        private Context context;

        public RegisterInBackgroundTask(Context context) {
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
        }

        private void sendRegistrationIdToBackend(String registrationId) {

            String backendBaseUrl = readStringFromSharedPreferences(SettingsActivity.SETTINGS_KEY_BACKEND_URL);
            if (backendBaseUrl == null || backendBaseUrl == "")
            {
                return;//no backend base url set in settings, do not try to call backend
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
            device.SubscriptionCategories.add("horse");
            device.SubscriptionCategories.add("dog");
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