:: Run the bedework indexer program

:: JAVA_HOME needs to be defined

@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

if not "%JAVA_HOME%"=="" goto noJavaWarn
ECHO
ECHO
ECHO ***************************************************************************
ECHO          Warning: JAVA_HOME is not set - results unpredictable
ECHO ***************************************************************************
ECHO
ECHO
:noJavaWarn

SET cp=.;./classes;./resources
FOR /f %%i IN ('dir /b lib\*.jar') DO SET cp=!cp!;./lib/%%i

SET RUNCMD="%JAVA_HOME%\bin\java" -cp %cp% org.bedework.indexer.BwIndexApp

SET APPNAME=indexer

ECHO.
ECHO.
ECHO   Bedework Lucene Reindexer
ECHO   -------------------------
ECHO.

:branch
  if "%1" == "reindex" GOTO reindex
  if "%1" == "reindex-nostart" GOTO reindex-nostart
  if "%1" == "start" GOTO start

:usage
  ECHO   Usage:
  ECHO.
  ECHO  %0 reindex
  ECHO     Reindex the system then process queue events"
  ECHO  %0 reindex-nostart
  ECHO     Reindex the system. Do not process queue events"
  ECHO  %0 start
  ECHO     Process queue events"
  ECHO.

  GOTO:EOF


:reindex
  ECHO   %RUNCMD% -appname %APPNAME% -crawl -listen %2 %3 %4 %5 %6 %7 %8 %9
  %RUNCMD% -appname %APPNAME% -crawl -listen %2 %3 %4 %5 %6 %7 %8 %9
  GOTO:EOF

:reindex-nostart
  ECHO   %RUNCMD% -appname %APPNAME% -crawl %2 %3 %4 %5 %6 %7 %8 %9
  %RUNCMD% -appname %APPNAME% -crawl %2 %3 %4 %5 %6 %7 %8 %9
  GOTO:EOF

:start
  ECHO   %RUNCMD% -appname %APPNAME% -listen %2 %3 %4 %5 %6 %7 %8 %9
  %RUNCMD% -appname %APPNAME% -listen %2 %3 %4 %5 %6 %7 %8 %9
  GOTO:EOF

