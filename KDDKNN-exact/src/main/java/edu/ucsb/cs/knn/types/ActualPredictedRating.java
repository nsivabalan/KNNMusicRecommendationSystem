package edu.ucsb.cs.knn.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class ActualPredictedRating implements WritableComparable<ActualPredictedRating> {


	public int actualRating;
	public double predictedRating;

	public ActualPredictedRating() {}

	public ActualPredictedRating(int actualRating, double predictedRating) {
		this.actualRating = actualRating;
		this.predictedRating = predictedRating;
	}

	public int compareTo(ActualPredictedRating other) {
		if (this.actualRating < other.actualRating)
			return 1;
		else if (this.predictedRating > other.predictedRating)
			return -1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return actualRating + " " + predictedRating;
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(actualRating);
		out.writeDouble(predictedRating);
	}

	public void readFields(DataInput in) throws IOException {
		actualRating = in.readInt();
		predictedRating = in.readDouble();	
	}
}
