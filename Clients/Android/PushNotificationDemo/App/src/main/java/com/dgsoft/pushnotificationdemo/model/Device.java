package com.dgsoft.pushnotificationdemo.model;

import java.util.List;

public class Device
{
    //non-camel case field names to ease deserialization from backend
    public String DeviceGuid;
    public String Token;
    public String UserName;
    public String Platform;
    public String PlatformDescription;
    public List<String> SubscriptionCategories;
}
