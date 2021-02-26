package hive18217;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hadoop2Conf.Text;

public class Buggycode {
	private Set<Integer> deletionSet = new HashSet<Integer>();
	private Map<Integer, Integer> replacementMap = new HashMap<Integer, Integer>();
	
	public void populateMappings(Text from, Text to) {
	    replacementMap.clear();
	    deletionSet.clear();

	    ByteBuffer fromBytes = ByteBuffer.wrap(from.getBytes(), 0, from.getLength());
	    ByteBuffer toBytes = ByteBuffer.wrap(to.getBytes(), 0, to.getLength());

	    // Traverse through the from string, one code point at a time
	    while (fromBytes.hasRemaining()) {
	      // This will also move the iterator ahead by one code point
	      int fromCodePoint = Text.bytesToCodePoint(fromBytes);
	      System.out.println("inside loop...fromCodePoint = " + fromCodePoint);
	      // If the to string has more code points, make sure to traverse it too
	      if (toBytes.hasRemaining()) {
	        int toCodePoint = Text.bytesToCodePoint(toBytes);
	        // If the code point from from string already has a replacement or is to be deleted, we
	        // don't need to do anything, just move on to the next code point
	        if (replacementMap.containsKey(fromCodePoint) || deletionSet.contains(fromCodePoint)) {
	          continue;
	        }
	        replacementMap.put(fromCodePoint, toCodePoint);
	      } else {
	        // If the code point from from string already has a replacement or is to be deleted, we
	        // don't need to do anything, just move on to the next code point
	        if (replacementMap.containsKey(fromCodePoint) || deletionSet.contains(fromCodePoint)) {
	          continue;
	        }
	        deletionSet.add(fromCodePoint);
	      }
	    }
	  }
}
