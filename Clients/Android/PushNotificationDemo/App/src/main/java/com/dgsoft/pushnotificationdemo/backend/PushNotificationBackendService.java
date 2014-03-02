package com.dgsoft.pushnotificationdemo.backend;

import com.dgsoft.pushnotificationdemo.model.Device;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.PUT;

public interface PushNotificationBackendService {

    @PUT("/api/device")
    void registerDevice(@Body Device device, Callback<Device> callback);
}
