# Assinare

In order to compile the projet execute the following:

  ```
  mvn clean install
  ```

To generate artifacts signed by your own certificates add the following properties to maven execution:

  - **keystore.path**: path to the keystore with the certificate keys
  - **keystore.password**: keystsore password
  - **keystore.alias**: keystore certificate alias

When installing Assinare in remote environments, add the following properties to maven execution:
  - **host.name**: a list of hostnames, separated by spaces, that can execute the application. This value will be used in the JARs Manifest codebase attribute. If a protocol is not specified then both http and https can be used (default: http://localhost:8080)
  - **jnlp.codebase**: the base url to which all the JNLP relative urls will reference. If an empty value is specified then all the JNLP relative urls will reference the url used to access the JNLP (default: http://localhost:8080/assinare-web)
  - **web-start-launcher.url**: the url where the web start launcher application is deployed (default: http://localhost:8080/web-start-launcher)
  - **assinare-web.url**: the url where the assinare web application is deployed (default: http://localhost:8080/assinare-web)
  - **Caller-Allowable-Codebase**: the domains, separated by spaces, that can execute the application. If an asterisk (*) wildcard is used then . If a protocol is not specified then both http and https can be used (default: *)
  - **Application-Library-Allowable-Codebase**: a list of hostnames, separated by spaces, that can host the application. An asterisk (*) can be used as a wildcard to specify all domains or as a specific domain wildcard, as in *.linkare.com. If a protocol is not specified then both http and https can be used (default: *)
