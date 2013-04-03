
package edu.ucsb.cs.knn.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class UserSongRatingPair implements WritableComparable<UserSongRatingPair> {

	public long userId;
	public long songId;
	public int rating;
	
	public UserSongRatingPair() {}

	public UserSongRatingPair(long userId, long songId, int rating) {
		this.userId = userId;
		this.songId = songId;
		this.rating = rating;
	}

	public int compareTo(UserSongRatingPair other) {
		if (this.userId < other.userId)
			return 1;
		else if (this.songId > other.songId)
			return -1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return userId + " " + songId+" "+rating;
	}

	public void write(DataOutput out) throws IOException {
		out.writeLong(userId);
		out.writeLong(songId);
		out.writeInt(rating);
	}

	public void readFields(DataInput in) throws IOException {
		userId = in.readLong();
		songId = in.readLong();
		rating = in.readInt();
	}
}
