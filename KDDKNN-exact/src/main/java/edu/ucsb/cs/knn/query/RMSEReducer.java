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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.io.MapFile;
import edu.ucsb.cs.knn.types.PostingUser;
import edu.ucsb.cs.knn.types.PostingSongArrayWritable;
import edu.ucsb.cs.knn.types.ActualPredictedRating;
import edu.ucsb.cs.knn.types.UserSongRatingPair;
import edu.ucsb.cs.knn.types.SongRatingPair;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.JobConf;
import edu.ucsb.cs.knn.KnnDriver;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.conf.Configuration;
import edu.ucsb.cs.knn.types.PostingUser;
import edu.ucsb.cs.knn.types.UserSongPair;
import edu.ucsb.cs.knn.types.PostingUserArrayWritable;
import edu.ucsb.cs.knn.types.NeighboursArrayWritable;
import edu.ucsb.cs.knn.types.Neighbour;
import edu.ucsb.cs.knn.types.PostingSong;

/**
 * The reducer takes in: KEY:&ltuserID&gt, VALUE: [&ltActualPrecitedRating&gt]+ <br>
 * and output: KEY:&ltdummyuserID&gt, VALUE: &ltRMSEvalue&gt <br>
 * 
 * @author Sivabalan Narayanan and Vivek Goswami
 * 
 */
public class RMSEReducer extends MapReduceBase implements
Reducer<LongWritable,DoubleWritable, LongWritable,DoubleWritable> {

	private DoubleWritable total = new DoubleWritable();

	public void reduce(LongWritable userId,Iterator<DoubleWritable> values,
			OutputCollector<LongWritable,DoubleWritable> output, Reporter reporter)
					throws IOException {

		double sum = 0.0;
		int count = 0;
		while (values.hasNext()) {
			sum += values.next().get();
			count++;
		}
		Double result = Math.pow((sum/count), 0.5);
		total.set(result);
		output.collect(userId,total);

	}

}
