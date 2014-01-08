using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace PushNotificationDemo.WebApiBackend.Models
{
    public class Notification
    {
        [Required]
        public string Message { get; set; }
        [Required]
        public string SenderName { get; set; }

        public string NotificationGuid { get; set; }
        public DateTime TimeStamp { get; set; }
        public List<string> Recipients { get; set; }
        public string TrackingId { get; set; } 
    }
}