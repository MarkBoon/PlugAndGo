#!/bin/sh

BLACK="nice java -ea -server -Xmx240M -jar GoEngineGTP.jar Bot3"
WHITE="nice java -server -Xmx240M -jar GoEngineGTP.jar Bot4"

java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/test &
