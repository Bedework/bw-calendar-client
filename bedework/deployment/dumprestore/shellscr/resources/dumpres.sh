#!/bin/sh

# Run the bedework dump/restore program
# First arg defines the action, dump, restore etc
# Second arg should be the filename

# JAVA_HOME needs to be defined

cp=.:./classes:./resources

for i in lib/*
  do
    cp=$cp:$i
done

# Need a temp dir for hibernate cache
TEMPDIR="./temp"
mkdir $TEMPDIR

DUMPCMD="$JAVA_HOME/bin/java -Djava.io.tmpdir="$TEMPDIR" -cp $cp @DUMP-CLASS@"
RESTORECMD="$JAVA_HOME/bin/java -Djava.io.tmpdir="$TEMPDIR" -cp $cp @RESTORE-CLASS@"
SCHEMACMD="$JAVA_HOME/bin/java -Djava.io.tmpdir="$TEMPDIR" -cp $cp org.hibernate.tool.hbm2ddl.SchemaExport"

APPNAME=@BW-APP-NAME@


case "$1" in
  dump)
    echo $DUMPCMD -appname $APPNAME -f $2 $3 $4 $5 $6 $7 $8 $9
    $DUMPCMD -appname $APPNAME -f $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  restore)
    echo $RESTORECMD -appname $APPNAME -f $2 $3 $4 $5 $6 $7 $8 $9
    $RESTORECMD -appname $APPNAME -f $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  restore-for-quickstart)
    echo $RESTORECMD -appname $APPNAME -onlyusers "public-user,caladmin,douglm,agrp_*" -f $2 $3 $4 $5 $6 $7 $8 $9
    $RESTORECMD -appname $APPNAME -onlyusers "public-user,caladmin,douglm,agrp_*" -f $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  backup)
    TARGET=$2/$3`date +%Y%m%d_%H%M%S`.xml
    echo $DUMPCMD -appname $APPNAME -f $TARGET
    $DUMPCMD -appname $APPNAME -f $TARGET
    ;;
  initdb)
    echo $RESTORECMD -appname $APPNAME -f ./data/initbedework.xml -initSyspars $2 $3 $4 $5 $6 $7 $8 $9
    $RESTORECMD -appname $APPNAME -f ./data/initbedework.xml -initSyspars $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  newsys)
    echo $RESTORECMD -appname $APPNAME -newSystem -rootid $2 $3 $4 $5 $6 $7 $8 $9
    $RESTORECMD -appname $APPNAME -newSystem -rootid $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  drop)
    echo $SCHEMACMD --text --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
    $SCHEMACMD --text --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
    ;;
  drop-export)
    echo $SCHEMACMD --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql $2 $3 $4 $5 $6 $7 $8 $9
    $SCHEMACMD --drop --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  schema)
    echo $SCHEMACMD --text --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
    $SCHEMACMD --text --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql
    ;;
  schema-export)
    echo $SCHEMACMD --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql $2 $3 $4 $5 $6 $7 $8 $9
    $SCHEMACMD --create --format --delimiter="@SCHEMA-DELIMITER@" --config=./classes/hibernate.cfg.xml --output=schema.sql $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  *)
    echo $" "
    echo $"Usage: "
    echo $"  $0 dump <filename> [-ndebug] "
    echo $"     Dump the database in xml format suitable for restore."
    echo $" "
    echo $"  $0 restore <filename> [-ndebug]"
    echo $"     Restore the database from an xml formatted dump."
    echo $" "
    echo $"  $0 backup <directory> <prefix>} "
    echo $"     Dump the database in xml format suitable for restore."
    echo $"     Files will have a name built from the prefix and the current date/time."
    echo $" "
    echo $"  $0 initdb [ -ndebug] [-indexroot=<lucene-index-root>"
    echo $"     Populate the database using the provided initial data."
    echo $" "
    echo $"  $0 newsys <root-user> [ -ndebug]"
    echo $"     Create a new empty system based on the build configuration with a"
    echo $"     single root user."
    echo $" "
    echo $"  $0 drop [--haltonerror] "
    echo $"     Create a file in the current directory with sql drop statements"
    echo $" "
    echo $"  $0 drop-export [--haltonerror]"
    echo $"     Drop tables in the database. Note this may not work if the schema"
    echo $"     was changed."
    echo $" "
    echo $"  $0 schema [--haltonerror] "
    echo $"     Create a schema from the xml schema. Placed in a file in the current directory"
    echo $" "
    echo $"  $0 schema-export [--haltonerror]"
    echo $"     Create a schema from the xml schema."
    echo $"     Also create the database tables, indexes etc."
    echo $" "
esac

