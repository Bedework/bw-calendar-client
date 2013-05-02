#!/bin/sh

# Run the bedework system event logger program

# JAVA_HOME needs to be defined

cp=.:./classes:./resources

for i in lib/*
  do
    cp=$cp:$i
done

# Create a a temp dir
TEMPDIR="./temp"
mkdir $TEMPDIR

RUNCMD="$JAVA_HOME/bin/java -Djava.io.tmpdir="$TEMPDIR" -cp $cp org.bedework.sysevents.listeners.LogListener"

APPNAME=srvevlog

case "$1" in
  start)
    echo $RUNCMD -appname $APPNAME $2 $3 $4 $5 $6 $7 $8 $9
    $RUNCMD -appname $APPNAME $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  *)
    echo $" "
    echo $"Usage: "
    echo $"  $0 start [(-ndebug | -debug)] "
    echo $"     Start logging the system events"
    echo $" "
esac

