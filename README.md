# Assinare

## Requirements

This project has the following requirements:

- Apache Maven 3.6.3+;
- OpenJDK 11 (as the main JDK to be used by Maven);
- Oracle JDK 8;
- Toolchains configuration for the required Java installations (see the **Maven configuration** topic below);
- Maven repository configuration for CEF eSignature DSS library (see the **Maven configuration** topic below);
- Docker client;
- MOCCA smcc library (see the **Build Mocca smcc** topic below);
- Graphical environment (required for some of the Maven tests);

## Maven configuration

### CEF eSignature DSS repository

In order to be able to fetch [CEF eSignature DSS library](https://github.com/esig/dss) dependencies, configure the following repository on your Maven **settings.xml** file:

  ```xml
  <repository>
    <id>cefdigital</id>
    <name>cefdigital</name>
    <url>https://ec.europa.eu/cefdigital/artifact/content/repositories/esignaturedss/</url>
  </repository>
  ```

### Toolchains

Assinare requires the use of both JDK 8 and 11 through Maven toolchains plugin. In order to configure this plugin, configure your Maven **toolchains.xml** file with a configuration similar to the following (adapt the **jdkHome** settings to reflect your system JDK installation directories):

  ```xml
  <?xml version="1.0" encoding="UTF8"?>
  <toolchains>
    <toolchain>
      <type>jdk</type>
      <provides>
        <version>1.8</version>
        <vendor>oracle</vendor>
      </provides>
      <configuration>
        <jdkHome>/usr/lib/jvm/jdk1.8.0_301</jdkHome>
      </configuration>
    </toolchain>
    <toolchain>
      <type>jdk</type>
      <provides>
        <version>11</version>
        <vendor>openjdk</vendor>
      </provides>
      <configuration>
        <jdkHome>/usr/lib/jvm/java-11-openjdk-amd64</jdkHome>
      </configuration>
    </toolchain>
  </toolchains>
  ```

## Build Mocca smcc

Assinare depends on a fork of [MOCCA](https://joinup.ec.europa.eu/site/mocca/) smcc library. In order to install this dependency, follow this steps:

1. Get the repository code:

  ```bash
  git clone https://github.com/linkareti/mocca-carddata.git
  ```

1. Checkout the correct version:

  ```bash
  cd mocca-carddata
  git checkout moccaCardDataRetrieval-1.3.18.carddata.4
  ```

1. Build the library and its parent (adapt the path to your system Oracle JDK 8 installation):

  ```bash
  cd smcc
  JAVA_HOME=/usr/lib/jvm/jdk1.8.0_301 mvn clean install
  cd ..
  JAVA_HOME=/usr/lib/jvm/jdk1.8.0_301 mvn clean install -am -pl :smcc
  ```

## Compiling the project

In order to compile the projet execute the following:

  ```bash
  mvn clean install
  ```

To generate artifacts signed by your own certificates add the following properties to maven execution:

- **keystore.path**: path to the keystore with the certificate keys
- **keystore.password**: keystsore password
- **keystore.alias**: keystore certificate alias

When installing Assinare in remote environments, add the following properties to maven execution:

- **host.name**: a list of hostnames, separated by spaces, that can execute the application. This value will be used in the JARs Manifest codebase attribute. If a protocol is not specified then both http and https can be used (default: <http://localhost:8080>)
- **jnlp.codebase**: the base url to which all the JNLP relative urls will reference. If an empty value is specified then all the JNLP relative urls will reference the url used to access the JNLP (default: <http://localhost:8080/assinare-web>)
- **web-start-launcher.url**: the url where the web start launcher application is deployed (default: <http://localhost:8080/web-start-launcher>)
- **assinare-web.url**: the url where the assinare web application is deployed (default: <http://localhost:8080/assinare-web>)
- **Caller-Allowable-Codebase**: the domains, separated by spaces, that can execute the application. If an asterisk (\*) wildcard is used then . If a protocol is not specified then both http and https can be used (default: \*)
- **Application-Library-Allowable-Codebase**: a list of hostnames, separated by spaces, that can host the application. An asterisk (\*) can be used as a wildcard to specify all domains or as a specific domain wildcard, as in \*.linkare.com. If a protocol is not specified then both http and https can be used (default: \*)

## Licensing

Assionare is licensed under the European Union Public Licence (EUPL), version 1.2. See [LICENSE](LICENSE) for the full license text.
