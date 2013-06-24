#!/bin/sh

BLACK="nice java -server -Xmx480M -jar GoEngineGTP.jar Bot3"
WHITE="nice java -server -Xmx480M -jar GoEngineGTP.jar Bot5"

nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 2000 -size 9 -alternate -auto -sgffile games/testA &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 2000 -size 9 -alternate -auto -sgffile games/testB &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 2000 -size 9 -alternate -auto -sgffile games/testC &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 2000 -size 9 -alternate -auto -sgffile games/testD &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 2000 -size 9 -alternate -auto -sgffile games/testE &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 2000 -size 9 -alternate -auto -sgffile games/testF &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 2000 -size 9 -alternate -auto -sgffile games/testG &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/testH &

