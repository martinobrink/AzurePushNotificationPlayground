using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Web.Http;
using PushNotificationDemo.WebApiBackend.Models;
using Raven.Client;

namespace PushNotificationDemo.WebApiBackend.Controllers
{
    public class DeviceController : BaseController
    {
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
                deviceToSave = new Device {DeviceGuid = Guid.NewGuid().ToString()};
            }

            deviceToSave.TimeStamp = DateTime.UtcNow;
            deviceToSave.UserName = device.UserName;
            deviceToSave.SubscriptionCategories = device.SubscriptionCategories;
            deviceToSave.PlatformDescription = device.PlatformDescription;
            deviceToSave.Platform = device.Platform;
            deviceToSave.Token = device.Token;

            await Session.StoreAsync(deviceToSave);

            return Ok(device);
        }

        // DELETE api/device
        public IHttpActionResult Delete()
        {
            Session.DeleteAll<Device>();
            return Ok();
        }
    }
}