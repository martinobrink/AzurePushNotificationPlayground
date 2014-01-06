using System.Collections.Generic;

namespace PushNotificationDemo.WebApiBackend
{
    public static class HashSetExtensions
    {
        public static HashSet<string> Add(this HashSet<string> hashSet, string prefix, IEnumerable<string> itemsToAdd)
        {
            foreach (var item in itemsToAdd)
            {
                hashSet.Add(prefix + ":" + item);
            }

            return hashSet;
        }
    }
}