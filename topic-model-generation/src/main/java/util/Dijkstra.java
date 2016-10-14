package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import graph.Edge;
import graph.Graph;

public class Dijkstra {

	public static Map<Integer,Float> runAlgoOnSubgraph(Graph<Integer, Float> g, Set<Integer> subgraphNodes, Integer source){
		
		Graph<Integer, Float> subgraph = g.undirectedSubgraph(subgraphNodes);
		return runAlgo(subgraph, source);
		
	}
	
	public static Map<Integer,Float> runAlgo(Graph<Integer, Float> g, Integer source) {

		//System.out.println("source = "+source);
		Set<Integer> Q = new HashSet<Integer>();
		int size = g.getSize();
		Map<Integer,Float> dist = new HashMap<Integer,Float>();
		for (Integer node : g.getNodes()){
			dist.put(node, Float.MAX_VALUE);
			Q.add(node);
		}
		dist.put(source, (float) 0);
		
		while (Q.size()!=0){
			Integer u = getNodeWithMinDist(Q, dist);
			Q.remove(u);
			//System.out.println("u = "+u);
			if (u!=-1){
				Iterator<Edge<Integer,Float>> it = g.getEdges(u);
				while (it.hasNext()) {
					Edge<Integer,Float> edge = it.next();
					Integer v = edge.getSource();
					if (Q.contains(v)){
						Float weight = edge.getWeight();
						if (weight != 0){
							Float distEdge = 1/weight;
							Float alt = distEdge + dist.get(u);
							if (alt<dist.get(v)){
								dist.put(v, alt);
							}
							
						}
					}	
				}
			} else Q.clear(); // terminates
		}
		return dist;
	}

	private static Integer getNodeWithMinDist(Set<Integer> nodes, Map<Integer,Float> dist) {

		Integer nodeWithMinDist = -1;
		Float minDist = Float.MAX_VALUE;
		for (Integer node : nodes){
			float distNode = dist.get(node);
			if (distNode<minDist) {
				nodeWithMinDist = node;
				minDist = distNode;
			}
		}
		return nodeWithMinDist;
	}

}
