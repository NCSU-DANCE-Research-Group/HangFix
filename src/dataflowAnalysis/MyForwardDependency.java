package dataflowAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.G;
import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;
import utils.Pair;

public class MyForwardDependency {
	  public Map<Object, Set<Object>> map = new HashMap<Object, Set<Object>>();
	  public Map<Object, Set<Object>> reverse_map = new HashMap<Object, Set<Object>>();
	  public Map<Object, Set<Object>> map_merged = new HashMap<Object, Set<Object>>();
	  
	  public MyForwardDependency(UnitGraph graph, boolean containsIntermediatedDependency){
		  MyForwardAnalysis lv = new MyForwardAnalysis(graph);		
		  List<Unit> tails = graph.getTails();
		  for (Unit t : tails) {
				FlowSet outSet = (FlowSet) lv.getFlowAfter(t);
				Iterator itSet = outSet.iterator();
				while(itSet.hasNext()){
//					G.v().out.println("outSet has next");
					Pair<Object> p = (Pair)itSet.next();
					if (!map.containsKey(p.impacted)) {
						map.put(p.impacted, new HashSet<Object>());
						//map.get(p.impacted).add(p.impacted); //add itself
					}
					map.get(p.impacted).add(p.impacting);

					if (!reverse_map.containsKey(p.impacting)) {
						reverse_map.put(p.impacting, new HashSet<Object>());
						//reverse_map.get(p.impacting).add(p.impacting); //add itself
					}
					reverse_map.get(p.impacting).add(p.impacted);
				}
		 }
//		 G.v().out.println("map size = " + map.size());
//		 
		 G.v().out.println("-----------map-----------");
		 for (Map.Entry<Object, Set<Object>> entry : map.entrySet()) {
			G.v().out.println(entry.getKey() + " " + entry.getValue().toString());
		 }
		 G.v().out.println("-------------------------");
//		 
//		 G.v().out.println("reverse map size = " + reverse_map.size());
//		 G.v().out.println("-----------reverse_map-----------");
//		 for (Map.Entry<Object, Set<Object>> entry : reverse_map.entrySet()) {
//			G.v().out.println(entry.getKey() + " " + entry.getValue().toString());
//		 }
		 merge(containsIntermediatedDependency);
	  }
	  
	  public void merge(boolean containsIntermediatedDependency) {
			Set<Object> final_value = new HashSet<Object>();
			if(containsIntermediatedDependency){
				for (Map.Entry<Object, Set<Object>> entry : reverse_map.entrySet()) {
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
//			G.v().out.println("final_value" + final_value);
			for (Object impacting_final : final_value) {
				Stack<Object> s = new Stack<Object>(); // one DFS for each impacting_final
				Map<Object,Boolean> visited = new HashMap<Object,Boolean>();
				s.push(impacting_final);
				while (!s.isEmpty()) {
					Object impacting = s.pop();
					if(visited.containsKey(impacting) && visited.get(impacting) == true){
						continue;
					}
					visited.put(impacting, true);				
					if (!map_merged.containsKey(impacting)) {
						map_merged.put(impacting, new HashSet<Object>());
					}
					map_merged.get(impacting).add(impacting_final);
					//G.v().out.println("merge value----"+impacting.toString()+": "+impacting_final.toString());
					if (reverse_map.containsKey(impacting)) {
						Set<Object> impacted_set = reverse_map.get(impacting);
//						if(data_related.contains(impacting)){
//							data_related.addAll(impacted_set);
//						}
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
//			G.v().out.println("-----------Variables merged impacting Set-----------");
//			for (Map.Entry<Object, Set<Object>> entry : map_merged.entrySet()) {
//				G.v().out.println(entry.getKey() + " " + entry.getValue().toString());
//			}
			// return map_merged;
//			return final_value;
		}
}
