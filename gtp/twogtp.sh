#!/bin/sh

#BLACK="nice java -Xmx512M -jar GoEngineGTP.jar PatternMonteCarloTreeSearchBot2K"
#WHITE="nice java -Xmx240M -jar GoEngineGTP.jar MonteCarloTreeSearchBot2K"

BLACK="nice java -server -Xmx1000M -jar GoEngineGTP.jar SocketEngine"
WHITE="nice java -server -Xmx240M -jar GoEngineGTP.jar MonteCarloTreeSearchBot2K"

java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/test &
