package hadoop15417;

public class Configuration {
	private String dir = "/home/ting/DataLoopBugDetection/workspace_java/testInstrumentJavaClass/bin/hadoop15417";
	public String get(String str){
		if(str.equals("fs.s3.buffer.dir"))
			return dir;
		return "";
	}
	
	
	public int getInt(String s3StreamBufferSizeKey,
			int s3StreamBufferSizeDefault) {
		return 0;//inject faults
	}
}

