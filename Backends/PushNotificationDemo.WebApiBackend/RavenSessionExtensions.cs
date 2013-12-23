using Raven.Abstractions.Data;
using Raven.Client;
using Raven.Client.Indexes;

namespace PushNotificationDemo.WebApiBackend
{
    public static class RavenSessionExtensions
    {
        public static void DeleteAll<TEntity>(this IAsyncDocumentSession session) where TEntity : class
        {
            new RavenDocumentsByEntityName().Execute(WebApiApplication.RavenDbStore);
            session.Advanced.DocumentStore.DatabaseCommands.DeleteByIndex("Raven/DocumentsByEntityName",
                                                                          new IndexQuery { Query = "Tag:" + typeof(TEntity).Name + "s" },
                                                                          allowStale: true);
        }
    }
}