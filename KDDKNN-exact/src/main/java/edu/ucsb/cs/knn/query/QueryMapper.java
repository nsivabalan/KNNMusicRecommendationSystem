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
import org.apache.hadoop.io.Text;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import edu.ucsb.cs.knn.KnnDriver;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import edu.ucsb.cs.knn.types.NeighboursArrayWritable;
import edu.ucsb.cs.knn.types.SongRatingPair;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import edu.ucsb.cs.knn.types.PostingUser;
import edu.ucsb.cs.knn.types.PostingUserArrayWritable;
import edu.ucsb.cs.knn.types.UserSongPair;

/**
 * This assumes the query text file of user information (taken from the input)
 * is stored in ../data/query. This will be copied to "query" in hdfs.
 * 
 * @author Sivabalan and Vivek
 * 
 */
public class QueryMapper extends MapReduceBase implements Mapper<Object, Text, LongWritable, SongRatingPair> {

	int nRatings = 0;
	long userId ;

	public void map(Object unused, Text line,OutputCollector<LongWritable, SongRatingPair> output,
			Reporter reporter)
					throws IOException {

		if(line.toString().contains("|"))
		{
			StringTokenizer str = new StringTokenizer(line.toString(), " |");
			userId = Long.parseLong(str.nextToken());
		}
		else{
			StringTokenizer str = new StringTokenizer(line.toString(), " \t");
			long songId = Long.parseLong(str.nextToken());
			int rate = Integer.parseInt(str.nextToken());
			System.out.println("User "+userId +", Song id "+songId +" Rating "+rate);
			output.collect(new LongWritable(userId),  new SongRatingPair(songId,rate));
		}
	}

}
