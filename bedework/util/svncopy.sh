#!/bin/bash

# Copy all the projects in one branch to a new location

usage() {
  echo "This script will copy the trunk (or named location) to a new location"
  echo " "
  echo " $0 help"
  echo " $0 (branch | tag | release) name comment-text [ from (branch | tag | release) name]"
  echo " "
  echo " par 1: branch tag or release specifies what kind of copy"
  echo " par 2: name e.g. my-copy or bedework-4.0.1"
  echo " par 3: comment text"
  echo " "
  echo " Examples:"
  echo '  svncopy branch my-copy "my personal branch"'
  echo '  svncopy release bedework-4.1 "New release"'
  echo " "
  exit
}

# Check a parameter is set
#
# par 1: branch/tag/release
#
checkbranchtag() {
  if [ "${1}x" = "x" ]
  then
      usage
  fi

  case "$1" in
    branch)
      BTR="branches"
      ;;
    tag)
      BTR="tags"
      ;;
    release)
      BTR="releases"
      ;;
    help)
      usage
      ;;
    *)
      echo " "
      echo "**** "
      echo "**** First parameter must be branch tag or release"
      echo "**** "
      echo " "
      usage
   esac
}

# Check a parameter is set
#
# par 1: Name of parameter
# par 2: value
#
check() {
  if [ "${2}x" = "x" ]
  then
    echo " "
    echo "**** "
    echo "**** Parameter $1 is not set"
    echo "**** "
    echo " "
    usage
  fi
}

# ------------------------------------------------------------------
# copyproject - copy a single project. 
#
# par 1 - project name
# par 2 - destination
# par 3 - comment
# par 4 - source
# ------------------------------------------------------------------
copyproject() {
  echo "copyproject $1 $2 from $4"

  echo "svn copy -m \"$3\" -rHEAD $SVNREPOSITORY/$1/$4 $SVNREPOSITORY/$1/$2"
  svn copy -m "$3" -rHEAD $SVNREPOSITORY/$1/$4 $SVNREPOSITORY/$1/$2
}

SOURCE="trunk"


echo "par 4 is \"$4\""
if [ "$4" = "from" ]
then
  checkbranchtag "$5"
  check "from-Name" "$6"
  SOURCE=$BTR/$6
fi

checkbranchtag "$1"
check "Name" "$2"
check "Comment" "$3"

SVNREPOSITORY="https://www.bedework.org/svn"
TARGET="$BTR/$2"
COMMENT="$3"

PROJECTS=""
PROJECTS="$PROJECTS access"
PROJECTS="$PROJECTS bedenote"
PROJECTS="$PROJECTS bedework"
PROJECTS="$PROJECTS bwannotations"
PROJECTS="$PROJECTS bwcalcore"
PROJECTS="$PROJECTS bwcaldav"
PROJECTS="$PROJECTS bwcalfacade"
PROJECTS="$PROJECTS bwdeployutil"
PROJECTS="$PROJECTS bwical"
PROJECTS="$PROJECTS bwinterfaces"
# PROJECTS="$PROJECTS bwmisc"
PROJECTS="$PROJECTS bwsysevents"
PROJECTS="$PROJECTS bwtools"
PROJECTS="$PROJECTS bwtzsvr"
PROJECTS="$PROJECTS bwwebapps"
PROJECTS="$PROJECTS bwxml"
PROJECTS="$PROJECTS cachedfeeder"
PROJECTS="$PROJECTS caldav"
PROJECTS="$PROJECTS caldavTest"
PROJECTS="$PROJECTS carddav"
PROJECTS="$PROJECTS clientapp"
# PROJECTS="$PROJECTS davutil"
PROJECTS="$PROJECTS dumprestore"
PROJECTS="$PROJECTS eventreg"
#  exchgGateway - later
PROJECTS="$PROJECTS indexer"
PROJECTS="$PROJECTS monitor"
PROJECTS="$PROJECTS naming"
PROJECTS="$PROJECTS rpiutil"
PROJECTS="$PROJECTS synch"
PROJECTS="$PROJECTS testsuite"
PROJECTS="$PROJECTS webdav"

for project in $PROJECTS
do
   copyproject "$project" "$TARGET" "$COMMENT" "$SOURCE"
done
