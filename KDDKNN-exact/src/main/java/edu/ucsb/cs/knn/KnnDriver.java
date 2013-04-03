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

package edu.ucsb.cs.knn;

/*
 * Track1: Learning to predict users' ratings of musical items. Items can be tracks,
 * albums, artists and genres. Items form a hierarchy, such that each track belongs to
 * an album, albums belong to artists, and together they are tagged by genres.
 * 
 * Project Goal:
 *  - Boost performance for KNN generation.
 *  - Make sure code execution time is state-of-art compatiable.<br>
 *  - Demonstrate the scalability of the code using Yahoo dataset.<br>
 *  
 *  Dataset:
 *  - Training set: used to build the knn of each song. 
 *  - Validation set: used to query the above results and validate it's producing correct values.
 *  - Test set: now that the contest is over, it's similar to validation (used to be hidden and used
 *  for choosing winners and calculating errors in prediction-RMSE).
 */
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.ProgramDriver;

import edu.ucsb.cs.knn.hybrid.HybridMain;
import edu.ucsb.cs.knn.inverted.InvertedMain;
import edu.ucsb.cs.knn.preprocess.SongInvertedIndexMain;
import edu.ucsb.cs.knn.preprocess.UserForwardIndexMain;
import edu.ucsb.cs.knn.query.QueryMain;
import edu.ucsb.cs.knn.query.RMSEMain;
import edu.ucsb.cs.knn.query.QueryAllMain;
import edu.ucsb.cs.knn.types.SequenceReader;

/**
 * The starting point of execution. This class present the job options available
 * to run then execute user's choice .
 * 
 * @author Sivabalan Narayanan and Vivek Goswami
 * 
 */
public class KnnDriver {

	public static final String INPUT_DIR_PROPERTY = "knn.input.dir";
	public static final String OUTPUT_DIR_PROPERTY = "knn.output.dir";
	public static final String QUERY_DIR_PROPERTY = "knn.query.dir";
	public static final String RESULT_DIR_PROPERTY = "knn.result.dir";
	public static final String THRESHOLD_PROPERTY = "knn.sim.threshold";
	public static final float THRESHOLD_VALUE = 0.85f;

	/**
	 * Presents the available job options for the user to choose from. Then
	 * invoke the class responsible for configuring the job.
	 */
	public static void main(String argv[]) {

		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();
		try {
			pgd.addClass("invindex", SongInvertedIndexMain.class,
					"Generate song inverted index for Yahoo data.");
			pgd.addClass("forindex", UserForwardIndexMain.class,
					"Generate user forward index for Yahoo data.");
			pgd.addClass("invertedknn", InvertedMain.class, " knn over songs inverted index.");
			pgd.addClass("hybridknn", HybridMain.class,
					" not yet done ...knn over songs forward index.");
			// pgd.addClass("estimateknn", InvertedMain.class, " TO-DO.");
			pgd.addClass("readseq", SequenceReader.class, " View sequence file as text to read.");
			//pgd.addClass("queryknn", QueryMain.class, " Query the KNN results for recommendation.");
			pgd.addClass("queryknn", QueryAllMain.class, " Query the KNN results for recommendation.");
			pgd.addClass("rmse", RMSEMain.class, " Calculate the RMSE value");
			pgd.driver(argv);
			exitCode = 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.exit(exitCode);
	}

	/**
	 * Submit the configured job to Hadoop JobTracker to start the process.
	 */
	public static void run(JobConf job) throws IOException {

		job.setJarByClass(KnnDriver.class); // This method sets the jar
		String ret = stars() + "\nKnnDriver(" + job.getJobName() + ")\n" + "  Input Path:  {";
		Path inputs[] = FileInputFormat.getInputPaths(job);
		for (int ctr = 0; ctr < inputs.length; ctr++) {
			if (ctr > 0) {
				ret += "\n                ";
			}
			ret += inputs[ctr].toString();
		}
		ret += "}\n";
		ret += "  Output Path: " + FileOutputFormat.getOutputPath(job) + "\n" + "  Map Tasks:    "
				+ job.getNumMapTasks() + "\n" + "  Reduce Tasks: " + job.getNumReduceTasks() + "\n";
		ret += "  Threshold:    " + job.getFloat(THRESHOLD_PROPERTY, THRESHOLD_VALUE) + "\n";
		System.out.println(ret);
		//
		// run job
		//
		JobClient.runJob(job);
	}

	/**
	 * Prints stars to the output console.
	 */
	public static String stars() {
		return new String(new char[77]).replace("\0", "*");
	}

}
