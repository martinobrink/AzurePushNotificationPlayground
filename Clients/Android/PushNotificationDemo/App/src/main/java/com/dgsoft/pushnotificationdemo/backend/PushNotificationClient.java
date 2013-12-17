package com.dgsoft.pushnotificationdemo.backend;

import com.dgsoft.pushnotificationdemo.model.Device;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Path;

public class PushNotificationClient implements PushNotificationBackendService {
    private PushNotificationBackendService client;

    public PushNotificationClient(String backendBaseUrl) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(backendBaseUrl)
                .build();
        client = restAdapter.create(PushNotificationBackendService.class);
    }

    @Override
    public void performSearch(@Path("query") String query, Callback<String> callback) {
        client.performSearch(query, callback);
    }

    @Override
    public void registerDevice(@Body Device device, Callback<Device> callback) {
        client.registerDevice(device, callback);
    }
}