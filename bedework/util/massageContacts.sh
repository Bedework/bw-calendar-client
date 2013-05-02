#! /bin/bash -f

rejectList=("Davidr*n Taylor" unnamed)
inputFile=$1
#inputFile=MimisContacts.txt
tmpFile=/tmp/MimisContactsMassaged$$.txt
# the resulting file is pumped out via stdout

sed -e '/ADR;TYPE=WORK:;;;;;;$/d' -e 's/$//' -e '/ADR;TYPE=HOME:;;;;;;$/d' -e '/ADR;TYPE=X-ORACLE-OTHER:;;;;;;$/d' -e 's/VERSION:3.0$/VERSION:4.0/' $inputFile > $tmpFile


while read line; do 
   
  case "$line" in
     ^$)
       # toss empty lines
       ;;
     BEGIN:VCARD)
       # save the Begin line
       beginLine=$line
       ;;
     N:*)
       # save N: lines for examination
       nLine=$line
       cardBuffer=${cardBuffer}${line}%%
       ;;
     ORG:*)
       # save ORG: lines for examination
       orgLine=$line
       cardBuffer=${cardBuffer}${line}%%
       ;;
     FN:*)
       # save FN: lines for examination
       fnLine=$line
       cardBuffer=${cardBuffer}${line}%%
       ;;
     END:VCARD)
       #process what we have
       # echo out the begin line
       echo $beginLine
       if [ x"$fnLine" = x ]; then
          # skip over the rejects
          for x in $rejectList; do
            if [ `grep "$x" $fnLine` ] ; then
              continue
            fi
          done
          # mock up an FN and spit it out
          if [ "$nLine" != "N:;;;;" ]; then
            newFnLine=`echo $nLine | sed 's/N://'`
            lastName=`echo "$newFnLine" | cut -d\; -f1`
            firstName=`echo "$newFnLine" | cut -d\; -f2`
            echo "FN:$firstName $lastName" | sed 's/FN: /FN:/'
          else
            newFnLine=`echo $orgLine | sed 's/ORG: /FN:/' |  sed 's/FN: /FN:/'`
          fi
          
       fi
       # echo out the rest of the lines
       echo -n $cardBuffer | sed 's/%%/\n/g'
       #echo out the end line
       echo $line
       fnLine=""
       cardBuffer=""
       nLine=""
       orgLine=""
       ;;
     *)
       cardBuffer=${cardBuffer}${line}%%
       ;;
esac      

done < $tmpFile





