import static org.forgerock.json.JsonValue.*;
import org.forgerock.openam.auth.node.api.*;
import javax.security.auth.callback.NameCallback;

if (callbacks.isEmpty()) {
  action = Action.send(new NameCallback("FriendlyName")).build();
} else {
  sharedState = sharedState.put("popFriendlyName", callbacks[0].getName());
  action = Action.goTo("true").build();
}
