#!/bin/sh

# Run the bedework indexer program

# JAVA_HOME needs to be defined

cp=.:./classes:./resources

for i in lib/*
  do
    cp=$cp:$i
done

# Need a temp dir for hibernate cache
TEMPDIR="./temp"
mkdir $TEMPDIR

mkdir logs

RUNCMD="$JAVA_HOME/bin/java -Djava.io.tmpdir="$TEMPDIR" -cp $cp org.bedework.indexer.BwIndexApp"

APPNAME=indexer


case "$1" in
  reindex)
    echo $RUNCMD -appname $APPNAME -crawl -listen $2 $3 $4 $5 $6 $7 $8 $9
    $RUNCMD -appname $APPNAME -crawl -listen $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  reindex-nostart)
    echo $RUNCMD -appname $APPNAME -crawl $2 $3 $4 $5 $6 $7 $8 $9
    $RUNCMD -appname $APPNAME -crawl $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  start)
    echo $RUNCMD -appname $APPNAME -listen $2 $3 $4 $5 $6 $7 $8 $9
    $RUNCMD -appname $APPNAME -listen $2 $3 $4 $5 $6 $7 $8 $9
    ;;
  *)
    echo $" "
    echo $"Usage: "
    echo $"  $0 reindex "
    echo $"     Reindex the system then process queue events"
    echo $"  $0 reindex-nostart "
    echo $"     Reindex the system. Do not process queue events"
    echo $"  $0 start"
    echo $"     Process queue events"
    echo $" "
esac

