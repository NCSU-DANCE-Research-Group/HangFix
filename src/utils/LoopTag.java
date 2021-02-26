package utils;

import soot.tagkit.Tag;

public class LoopTag implements Tag {
	int loop_id;
	int loop_stmt_id;

	public LoopTag(int loop_i, int loop_stmt_i) {
		loop_id = loop_i;
		loop_stmt_id = loop_stmt_i;
	}
	
	public LoopTag(int loop_i){
		loop_id = loop_i;
	}

	public String getName() {
		return "LoopTag";
	}

	public String getInfo() {
		return loop_id + "_" + loop_stmt_id;
	}

	public String toString() {
		return loop_id + "_" + loop_stmt_id;
	}

	public int getLoopId() {
		return loop_id;
	}

	public int getLoopStmtId() {
		return loop_stmt_id;
	}

	/** Returns the tag raw data. */
	public byte[] getValue() {
		throw new RuntimeException("LoopTag has no value for bytecode");
	}
}