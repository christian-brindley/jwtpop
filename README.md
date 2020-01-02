# jwtpop
JWT based proof of possession for mobile devices

This is a demonstration set of assets for secure binding of a mobile device to a user identity, implemented using the ForgeRock identity stack.

In this scenario, the device presents a signed challenge to the access management service, using a registered key which is strongly protected within the device. The exact protection mechanism for the device key is implmentation specific: it could for example be secured by a biometric check to support multi factor authentication.

The core functions are provided by a set of authentication nodes for the ForgeRock Access Management service. These nodes are implemented as Scripted Decision Nodes for convenience; for production purposes, authentication nodes should be implemented as native Java classes.

Note that these assets are for demonstration purposes only, and have a number of limitations, including

- Limited to zero error handling
- Plain text passwords (should be secrets)
- Support for RSA signing only (should support EC)
- Plenty more limitations

## Contents




