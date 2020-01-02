# jwtpop
JWT based proof of possession for mobile devices

This is a demonstration set of assets for secure binding of a mobile device to a user identity, implemented using the ForgeRock identity stack.

Using JWT based challenge response authentication, the device presents a signed challenge to the access management service, using a registered key which is strongly protected within the device. The exact protection mechanism for the device key is implmentation specific: it could for example be secured by a biometric check to support multi factor authentication.

The core functions are provided by a set of authentication nodes for the ForgeRock Access Management service. These nodes are implemented as Scripted Decision Nodes for convenience; for production purposes, authentication nodes should be implemented as native Java classes.

Note that these assets are purely for demonstrating the concept of JWT based proof of possession, and have a number of limitations, including

- Limited to zero error handling
- Plain text IDM credentials
- Support for RSA signing only (should support EC)
- Plenty more limitations

## Contents

This repo consists of the following assets

- Sample scripted decision nodes for ForgeRock Access Manager 
- Sample LDIF files with the LDAP container definition for holding the device key information in a user directory 
- Sample configuration files for ForgeRock IDM to handle device registration
- Sample Postman collection and environment definition to emulate the mobile device for testing

## Requirements

You'll need the following to implement the full demo setup

- [ForgeRock Access Management](https://www.forgerock.com/platform/access-management) (tested with AM 6.5.2)
- [ForgeRock Directory Services](https://www.forgerock.com/platform/directory-services) (tested with DS 6.5.2)
- [ForgeRock Identity Management](https://www.forgerock.com/platform/identity-management) (tested with IDM 6.5.0)
- [Postman](https://www.getpostman.com) (tested with Postman 7.12)





