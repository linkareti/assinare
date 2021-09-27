#! /bin/sh

${GRAALVM_HOME:?}/bin/java -agentlib:native-image-agent=config-output-dir=native-image-agent-output -jar target/quarkus-app/quarkus-run.jar
