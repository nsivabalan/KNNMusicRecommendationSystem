package edu.ucsb.cs.knn.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class UserSongPair implements WritableComparable<UserSongPair> {

	public long userId;
	public long songId;
	
	public UserSongPair() {}

	public UserSongPair(long userId, long songId) {
		this.userId = userId;
		this.songId = songId;
	}

	public int compareTo(UserSongPair other) {
		if (this.userId < other.userId)
			return 1;
		else if (this.songId > other.songId)
			return -1;
		else
			return 0;
	}

	@Override
	public String toString() {
		return userId + " " + songId;
	}

	public void write(DataOutput out) throws IOException {
		out.writeLong(userId);
		out.writeLong(songId);
	}

	public void readFields(DataInput in) throws IOException {
		userId = in.readLong();
		songId = in.readLong();
	}
}
