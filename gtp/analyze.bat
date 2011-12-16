del games\*.summary.dat
del games\total.dat
type games\*.dat > games\total.dat
java -jar gogui-twogtp.jar -force -analyze games\total.dat
