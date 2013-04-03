
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
import java.util.HashMap;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;

import edu.ucsb.cs.knn.types.PostingUser;
import edu.ucsb.cs.knn.types.PostingSongArrayWritable;
import edu.ucsb.cs.knn.types.ActualPredictedRating;
import edu.ucsb.cs.knn.types.UserSongRatingPair;
import edu.ucsb.cs.knn.types.SongRatingPair;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.JobConf;
import edu.ucsb.cs.knn.KnnDriver;
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
 * The reducer takes in: KEY:&ltuserID&gt, VALUE: [&ltSongRatingPair&gt]+ <br>
 * and output: KEY:&ltUserSongPair&gt, VALUE: &ltActualPredictedRating&gt <br>
 * 
 * @author Sivabalan and Vivek
 * 
 */

public class QueryReducer extends MapReduceBase implements
Reducer<LongWritable,SongRatingPair, UserSongPair, ActualPredictedRating> {

	
	MapFile.Reader reader ;
	LongWritable key ;
	private static PostingSong[] SongsIndex;
	private static Neighbour[] neighborhood;
	private HashMap<Long,Neighbour[]> neighbormap;
	private HashMap<Long,PostingSong[]> songUsermap;
	private static boolean isLoaded = false;

	public void reduce(LongWritable userId,Iterator<SongRatingPair> songlist,
			OutputCollector<UserSongPair, ActualPredictedRating> output, Reporter reporter)
					throws IOException {

		double predictedValue = 0.0 ;

		while (songlist.hasNext()) {
			SongRatingPair temp = songlist.next();		
			String[] songRating = temp.toString().split("\\s+");
			try{
				predictedValue = getPredictedRating(userId.get(),Long.parseLong(songRating[0]));	
				//predictedValue = 1.0;
				UserSongPair userSongPair = new UserSongPair(userId.get(),Long.parseLong(songRating[0]));
				ActualPredictedRating actualPredictedRating = new ActualPredictedRating(Integer.parseInt(songRating[1]),predictedValue);
				output.collect(userSongPair,actualPredictedRating);
			}
			catch(InstantiationException e)
			{
				e.printStackTrace();
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}		

		}

	}


	public double getPredictedRating(Long userId, Long songId) throws InstantiationException,IllegalAccessException,IOException
	{
		double predictedValue = 0.0;
		double predictedRateUp = 0.0;
		double predictedRateDown = 0.0;

		System.out.println("User : "+userId+" Song "+songId);
		Configuration config = new Configuration();
		FileSystem hdfs = FileSystem.get(config);

			//list of neighbours for the current song (Ni)
		neighborhood = new Neighbour[12];
		SongsIndex = new PostingSong[2000];	

		if(!isLoaded){			
			neighbormap = new HashMap<Long,Neighbour[]>();
			SequenceFile.Reader reader = new SequenceFile.Reader(hdfs, new Path("knn-output/part-00000"),new Configuration());
			LongWritable key1 = (LongWritable) reader.getKeyClass().newInstance();
			NeighboursArrayWritable value = (NeighboursArrayWritable) reader.getValueClass().newInstance();
			while (reader.next(key1, value)){
				neighbormap.put(key1.get(),value.getPosting());
			}
			reader.close();
			
			songUsermap = new HashMap<Long,PostingSong[]>();
			SequenceFile.Reader reader1 = new SequenceFile.Reader(hdfs, new Path("uforwardindex/part-00000"),new Configuration());
			key1 = (LongWritable) reader1.getKeyClass().newInstance();
			PostingSongArrayWritable value1 = (PostingSongArrayWritable) reader1.getValueClass().newInstance();
			while (reader1.next(key1, value1)){
				songUsermap.put(key1.get(),value1.getPosting());
			}
			reader1.close();
			isLoaded = true;
		}		
		
		SongsIndex = songUsermap.get(userId);
		//get list of Ru : list of songs rated by the current user
		ArrayList<Long> songList = new ArrayList<Long>();
		if(SongsIndex != null){
			PostingSong[] songIds = SongsIndex;
		if(songIds != null){
			int nsongs = songIds.length;
			for (int i = 0; i < nsongs ; i++) {
				System.out.print(" "+songIds[i].id);
				if(songIds[i].id == songId)
					return songIds[i].rate;
				songList.add(songIds[i].id);
			}
			System.out.println();
		}
		
		//intersection of Ru and Ni
		ArrayList<Long> result = new ArrayList<Long>();

		neighborhood = neighbormap.get(songId);
		if(neighborhood != null){
			System.out.println("List  of neighbours "+neighborhood.toString());

			//finding intersection of Ru and Ni		
			Neighbour[] neighbourIds = neighborhood;
			if(neighbourIds != null) {
				int nNeighbours = neighbourIds.length;
				System.out.println("Neighbours  ******** ");
				for (int i = 0; i < nNeighbours; i++) {
					System.out.print("Neigbour "+neighbourIds[i].songjId);
					if(songList.contains(neighbourIds[i].songjId)){
						System.out.print(" : Found match ");
						result.add(neighbourIds[i].songjId);
					}
					System.out.println();
				}
			}


			for(Long neighborId : result)
			{
				int currentUserRate = 0;
				if(songIds != null){
					int nsongs = songIds.length;
					for(int j = 0; j < nsongs ; j++) {
						if(neighborId == (songIds[j].id)) {
							currentUserRate = songIds[j].rate;
							break;
						}
					}
				}

				double wij = 0;
				int nNeighbours = neighbourIds.length;
				for (int i = 0; i < nNeighbours; i++) {
					if(neighbourIds[i].songjId == neighborId)
						wij = neighbourIds[i].wij;
				}
					

				predictedRateUp += currentUserRate * wij;
				predictedRateDown += Math.abs(wij);
			}
		}
		}

		if(!(predictedRateDown < 0.0001 && predictedRateDown > -.0001 ))
			predictedValue = predictedRateUp/ predictedRateDown;
		return predictedValue;
	}



}
