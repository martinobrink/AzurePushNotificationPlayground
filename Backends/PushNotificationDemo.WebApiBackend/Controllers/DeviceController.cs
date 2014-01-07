using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Web.Http;
using Microsoft.ServiceBus.Notifications;
using PushNotificationDemo.WebApiBackend.Models;
using Raven.Client;

namespace PushNotificationDemo.WebApiBackend.Controllers
{
    public class DeviceController : BaseController
    {
        //please replace the connectionString and notificationHubName fields below with your own notification hub connection info
        private const string _connectionString = "Endpoint=sb://pushdemo.servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=VOQDgU/RZ9QhCZsIlJLgVBs0Gq8VFK1lEhXnr6HMPHs=";
        private const string _notificationHubName = "pushdemo";
        private readonly NotificationHubClient _hubClient;

        public DeviceController()
        {
            _hubClient = NotificationHubClient.CreateClientFromConnectionString(_connectionString, _notificationHubName);
        }

        // GET api/device
        public Task<IList<Device>> Get()
        {
            return Session.Query<Device>().ToListAsync();
        }

        // GET api/device/44809e3f-ebe0-4bdf-8aaf-2c6581496ef2
        public async Task<IHttpActionResult> Get(string deviceGuid)
        {
            var device = await Session.Query<Device>().FirstOrDefaultAsync(d => d.DeviceGuid == deviceGuid);

            if (device != null)
            {
                return Ok(device);
            }

            return NotFound();
        }

        // PUT api/device
        public async Task<IHttpActionResult> Put([FromBody]Device device)
        {
            var deviceToSave = await Session.Query<Device>().FirstOrDefaultAsync(d => d.Token == device.Token);
            if (deviceToSave == null)
            {
                deviceToSave = await Session.Query<Device>().FirstOrDefaultAsync(d => d.DeviceGuid == device.DeviceGuid);
            }
            if (deviceToSave == null)
            {
                deviceToSave = new Device { DeviceGuid = Guid.NewGuid().ToString() };
            }

            deviceToSave.TimeStamp = DateTime.UtcNow;
            deviceToSave.UserName = device.UserName;
            deviceToSave.SubscriptionCategories = device.SubscriptionCategories;
            deviceToSave.PlatformDescription = device.PlatformDescription;
            deviceToSave.Platform = device.Platform;
            deviceToSave.Token = device.Token;

            var hubRegistration = await RegisterDeviceWithNotificationHub(deviceToSave);

            deviceToSave.HubRegistrationId = hubRegistration.RegistrationId;

            await Session.StoreAsync(deviceToSave);

            return Ok(deviceToSave);
        }

        
        // DELETE api/device
        public IHttpActionResult Delete()
        {
            Session.DeleteAll<Device>();
            return Ok();
        }

        private async Task<RegistrationDescription> RegisterDeviceWithNotificationHub(Device device)
        {
            var hubTags = new HashSet<string>()
                .Add("user", new[] { device.UserName })
                .Add("category", device.SubscriptionCategories);

            var hubRegistrationId = device.HubRegistrationId ?? "0";//null or empty string as query input throws exception
            var hubRegistration = await _hubClient.GetRegistrationAsync<RegistrationDescription>(hubRegistrationId);
            if (hubRegistration != null)
            {
                hubRegistration.Tags = hubTags;
                await _hubClient.UpdateRegistrationAsync(hubRegistration);
            }
            else
            {
                hubRegistration = await _hubClient.CreateGcmNativeRegistrationAsync(device.Token, hubTags);
            }
            return hubRegistration;
        }
    }
}