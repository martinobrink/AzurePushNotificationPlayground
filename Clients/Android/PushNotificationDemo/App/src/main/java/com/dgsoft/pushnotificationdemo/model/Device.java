package com.dgsoft.pushnotificationdemo.model;

import java.util.List;

public class Device {
    public String registrationId;
    public String uniqueDeviceId;//set by server
    public String userName;
    public String emailAddress;
    public String phoneNumber;
    public List<String> categories;
}
