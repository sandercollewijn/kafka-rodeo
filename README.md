Use java11, if not default on the classpath use

~/.gradle/gradle.properties and set
org.gradle.java.home=<path-to-jdk>
#e.g. org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home

--- Build ---
./gradlew clean build

--- Cluster setup files ---
kafka/config

--- Kafka Rodeo scenario's  ---
kafka/docs
kafka/case
