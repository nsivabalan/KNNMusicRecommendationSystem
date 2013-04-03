/**
 * Copyright 2012-2013 The Regents of the University of California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS"; BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under
 * the License.
 * 
 * Author: Sivabalan Narayanan <sivabalan (at) cs.ucsb.edu>
 *         Vivek Goswami <vivekgoswami (at) cs.ucsb.edu>
 * @Since Feb 20, 2013
 */



package edu.ucsb.cs.knn.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.GenericOptionsParser;
import edu.ucsb.cs.knn.types.PostingUser;
import edu.ucsb.cs.knn.types.PostingSongArrayWritable;
import edu.ucsb.cs.knn.types.ActualPredictedRating;
import edu.ucsb.cs.knn.types.UserSongRatingPair;
import edu.ucsb.cs.knn.types.SongRatingPair;

import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import edu.ucsb.cs.knn.types.NeighboursArrayWritable;
import edu.ucsb.cs.knn.preprocess.NonSplitableTextInputFormat;
import edu.ucsb.cs.knn.KnnDriver;
import org.apache.hadoop.mapred.TextOutputFormat;
import edu.ucsb.cs.knn.types.UserSongPair;


public class QueryAllMain {

		
	public static void main(String[] args) throws Exception {
		JobConf job = new JobConf();
		job.set("mapred.child.java.opts", "-Xmx8192m -Xincgc");
		new GenericOptionsParser(job, args);
		job.setJarByClass(QueryAllMain.class);
		job.setJobName(QueryAllMain.class.getSimpleName());

		job.setMapperClass(QueryMapper.class);
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(SongRatingPair.class);
		
		job.setReducerClass(QueryReducer.class);
		job.setOutputKeyClass(UserSongPair.class);
		job.setOutputValueClass(ActualPredictedRating.class); 
		
		// try MultiFileInputFormat
		Path inputPath = new Path(job.get(KnnDriver.QUERY_DIR_PROPERTY));
		if (inputPath == null)
			throw new UnsupportedOperationException("ERROR: query directory not set");
		job.setInputFormat(NonSplitableTextInputFormat.class);
		NonSplitableTextInputFormat.addInputPath(job, inputPath);
		Path outputPath = new Path(job.get(KnnDriver.RESULT_DIR_PROPERTY));
		FileSystem.get(job).delete(outputPath, true);
		// Change to FileOutputFormat to see output
		
		job.setOutputFormat(TextOutputFormat.class);
		TextOutputFormat.setOutputPath(job, outputPath);

		KnnDriver.run(job);
	}
	
}
