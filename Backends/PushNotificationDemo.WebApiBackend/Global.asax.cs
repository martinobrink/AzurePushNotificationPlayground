using System.Reflection;
using System.Web;
using System.Web.Http;
using System.Web.Mvc;
using Raven.Client;
using Raven.Client.Document;
using Raven.Client.Embedded;
using Raven.Client.Indexes;

namespace PushNotificationDemo.WebApiBackend
{
    public class WebApiApplication : HttpApplication
    {
        public static IDocumentStore RavenDbStore;

        protected void Application_Start()
        {
            CreateRavenDb();

            AreaRegistration.RegisterAllAreas();

            GlobalConfiguration.Configure(WebApiConfig.Register);
        }

        private static void CreateRavenDb()
        {
            RavenDbStore = new EmbeddableDocumentStore
                {
                    DataDirectory = @"~\App_Data\db"
                };
            RavenDbStore.Initialize();
            IndexCreation.CreateIndexes(Assembly.GetCallingAssembly(), RavenDbStore);
        }
    }
}
