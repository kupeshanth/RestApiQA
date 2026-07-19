@REM Maven Wrapper for Windows
@REM Use this instead of "mvn" if you don't have Maven installed globally
@REM Example: mvnw.cmd test

@echo off
setlocal

set MAVEN_WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
set MAVEN_WRAPPER_PROPERTIES="%~dp0.mvn\wrapper\maven-wrapper.properties"
set DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

if not exist %MAVEN_WRAPPER_JAR% (
    echo Downloading Maven Wrapper JAR...
    powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile %MAVEN_WRAPPER_JAR%"
)

"%JAVA_HOME%\bin\java.exe" -jar %MAVEN_WRAPPER_JAR% %*
if "%JAVA_HOME%" == "" java -jar %MAVEN_WRAPPER_JAR% %*
