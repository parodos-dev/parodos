set -e
export SERVER_PORT=8081
JAR=`ls ../notification-service/target/notification-service-1.0.*.jar`

if [ x`echo ${JAR}|wc -w` != x1 ] ; then
  echo Multiple versions of notification-service .jar found, exiting.
  exit
fi

echo Starting: ${JAR}
java -jar -Dspring.profiles.active=local ${JAR}
#    -Dloader.path=../workflow-examples/target/workflow-examples-1.0.4-SNAPSHOT-jar-with-dependencies.jar  \
