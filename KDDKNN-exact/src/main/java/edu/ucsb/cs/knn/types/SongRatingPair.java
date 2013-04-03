
package edu.ucsb.cs.knn.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class SongRatingPair implements WritableComparable<SongRatingPair> {

	public long songId;
	public int rating;
	
	public SongRatingPair() {}

	public SongRatingPair( long songId, int rating) {
		this.songId = songId;
		this.rating = rating;
	}

	public int compareTo(SongRatingPair other) {
		if (this.songId < other.songId)
			return 1;
		else if (this.rating > other.rating)
			return -1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return songId+" "+rating;
	}

	public void write(DataOutput out) throws IOException {
		out.writeLong(songId);
		out.writeInt(rating);
	}

	public void readFields(DataInput in) throws IOException {

		songId = in.readLong();
		rating = in.readInt();
	}
}
