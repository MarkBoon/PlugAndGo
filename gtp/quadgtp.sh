#!/bin/sh

BLACK="nice java -server -Xmx240M -jar GoEngineGTP.jar Bot3"
WHITE="nice java -server -Xmx240M -jar GoEngineGTP.jar Bot4"

nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 5000 -size 9 -alternate -auto -sgffile games/testA &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 5000 -size 9 -alternate -auto -sgffile games/testB &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 5000 -size 9 -alternate -auto -sgffile games/testC &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 5000 -size 9 -alternate -auto -sgffile games/testD &

