#!/bin/sh

#BLACK="nice java -Xmx512M -jar GoEngineGTP.jar PatternMonteCarloTreeSearchBot2K"
#WHITE="nice java -Xmx240M -jar GoEngineGTP.jar MonteCarloTreeSearchBot2K"

BLACK="nice java -server -Xmx240M -jar GoEngineGTP.jar MonteCarloTreeSearchBot2K"
WHITE="nice java -server -Xmx240M -jar GoEngineGTP.jar MonteCarloTreeSearchBot4K"

java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/test &
