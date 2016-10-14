package graph;

import java.util.Map;

public class ResultatBuildGraph {
	
	private Graph<Integer, Float> g;
	private Map<Integer,String> nodesMapItoS;
	private Map<String,Integer> nodesMapStoI;
	
	public ResultatBuildGraph(Graph<Integer, Float> g,
			Map<Integer, String> nodesMapItoS,
			Map<String, Integer> nodesMapStoI) {
		super();
		this.g = g;
		this.nodesMapItoS = nodesMapItoS;
		this.nodesMapStoI = nodesMapStoI;
	}
	
	public Graph<Integer, Float> getG() {
		return g;
	}
	public void setG(Graph<Integer, Float> g) {
		this.g = g;
	}
	public Map<Integer, String> getNodesMapItoS() {
		return nodesMapItoS;
	}
	public void setNodesMapItoS(Map<Integer, String> nodesMapItoS) {
		this.nodesMapItoS = nodesMapItoS;
	}
	public Map<String, Integer> getNodesMapStoI() {
		return nodesMapStoI;
	}
	public void setNodesMapStoI(Map<String, Integer> nodesMapStoI) {
		this.nodesMapStoI = nodesMapStoI;
	}
}
