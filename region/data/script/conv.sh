#!/bin/bash
for i in $(find . -name "*.csv")
do
   echo "converting $i"
   iconv -c -f UNICODE $i  | sed "s/^[^A-Za-z0-9,]//" > $i.conv
   rm $i
   mv $i.conv $i
done