using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace PushNotificationDemo.WebApiBackend.Models
{
    public class Device
    {
        public string DeviceGuid { get; set; }
        [Required]
        public string Token { get; set; }
        [JsonConverter(typeof(StringEnumConverter))]
        [Required]
        public PlatformType Platform { get; set; }
        [Required]
        public string UserName { get; set; }
        public string PlatformDescription { get; set; }
        public List<string> SubscriptionCategories { get; set; }
    }

    public enum PlatformType
    {
        Android = 1,
        iOS = 2,
        WindowsPhone = 3,
        Windows = 4
    }
}