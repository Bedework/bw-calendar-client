:: Run the bedework dump/restore program
:: First arg defines the action, dump, restore etc
:: Second arg should be the filename

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

SET DUMPCMD="%JAVA_HOME%/bin/java" -cp %cp% @DUMP-CLASS@
SET RESTORECMD="%JAVA_HOME%/bin/java" -cp %cp% @RESTORE-CLASS@
SET SCHEMACMD="%JAVA_HOME%/bin/java" -cp %cp% org.hibernate.tool.hbm2ddl.SchemaExport

SET APPNAME=@BW-APP-NAME@

ECHO.
ECHO.
ECHO   Bedework Database Tools
ECHO   -----------------------
ECHO.

:branch
  if "%1" == "dump" GOTO dump
  if "%1" == "restore" GOTO restore
  if "%1" == "restore-for-quickstart" GOTO restore-for-quickstart
  if "%1" == "backup" GOTO backup
  if "%1" == "initdb" GOTO initdb
  if "%1" == "newsys" GOTO newsys
  if "%1" == "drop" GOTO drop
  if "%1" == "drop-export" GOTO drop-export
  if "%1" == "schema" GOTO schema
  if "%1" == "schema-export" GOTO schema-export

:usage
  ECHO   Usage:
  ECHO.
  ECHO     schema-export [--haltonerror]
  ECHO        Create a schema from the xml schema.
  ECHO        Also create the database tables, indexes etc.
  ECHO.
  ECHO     initdb [--indexroot={lucene-index-root}]
  ECHO        Populate the database using the provided initial data.
  ECHO.
  ECHO     newsys <root-user> [-ndebug]
  ECHO        Create a new empty system based on the build configuration with a
  ECHO        single root user
  ECHO.
  ECHO     dump {filename}
  ECHO        Dump the database in xml format suitable for restore.
  ECHO.
  ECHO     restore {filename}
  ECHO        Restore the database from an xml formatted dump.
  ECHO.
  ECHO     backup {directory} {prefix}
  ECHO        Dump the database in xml format suitable for restore.
  ECHO        Files will have a name built from the prefix and the current date/time.
  ECHO.
  ECHO     drop [--haltonerror]
  ECHO        Create a file in the current directory with sql drop statements
  ECHO.
  ECHO     drop-export [--haltonerror]
  ECHO        Drop tables in the database. Note this may not work if the schema
  ECHO        was changed.
  ECHO.
  ECHO     schema [--haltonerror]
  ECHO        Create a schema from the xml schema. Placed in a file in the
  ECHO        current directory
  ECHO.

  GOTO end


:dump
  ECHO   Dumping data:
  ECHO.
  ECHO   %DUMPCMD% -appname %APPNAME% -f %2 %3 %4 %5 %6 %7 %8 %9
  %DUMPCMD% -appname %APPNAME% -f %2 %3 %4 %5 %6 %7 %8 %9
  GOTO end
  ::
:restore
  ECHO   Restoring data:
  ECHO.
  ECHO   %RESTORECMD% -appname %APPNAME% -f %2 %3 %4 %5 %6 %7 %8 %9
  %RESTORECMD% -appname %APPNAME% -f %2 %3 %4 %5 %6 %7 %8 %9
  GOTO end
  ::
:restore-for-quickstart
  ECHO   Restoring data for quickstart:
  ECHO.
  ECHO   %RESTORECMD% -appname %APPNAME% -onlyusers "public-user,caladmin,douglm,agrp_*" -f %2 %3 %4 %5 %6 %7 %8 %9
  %RESTORECMD% -appname %APPNAME% -onlyusers "public-user,caladmin,douglm,agrp_*" -f %2 %3 %4 %5 %6 %7 %8 %9
  GOTO end
  ::
:backup
  :: first strip out delimeters from date and time
  echo %date% > temp.txt
  for /f "tokens=1-4 delims=/ " %%a in (temp.txt) do (
    set dname=%%a
    set mm=%%b
    set dd=%%c
    set yy=%%d
  )
  echo %time% > temp.txt
  for /f "tokens=1-3 delims=: " %%a in (temp.txt) do (
    set hh=%%a
    set mn=%%b
    set ss=%%c
  )
  echo %hh%%mn%%ss% > temp.txt
  for /f "tokens=1-2 delims=. " %%a in (temp.txt) do (
    set tm=%%a
    set ms=%%b
  )
  SET TARGET=%2\%3%yy%%mm%%dd%_%tm%%ms%.xml

  :: now backup the data
  ECHO   Backing up data into:
  ECHO   %TARGET%
  ECHO.
  ECHO   %DUMPCMD% -appname %APPNAME% -f %TARGET%
  %DUMPCMD% -appname %APPNAME% -f %TARGET%
  GOTO end
  ::

:initdb
  ECHO   Initializing the database:
  ECHO.
  ECHO   %RESTORECMD% -appname %APPNAME% -f ./data/initbedework.xml -initSyspars %2 %3 %4 %5 %6 %7 %8 %9
  %RESTORECMD% -appname %APPNAME% -f ./data/initbedework.xml -initSyspars %2 %3 %4 %5 %6 %7 %8 %9
  GOTO end
  ::

:newsys
  ECHO   Creating new system database:
  ECHO.
  ECHO   %RESTORECMD% -appname %APPNAME% -newSystem -rootid %2 %3 %4 %5 %6 %7 %8 %9
  %RESTORECMD% -appname %APPNAME% -newSystem -rootid %2 %3 %4 %5 %6 %7 %8 %9
  GOTO end
  ::

:drop
  ECHO   Creating drop sql
  ECHO.
  ECHO   %SCHEMACMD% --text --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  %SCHEMACMD% --text --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  GOTO end
  ::
:drop-export
  ECHO   Exporting drop sql:
  ECHO.
  ECHO   %SCHEMACMD% --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  %SCHEMACMD% --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  GOTO end
  ::
:schema
  ECHO   Creating the schema:
  ECHO.
  ECHO   %SCHEMACMD% --text --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  %SCHEMACMD% --text --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  GOTO end
  ::
:schema-export
  ECHO   Exporting the schema:
  ECHO.
  ECHO   %SCHEMACMD% --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  %SCHEMACMD% --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
  GOTO end
  ::

:end
ECHO.
ECHO.
