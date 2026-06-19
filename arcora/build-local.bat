@echo off
setlocal

rem Local helper for machines where java.exe is not on PATH.
rem Android Studio normally bundles a JDK at this location.
if not defined JAVA_HOME (
  if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
  )
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo ERROR: JAVA_HOME is not set and Android Studio JBR was not found.
  echo Set JAVA_HOME to a JDK 17+ installation, then rerun this script.
  exit /b 1
)

if not defined ANDROID_HOME (
  if exist "%LOCALAPPDATA%\Android\Sdk\platforms" (
    set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
  )
)

if not exist "%ANDROID_HOME%\platforms" (
  echo ERROR: ANDROID_HOME is not set and Android SDK was not found.
  echo Install Android SDK Platform 35+ or set ANDROID_HOME to your SDK path.
  exit /b 1
)

pushd "%~dp0"
call "%~dp0gradlew.bat" :app:assembleDebug --stacktrace --console=plain
set BUILD_EXIT=%ERRORLEVEL%
popd

exit /b %BUILD_EXIT%
