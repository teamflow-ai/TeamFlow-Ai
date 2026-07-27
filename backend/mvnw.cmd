@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@echo off
@setlocal

set WRAPPER_JAR=".mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES=".mvn\wrapper\maven-wrapper.properties"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set BASE_DIR=%~dp0

if not exist %BASE_DIR%%WRAPPER_JAR% (
  echo Downloading Maven Wrapper jar...
  for /f "tokens=1,* delims==" %%A in ('findstr /r "^wrapperUrl=" %BASE_DIR%%WRAPPER_PROPERTIES%') do set WRAPPER_URL=%%B
  powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile %BASE_DIR%%WRAPPER_JAR%"
)

if not "%JAVA_HOME%"=="" (
  set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
  set JAVACMD=java.exe
)

%JAVACMD% ^
  -classpath %BASE_DIR%%WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" ^
  %WRAPPER_LAUNCHER% %*

@endlocal
