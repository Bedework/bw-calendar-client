:: Run the bedework system event logger program

:: JAVA_HOME needs to be defined

@echo off
setlocal

if not "%JAVA_HOME%"=="" goto noJavaWarn
ECHO
ECHO
ECHO ***************************************************************************
ECHO          Warning: JAVA_HOME is not set - results unpredictable
ECHO ***************************************************************************
ECHO
ECHO
:noJavaWarn

SET cp=@CP@

SET RUNCMD="%JAVA_HOME%/bin/java" -cp %cp% org.bedework.sysevents.listeners.LogListener

SET APPNAME=@BW-APP-NAME@

ECHO.
ECHO.
ECHO   Bedework Scheduler Task
ECHO   -----------------------
ECHO.

:branch
  if "%1" == "start" GOTO start

:usage
  ECHO   Usage:
  ECHO.
  ECHO     start
  ECHO        Start the logging program
  ECHO.

  GOTO end


:start
  ECHO   Starting logging:
  ECHO.
  ECHO   %RUNCMD% -appname %APPNAME% -f %2 %3 %4 %5 %6 %7 %8 %9
  %RUNCMD% -appname %APPNAME% -f %2 %3 %4 %5 %6 %7 %8 %9
  GOTO end
  ::

:end
ECHO.
ECHO.
