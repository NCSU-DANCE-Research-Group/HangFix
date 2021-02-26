package utils;

import java.util.ArrayList;
import java.util.List;

public class UseConfiguration {
	
	private static List<String> configurationNames = new ArrayList<String>();
	
	static{
		configurationNames.add("JobConf");
	}
	
	public static boolean invokeConfiguration(String className){
		for(String confName : configurationNames){
			if(className.contains(confName)){
				return true;
			}
		}
		return false;
	}
	
	
}
