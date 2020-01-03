# jwtpop
JWT based proof of possession for mobile devices

This is a demonstration set of assets for secure binding of a mobile device to a user identity, implemented using the ForgeRock identity stack.

The demo uses JWT based challenge response authentication. The device presents a signed challenge to the access management service, using a registered key which is strongly protected within the device. The exact protection mechanism for the device key is implmentation specific: it could for example be secured by a biometric check to support multi factor authentication.

If the device key is already registered to the user's identity, then authentication is complete and the user is given access. If the device key is not known, then the user can be authenticated using as many available factors as required: once this authentication process is complete, the device key is registered for subsequent logins, as per [this sample flow](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIAUCcD2iBm1XQA6IM48niIgHYBQpmAhvKAMYhXHDQCyiARiFBdXQ5U2gBBFjxoh6jZgBEAyuTacoAWgB8IgFywA8rIAq0APSUArsAAWkJhMrBIpRV0gAeZZoAsABgCM0ABLAwJgAwpTg4OyUtADWpMSIdtBQKMwYmtAA6lluZpbWtLaQGtDOtOZhUMQA5pCqDhxOao5QxbIgVcTQ8ATYxPjQAFYA7swAFMDw7TXw0JyIALaQExIAlPVKkGqaOvpGphZWdIVxCTCTVeapaM1FwrmIkwBetkTExc7dOL39w8B1IlsWMUAGqQSYoACe0Hw4DQOHaxEgABNBiNoAAdYgAUQAHhMoswTPgZgIUUjIAA3CQwcnAShcHCkAGqOTFADiSy6kCqIBwdm6ZMp1Jw0BQD2gRLBTJYrlZ0AAStzefzkdByVTaARpYDisFLDFoAApDIGCk4AB0XJ5fLBqvVwtIYWY9s10HizDFJmISPWTlcmgAqsToPs8kdQCRoAVwpEYoybjroEGwSHcocbBGyMyAJLSIEKpU2mYu+y5lhqOVCJGCjUwYCICXBvkPexWH03f1AvCIPSIaJWchAA).

The core functions are provided by a set of authentication nodes for the ForgeRock Access Management service. These nodes are implemented as Scripted Decision Nodes for convenience; for production purposes, authentication nodes should be implemented as native Java classes.

Note that these assets are purely for demonstrating the concept of JWT based proof of possession, and have a number of limitations, including

- Limited/zero error handling
- Support for RSA signing only (no EC support)
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

## Setup

### Access Management

#### Realm

It is recommended that you set up a separate realm for testing the assets. 

#### Secrets

You'll need to set up a secret with ID "IDMPassword" for the registration script. This should be the password for the IDM user "openidm-admin" (change this in the registration node if necessary).

#### Authentication nodes

You'll need to add all four groovy scripts as scripted decision nodes. Once done, you can build an authentication tree 



