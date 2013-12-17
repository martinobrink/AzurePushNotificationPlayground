using System;
using System.Collections.Generic;
using System.Web.Http;
using PushNotificationDemo.WebApiBackend.Models;

namespace PushNotificationDemo.WebApiBackend.Controllers
{
    public class DeviceController : ApiController
    {
        private static readonly Dictionary<string, Device> _devices = new Dictionary<string, Device>();

        // GET api/device
        public IEnumerable<Device> Get()
        {
            return _devices.Values;
        }

        // GET api/device/44809e3f-ebe0-4bdf-8aaf-2c6581496ef2
        public IHttpActionResult Get(string deviceGuid)
        {
            Device device;
            var deviceFound = _devices.TryGetValue(deviceGuid, out device);

            if (deviceFound)
            {
                return Ok(device);
            }

            return NotFound();
        }

        // PUT api/device
        public IHttpActionResult Put([FromBody]Device device)
        {
            if (String.IsNullOrEmpty(device.DeviceGuid))
            {
                device.DeviceGuid = Guid.NewGuid().ToString();
            }

            device.TimeStamp = DateTime.UtcNow;

            _devices[device.DeviceGuid] = device;

            return Ok(device);
        }

        // DELETE api/device
        public IHttpActionResult Delete()
        {
            _devices.Clear();
            return Ok();
        }
    }
}