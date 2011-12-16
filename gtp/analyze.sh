rm games/*.summary.dat
rm games/total.dat
cat games/*.dat > games/total.dat
java -jar gogui-twogtp.jar -force -analyze games/total.dat

