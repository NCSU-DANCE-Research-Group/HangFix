package hive18216;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hadoop2Conf.Text;

public class Buggycode {
	private Set<Integer> deletionSet = new HashSet<Integer>();
	private Map<Integer, Integer> replacementMap = new HashMap<Integer, Integer>();
	
	public Buggycode(){
//		deletionSet.add(-1);
	}
	
	public String processInput(Text input) {
	    StringBuilder resultBuilder = new StringBuilder();
	    // Obtain the byte buffer from the input string so we can traverse it code point by code point
	    ByteBuffer inputBytes = ByteBuffer.wrap(input.getBytes(), 0, input.getLength());
	    // Traverse the byte buffer containing the input string one code point at a time
	    while (inputBytes.hasRemaining()) {
	      int inputCodePoint = Text.bytesToCodePoint(inputBytes);
	      System.out.println("inside loop...inputCodePoint = " + inputCodePoint);
	      // If the code point exists in deletion set, no need to emit out anything for this code point.
	      // Continue on to the next code point
	      if (deletionSet.contains(inputCodePoint)) {
	        continue;
	      }

	      Integer replacementCodePoint = replacementMap.get(inputCodePoint);
	      // If a replacement exists for this code point, emit out the replacement and append it to the
	      // output string. If no such replacement exists, emit out the original input code point
	      char[] charArray = Character.toChars((replacementCodePoint != null) ? replacementCodePoint
	          : inputCodePoint);
	      resultBuilder.append(charArray);
	    }
	    String resultString = resultBuilder.toString();
	    return resultString;
	  }
}
