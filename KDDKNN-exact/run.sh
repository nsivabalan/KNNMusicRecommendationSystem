#!/bin/bash
#!/usr/bin/env bash
#############################################
# Usage: run.sh [[clean]]
#
# Run by itself, the script will populate the
# HDF with a dataset and process it. Then it
# will ask a song to query and retunr the
# result.
#
# clean: remove all files from the HDF made
# with this script.
#
# You have to put your data initially in the
# location specified by $dataDir of this
# script.For query, you need to put it in the
# location shown in $queryFile. Then just
# run the script.
#
# To see output:
# Since results are stored in hdfs in sequence
# format, you need to run the following to
# read them as:
# hjar target/kddknn.jar readseq <part-0000>
#############################################

# Commands:
CLEAN="clean"
LOAD="load"
INVERT="invert"
FINDEX="findex"
KNN="knn"
QUERY="query"
RMSE="rmse"
ALL="all"

#dataDir=/home/yahooData/ydata-ymusic-kddcup-2011-track1/trainIdx1.txt
#dataDir=../data/trainIdx1~1K.txt
dataDir=../data/three
queryFile=../data/query/u1
resultFile=../data/query/result
#JAVA_HOME=/usr/lib/jvm/jdk1.7.0
HADOOP1=$HADOOP/bin/hadoop
xmlconf=src/main/resources/kddknn/conf.xml
knnjar=target/kddknn.jar

## Start run script ##
clean() {
	$HADOOP1 dfs -rmr sinvertedindex
	$HADOOP1 dfs -rmr knn-output
#	$HADOOP1 dfs -rmr knn-input
	$HADOOP1 dfs -rmr query
	$HADOOP1 dfs -rmr resultindex
        $HADOOP1 dfs -rmr rmse
}

load() {
  $HADOOP1 dfs -put $dataDir knn-input
}

invert() {
  $HADOOP1 jar $knnjar invindex -conf $xmlconf
}

findex(){
$HADOOP1 jar $knnjar forindex -conf $xmlconf

}

knn() {
  $HADOOP1 jar $knnjar invertedknn -conf $xmlconf
}

query(){
	$HADOOP1 dfs -put $queryFile query
        $HADOOP1 jar $knnjar queryknn -conf $xmlconf
}

rmse() {
	$HADOOP1 jar $knnjar rmse -conf $xmlconf
}

temprmse(){
	$HADOOP1 dfs -put $resultFile resultindex
        $HADOOP1 jar $knnjar rmse -conf $xmlconf

}

if [[ $1 = $CLEAN ]]; then
	clean
	ant clean
	exit
fi

if [[ $1 = $LOAD ]]; then
	load
	exit
fi




# Build from source.
ant build

if [[ $1 = $INVERT ]]; then
	invert
	exit
fi

if [[ $1 = $KNN ]]; then
        knn
	exit
fi

if [[ $1 = $FINDEX ]] ; then
     findex
     exit
fi

if [[ $1 = $ALL ]]; then
	clean
	ant clean
	ant
	load
	invert
        findex
	knn
	query
        rmse
	exit
fi

if [[ $1 == $RMSE ]]; then
	rmse
	exit
fi

if [[ $1 = $QUERY ]]; then
	query
	exit
fi


