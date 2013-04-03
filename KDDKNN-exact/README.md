Contact : sivabalan@cs.ucsb.edu or vivekgoswami@cs.ucsb.edu

Execution of the project using run.sh

//execution of log.sh
// change the location of data to correct location
sh log.sh
//ensure result directory(and subdirectories) is present 
//change the confDir appropriately


//execution of run.sh to run locally

//dont forget change the location of data in run.sh

./run.sh clean 
//cleans data

./run.sh load
//loads input data

./run.sh invert
//runs songinvertedindex

./run.sh knn
//find similarities between all songs (invertedKnn)

./run.sh findex
//executes Userforwardindex

./run.sh query
//executes queryallmain (prediction)

./run.sh rmse
//executes rmseindex (finds rmse value)

./run.sh all 
//executes all the above mentioned steps in a single go

