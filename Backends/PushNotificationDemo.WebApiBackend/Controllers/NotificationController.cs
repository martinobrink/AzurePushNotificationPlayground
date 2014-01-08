using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using System.Web.Http;
using Microsoft.ServiceBus.Notifications;
using Newtonsoft.Json;
using Raven.Client;
using Notification = PushNotificationDemo.WebApiBackend.Models.Notification;
using System.Linq;

namespace PushNotificationDemo.WebApiBackend.Controllers
{
    public class NotificationController : BaseController
    {
        //please replace the connectionString and notificationHubName fields below with your own notification hub connection info
        private const string _connectionString = "Endpoint=sb://pushdemo.servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=VOQDgU/RZ9QhCZsIlJLgVBs0Gq8VFK1lEhXnr6HMPHs=";
        private const string _notificationHubName = "pushdemo";
        private readonly NotificationHubClient _hubClient;

        public NotificationController()
        {
            _hubClient = NotificationHubClient.CreateClientFromConnectionString(_connectionString, _notificationHubName);
        }

        // GET api/notification
        public Task<IList<Notification>> Get()
        {
            return Session.Query<Notification>().ToListAsync();
        }

        // POST api/notification
        public async Task<IHttpActionResult> Post([FromBody]Notification notification)
        {
            var notificationToSave = new Notification
                {
                    NotificationGuid = Guid.NewGuid().ToString(),
                    TimeStamp = DateTime.UtcNow,
                    Message = notification.Message,
                    SenderName = notification.SenderName,
                    Recipients = new List<string>()
                };

            var recipientNamesString = new StringBuilder();
            var registrationDescriptions = await _hubClient.GetAllRegistrationsAsync(500);
            foreach (var registration in registrationDescriptions)
            {
                if (registration is GcmRegistrationDescription)
                {
                    var userName = registration.Tags
                        .Where(t => t.StartsWith("user"))
                        .Select(t => t.Split(':')[1].Replace("_", " "))
                        .FirstOrDefault();
                    userName = userName ?? "Unknown User";
                    notificationToSave.Recipients.Add(userName);
                    recipientNamesString.Append(userName + "#");
                }
            }
            
            string notificationJsonPayload = 
                "{\"data\" : " +
                "   {" +
                "   \"message\": \"" + notificationToSave.Message + "\"," +
                "   \"senderName\": \"" + notificationToSave.SenderName + "\"," +
                "   \"recipientNames\": \"" + recipientNamesString + "\"" +
                "   }" +
                "}";

            var result = await _hubClient.SendGcmNativeNotificationAsync(notificationJsonPayload);
            
            notificationToSave.TrackingId = result.TrackingId;

            await Session.StoreAsync(notificationToSave);

            return Ok(notificationToSave);
        }

        // DELETE api/notification
        public IHttpActionResult Delete()
        {
            Session.DeleteAll<Notification>();
            return Ok();
        }
    }
}