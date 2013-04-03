#!/bin/bash

dataDir=../data/track1viv/trainIdx1.txt
queryFile=../data/track1viv/validationIdx1000.txt
#knnoutput=/home/cs240a-ucsb-8/old_result/knn-output/
#userforwardindex=/home/cs240a-ucsb-8/old_result/uforwardindex/
#dataDir=../data/three
#queryFile=../data/testthree.txt
resultFile=../data/query/result
xmlconf=src/main/resources/kddknn/conf.xml
knnjar=target/kddknn.jar

		
export MY_HADOOP_HOME="/opt/hadoop/hadoop-0.20.2/contrib/myHadoop"
export HADOOP_HOME="/opt/hadoop/hadoop-0.20.2"
export HADOOP_CONF_DIR="/home/cs240a-ucsb-35/log/ConfDir"
echo "Set up the configurations for myHadoop please..."
$MY_HADOOP_HOME/bin/configure.sh -n 8 -c $HADOOP_CONF_DIR
echo "Format HDFS"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR namenode -format
echo
echo "Start all Hadoop daemons"
$HADOOP_HOME/bin/start-all.sh
echo "Clear Result folder"
rm -rf /home/cs240a-ucsb-35/result/sinvertedindex/*
rm -rf /home/cs240a-ucsb-35/result/uforwardindex/*
rm -rf /home/cs240a-ucsb-35/result/knn-output/*
rm -rf /home/cs240a-ucsb-35/result/resultindex/*
rm -rf /home/cs240a-ucsb-35/result/rmseindex/*
echo "Copy data to HDFS .. "
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyFromLocal $dataDir knn-input 
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyFromLocal $queryFile query
#$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyFromLocal $knnoutput knn-output
#$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyFromLocal $userforwardindex uforwardindex
echo "Compile"
ant clean
ant
echo "Run log analysis job .."
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar $knnjar invindex -conf $xmlconf
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar $knnjar forindex -conf $xmlconf
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -ls uforwardindex
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar $knnjar invertedknn -conf $xmlconf
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -ls knn-output
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar $knnjar queryknn -conf $xmlconf
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar $knnjar rmse -conf $xmlconf
time $HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -ls rmseindex
echo "Check output files after PC.. but i remove the old output data first"

#$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyToLocal  sinvertedindex/* ~/result/sinvertedindex/
#$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyToLocal  uforwardindex/* ~/result/uforwardindex/
#$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyToLocal  knn-output/* ~/result/knn-output/
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyToLocal  resultindex/* ~/result/resultindex/
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -copyToLocal  rmseindex/* ~/result/rmseindex/
cp -r $HADOOP_HOME/logs ~/logs/
echo "Stop all Hadoop daemons"
$HADOOP_HOME/bin/stop-all.sh
echo
echo "Clean up .."
$MY_HADOOP_HOME/bin/cleanup.sh -n 8
echo
