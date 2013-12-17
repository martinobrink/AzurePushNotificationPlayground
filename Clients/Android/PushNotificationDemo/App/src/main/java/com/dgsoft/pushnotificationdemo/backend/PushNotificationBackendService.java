package com.dgsoft.pushnotificationdemo.backend;

import com.dgsoft.pushnotificationdemo.model.Device;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface PushNotificationBackendService {

    @PUT("/api/device")
    void registerDevice(@Body Device device, Callback<Device> callback);
}
