import org.forgerock.json.jose.jws.JwsAlgorithm
import org.forgerock.json.jose.jws.handlers.HmacSigningHandler
import org.forgerock.json.jose.jws.SigningManager
import org.forgerock.json.jose.jws.handlers.SigningHandler
import org.forgerock.json.jose.utils.Utils
import org.forgerock.json.jose.jwt.JwtClaimsSet
import org.forgerock.json.jose.jws.JwsHeader
import org.forgerock.util.encode.Base64url
import org.forgerock.json.jose.jws.SignedJwt
import org.forgerock.json.jose.jwk.JWKSet
import org.forgerock.json.jose.jwk.JWK
import org.forgerock.json.jose.jwk.RsaJWK
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdUtils;
import org.forgerock.json.jose.jws.handlers.RSASigningHandler
import java.util.function.*;
import groovy.json.JsonOutput

class ParsedJwt {

  private String headerEncoded;
  private String bodyEncoded;
  
  private Object headerClaims;
  private Object bodyClaims;
  private Object signature;
  
  ParsedJwt(jwt) {
    def jwtEncoded = jwt.tokenize('.');

    this.headerEncoded = jwtEncoded[0];
    this.bodyEncoded = jwtEncoded[1]; 
    
    def jwtSignatureEncoded = jwtEncoded[2]; 
  
    def jwtHeaderDecoded = Utils.base64urlDecode(this.headerEncoded);
    def jwtClaimsDecoded = Utils.base64urlDecode(this.bodyEncoded); 
     

    this.headerClaims = Utils.parseJson(jwtHeaderDecoded); 
    this.bodyClaims = Utils.parseJson(jwtClaimsDecoded);
    this.signature = Base64url.decode(jwtSignatureEncoded);
  }

  Object getClaim(name) {
    return this.bodyClaims[name];
  }
  
  Object getJwk() {
    return JsonOutput.toJson(this.headerClaims.jwk);
  }
  
  boolean validateSig() {
    return validateSig(getJwk());
  }
  
  boolean validateSig(jwk) {
    def alg = headerClaims.alg;

    if (alg != "RS256") {
      logger.error("Unsupported algorithm " + alg);
      return null;
    }

    def RsaJWK rsajwk = RsaJWK.parse(jwk);

    def SigningManager signingManager = new SigningManager();
    def SigningHandler signingHandler = signingManager.newRsaSigningHandler(rsajwk.toRSAPublicKey());    
  
    def jwtHeaderBin = this.headerClaims;
  
    // Hack to replace header JWK with RsaJWK object type (otherwise JwsHeader() complains)
    def jwkHeaderJson = JsonOutput.toJson(jwtHeaderBin.jwk);
    def RsaJWK jwkHeader = RsaJWK.parse(jwkHeaderJson);  
    jwtHeaderBin.jwk = jwkHeader;
    // End hack
    
    def JwsHeader jwsHeader = new JwsHeader(jwtHeaderBin);

    def JwtClaimsSet claimsSet = new JwtClaimsSet(this.bodyClaims);

    def payload = this.headerEncoded + "." + this.bodyEncoded;
    byte[] payloadAsBytes = payload.getBytes("UTF-8")
    SignedJwt reconstructedJwt = new SignedJwt(jwsHeader, claimsSet, payloadAsBytes, signature)
    def validSignature = reconstructedJwt.verify(signingHandler);
  
    return validSignature;
  }
}

// Get POP response from previous challenge

def popChallenge = sharedState.get("popChallenge");
def popResponse = sharedState.get("popResponse");


def parsedResponse = new ParsedJwt(popResponse);

def userId = parsedResponse.getClaim("sub");
def deviceId = parsedResponse.getClaim("iss");
def challenge = parsedResponse.getClaim("jti");

logger.message ("Claimed user " + userId);
logger.message ("Claimed device " + deviceId);
logger.message   ("Claimed challenge " + challenge);
logger.message   ("POP challenge     " + popChallenge);

if (challenge != popChallenge){
  logger.error("Challenge mismatch");
  outcome = "failed";
  return;
}



// Pull user entry from identity store
userIdentity = IdUtils.getIdentity(userId,realm);

devices = userIdentity.getAttribute("popDeviceProfiles");
logger.message("Registered devices (" + devices.size() + "):" + devices);

// Run through registered devices for a match

Iterator it = devices.iterator(); 
validated = false;
found = false;

while (it.hasNext()) { 

  def deviceJson = (String)it.next();  
  logger.message("Device record " + deviceJson);
  def device = Utils.parseJson(deviceJson);
  logger.message("Device ID " + device.deviceId);
  
  if (device.deviceId == deviceId) {
    logger.message("Device match");
    found = true;    
    logger.message("registered jwk is " + device.jwk);
    validated = parsedResponse.validateSig(device.jwk);  
    break;
  }
  
} 

if (!found) {
  def jwkClaim = parsedResponse.getJwk();
  
  validated = parsedResponse.validateSig();
  if (!validated) {
    logger.error ("Self signed signature validation failed");
    outcome = "failed";
  }
  else {
    def popDevice = [
      "deviceId" : deviceId,
      "jwk" : jwkClaim
    ];
    logger.message ("Registering against user " + userId);
    sharedState.put("popDevice", popDevice);
    sharedState = sharedState.put("username",userId);
    logger.message ("Unregistered device added to shared state " + popDevice);
    outcome = "unregistered";
  }
}
else if (validated) {
  logger.message ("Signature validated");
  sharedState = sharedState.put("username",userId);
  outcome = "success";
}
else {
  logger.error ("Signature validation failed");
  outcome = "failed";
}
