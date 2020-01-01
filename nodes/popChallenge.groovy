import org.forgerock.openam.auth.node.api.*;
import com.sun.identity.authentication.spi.HttpCallback;
import java.util.Locale;
import java.util.UUID;

String AUTH_SCHEME = "JWT-PoP";
String CHALLENGE_HEADER = "WWW-Authenticate";
String RESPONSE_HEADER = "Authorization";
int CHALLENGE_STATUS_CODE = 401;

def getNonce() {
  return UUID.randomUUID().toString();
}

if (callbacks.isEmpty()) {
  def popChallenge = sharedState.get("popChallenge");
  if (popChallenge == null) {
    
    popChallenge = getNonce();
    logger.message("New POP Challenge " + popChallenge);
    sharedState = sharedState.put("popChallenge", popChallenge);
  }
  
  String challengeHeaderValue = AUTH_SCHEME + " realm=\"" + Locale.ROOT + "\", challenge=\"" + popChallenge + "\"";
  action = Action.send(new HttpCallback(RESPONSE_HEADER, CHALLENGE_HEADER, challengeHeaderValue, CHALLENGE_STATUS_CODE)).build();
} 
else {
  def popResponse = callbacks[0].getAuthorization();
  logger.message("Got response [" + popResponse + "]");
  sharedState = sharedState.put("popResponse", popResponse);
  action = Action.goTo("true").build();
}
