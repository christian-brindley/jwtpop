import org.forgerock.http.protocol.*
import org.forgerock.json.jose.utils.Utils
import groovy.json.JsonOutput
import java.util.Date;
import org.forgerock.guice.core.InjectorHolder;
import org.forgerock.openam.secrets.Secrets;
import org.forgerock.openam.secrets.SecretsProviderFacade;
import org.forgerock.secrets.Purpose;
  
def userName = sharedState.get("username");
 

def buildString(char[] input) {
  return String.valueOf(input);
}

// Get secret for IDM password
secrets = InjectorHolder.getInstance(Secrets.class);

def idmPass = secrets.getGlobalSecrets()
   .getNamedSecret(Purpose.PASSWORD,"IDMPassword")
   .getOrThrowUninterruptibly().revealAsUtf8(this.&buildString); 

def idmUser = "openidm-admin";


def error = false;


// Get the uuid for given username
  
Request request = new Request()
request.setMethod('GET');
request.setUri("https://idm.authdemo.org/openidm/managed/user?_queryFilter=userName+eq+'" + userName + "'")
request.getHeaders().add("X-OpenIDM-Username",idmUser);
request.getHeaders().add("X-OpenIDM-Password",idmPass);


def response = httpClient.send(request).get();
def String responseContent = response.getEntity();
def responseStatus = response.getStatus();

logger.message("status " + responseStatus);
logger.message("entity " + responseContent);

if (!responseStatus.toString().contains("200")) {
  logger.error("Failed to get user id from IDM");
  error = true;
}

if (!error) {

   def responseContentBin = Utils.parseJson(responseContent);
   def userEntry = responseContentBin.result;
   def id = userEntry._id[0];

   logger.message("id {}",id);

  
   // Add the device with link to user
  
   def device = sharedState.get("popDevice");
   def friendlyName = sharedState.get("popFriendlyName");
   def Date now = new Date();      
   def nowEpoch = now;
  
   def postBodyBin = [
     "deviceId": device.deviceId,
     "friendlyName": friendlyName,
     "lastUsed": nowEpoch,
     "registered": nowEpoch,
     "status": "active",
     "jwk": device.jwk,
     "owner": [
       "_ref": "managed/user/" + id,
     ]
   ];

   def postBody = JsonOutput.toJson(postBodyBin);

   logger.error("Sending content {}",postBody);
   request = new Request();
   request.setMethod('POST');
   request.setUri("https://idm.authdemo.org/openidm/managed/device?_action=create");
   request.getHeaders().add("X-OpenIDM-Username",idmUser);
   request.getHeaders().add("X-OpenIDM-Password",idmPass);
   request.getHeaders().add("Content-Type","application/json");
   request.setEntity(postBody);
                  
   response = httpClient.send(request).get();
   responseContent = response.getEntity();
   responseStatus = response.getStatus();

   logger.error("status " + responseStatus);
   logger.error("entity " + responseContent);

   if (!responseStatus.toString().contains("201")) {
      logger.error("Failed to create device");
      error = true;
   }  
}


outcome = error ? "false" : "true";
