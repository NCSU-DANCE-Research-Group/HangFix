package typeDataContent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class testDependency {
	
	public static void main(String[] args){
		testDependency inst = new testDependency();
		Object i1 = new Object();
		Object i4 = new Object();
		Object r2 = new Object();
		Object r0 = new Object();
		if(!inst.map.containsKey(i1)) inst.map.put(i1, new HashSet<Object>());
		if(!inst.map.containsKey(i4)) inst.map.put(i4, new HashSet<Object>());
		if(!inst.map.containsKey(r2)) inst.map.put(r2, new HashSet<Object>());
		inst.map.get(i1).add(i4); inst.map.get(i1).add(i1);
		
		
	}
	
	
	
	public Map<Object, Set<Object>> map = new HashMap<Object, Set<Object>>();
	  public Map<Object, Set<Object>> reverse_map = new HashMap<Object, Set<Object>>();
	  public Map<Object, Set<Object>> map_merged = new HashMap<Object, Set<Object>>();
	
	public void merge(boolean containsIntermediatedDependency) {
		Set<Object> final_value = new HashSet<Object>();
		if(containsIntermediatedDependency){
			//we consider all the objects
			for (Map.Entry<Object, Set<Object>> entry : reverse_map.entrySet()) {
				Object key = entry.getKey();
				final_value.add(key);
			}
			for (Map.Entry<Object, Set<Object>> entry : map.entrySet()) {
				Object key = entry.getKey();
				final_value.add(key);
			}
		} else {
			//we only consider the final dependency, which means, if foo depends bar, bar depends baz, 
			//then baz is in the final set but bar is not.
			for (Map.Entry<Object, Set<Object>> entry : reverse_map.entrySet()) {
				Object key = entry.getKey();
				if (!map.containsKey(key)) { //not impacted
					final_value.add(key);
				}
			}
		}
//		G.v().out.println("final_value" + final_value);
		for (Object impacting_final : final_value) {
			Stack<Object> s = new Stack<Object>(); // one DFS for each impacting_final
			Set<Object> visited = new HashSet<Object>();
			s.push(impacting_final);
			while (!s.isEmpty()) {
				Object impacting = s.pop();
				if(visited.contains(impacting)){
					continue;
				}
				visited.add(impacting);				
				if (!map_merged.containsKey(impacting)) {
					map_merged.put(impacting, new HashSet<Object>());
				}
				map_merged.get(impacting).add(impacting_final);
				//G.v().out.println("merge value----"+impacting.toString()+": "+impacting_final.toString());
				if (reverse_map.containsKey(impacting)) {
					Set<Object> impacted_set = reverse_map.get(impacting);
//					if(data_related.contains(impacting)){
//						data_related.addAll(impacted_set);
//					}
					for (Object impacted : impacted_set) {
						if(impacted.equals(impacting)){
							map_merged.get(impacting).add(impacted);
						} else {
							s.push(impacted);
						}
					}
				}
			}
			map_merged.remove(impacting_final);
		}
//		G.v().out.println("-----------Variables merged impacting Set-----------");
//		for (Map.Entry<Object, Set<Object>> entry : map_merged.entrySet()) {
//			G.v().out.println(entry.getKey() + " " + entry.getValue().toString());
//		}
		// return map_merged;
//		return final_value;
	}
}
