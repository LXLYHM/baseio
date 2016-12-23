cd ../baseio
call build-package.bat

cd ../baseio-test
call mvn clean compile -DskipTests

cd ../baseio-sample
call mvn clean package -DskipTests

copy target\baseio-sample*.jar ..\baseio-test\target\classes\http\app\_java_lib\

cd ../baseio-test

rem set MAVEN_OPTS=-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=6666,suspend=n

call mvn exec:java -Dexec.mainClass="com.generallycloud.test.nio.http11.TestHTTPServer"