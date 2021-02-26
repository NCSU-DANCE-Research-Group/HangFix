package hadoop2FSDataIPS;

public class StreamUtil {
	
	static final String regexpSpecials = "[]()?*+|.!^-\\~@";
	public static String regexpEscape(String plain) {
	    StringBuffer buf = new StringBuffer();
	    char[] ch = plain.toCharArray();
	    int csup = ch.length;
	    for (int c = 0; c < csup; c++) {
	      if (regexpSpecials.indexOf(ch[c]) != -1) {
	        buf.append("\\");
	      }
	      buf.append(ch[c]);
	    }
	    return buf.toString();
	  }
}
