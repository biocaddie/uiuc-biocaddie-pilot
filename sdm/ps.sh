#! /bin/bash

items=(0.05 0.10 0.15 0.20 0.25 0.30 0.35 0.40 0.45 0.50 0.55 0.60 0.65 0.70 0.75 0.80 0.85 0.90 0.95 1.0)

n=${#items[@]}
powersize=$((1 << $n))

i=0
while [ $i -lt $powersize ]
do
    subset=()
    j=0
    while [ $j -lt $n ]
    do
        if [ $(((1 << $j) & $i)) -gt 0 ]
        then
            subset+=("${items[$j]}")
        fi
        j=$(($j + 1))
    done
    k=${#subset[@]}
    if [ $k == 3 ] 
    then
       IFS='+' sum=$(echo "scale=1;${subset[*]}"|bc)
       if [ $sum == "1.00" ] 
       then 
          echo "'${subset[@]}'"
       fi 
    fi
    i=$(($i + 1))
done
