#!/bin/sh

#BLACK="java -Xmx240M -jar GoEngineGTP.jar TestBot2K"
#BLACK="java -Xmx240M -jar GoEngineGTP.jar TestBot10K"
#WHITE="java -Xmx240M -jar GoEngineGTP.jar TesujiRefBot"
#WHITE="java -Xmx240M -jar GoEngineGTP.jar MonteCarloTreeSearchBot"

BLACK="nice java -server -Xmx240M -jar GoEngineGTP.jar Bot3"
WHITE="nice java -server -Xmx240M -jar GoEngineGTP.jar Bot4"

#BLACK="nice java -server -Xmx240M -jar GoEngineGTP.jar UCTBot16K"
#WHITE="nice java -server -Xmx240M -jar GoEngineGTP.jar UCTPatternBot16K"

nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/testA &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/testB &
nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/testC &
#nice java -jar gogui-twogtp.jar -verbose -black "$BLACK" -white "$WHITE" -games 10000 -size 9 -alternate -auto -sgffile games/testD &

