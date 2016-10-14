package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mcl.MarkovClusteringSparse;
import mcl.SparseMatrix;
import mcl.SparseVector;

public class MclWrapper {

	public static Map<Integer, Set<Integer>> clusterGraphMCL(
			Graph<Integer, Float> g) {
		/*
		 * as some nodes were removed from the original graph, some nodeIds are missing
		 * we first create a new index from 0 to graph.size()
		 */
		Map<Integer, Integer> mapRealIdToMclId = new HashMap<Integer, Integer>();
		Map<Integer, Integer> mapMclIdToRealId = new HashMap<Integer, Integer>();
		for (Integer nodeRealId : g.getNodes()){
			mapRealIdToMclId.put(nodeRealId, mapRealIdToMclId.size());
			mapMclIdToRealId.put(mapMclIdToRealId.size(), nodeRealId);
		}
		g = buildGraphNewIds(g, mapRealIdToMclId, mapMclIdToRealId);
		mcl.SparseMatrix sparseMatrix = graph.MclWrapper.getSparseMatrix(g);
		MarkovClusteringSparse mc = new MarkovClusteringSparse();
		sparseMatrix = mc.run(sparseMatrix, 0, 2.0, 0.5, 0.00001);
		Map<Integer, Set<Integer>> clusters = getClusters(sparseMatrix);
		clusters = adaptClustersToRealIds(clusters, mapRealIdToMclId, mapMclIdToRealId);
		return clusters;
	}

	private static Map<Integer, Set<Integer>> adaptClustersToRealIds(
			Map<Integer, Set<Integer>> clusters,
			Map<Integer, Integer> mapRealIdToMclId,
			Map<Integer, Integer> mapMclIdToRealId) {

		Map<Integer, Set<Integer>> clustersRealIds = new HashMap<Integer, Set<Integer>>();
		for (Integer clusterId : clusters.keySet()){
			Set<Integer> clusterRealIds = new HashSet<Integer>();
			for (Integer node : clusters.get(clusterId)){
				clusterRealIds.add(mapMclIdToRealId.get(node));
			}
			clustersRealIds.put(clusterId, clusterRealIds);
		}
		return clustersRealIds;
	}

	private static Graph<Integer, Float> buildGraphNewIds(
			Graph<Integer, Float> g, Map<Integer, Integer> mapRealIdToMclId,
			Map<Integer, Integer> mapMclIdToRealId) {
		Graph<Integer, Float> graphNewIds = new ArrayBackedGraph<Float>(10, 1);
		for (Integer node : g.getNodes()){
			Iterator<Edge<Integer,Float>> itEdges = g.getEdges(node);
			while (itEdges.hasNext()){
				Edge<Integer,Float> edge = itEdges.next();
				graphNewIds.addEdgeUndirected(mapRealIdToMclId.get(node), mapRealIdToMclId.get(edge.source), edge.weight);
			}
		}
		return graphNewIds;
	}

	private static Map<Integer, Set<Integer>> getClusters(
			SparseMatrix sparseMatrix) {
		Map<Integer, Set<Integer>> clusters = new HashMap<Integer, Set<Integer>>();
		for (int i = 0; i<sparseMatrix.size();i++){
			SparseVector vec = sparseMatrix.getColum(i);
			if (vec.max()>0){
				Set<Integer> cluster = new HashSet<Integer>();
				for (Map.Entry<Integer, Double> entry : vec.entrySet()){
					if (entry.getValue()!=0){
						cluster.add(entry.getKey());
					}
				}
				clusters.put(clusters.size(), cluster);
			}
		}
		return clusters;
	}

	public static SparseMatrix getSparseMatrix(Graph<Integer, Float> g) {
		
		SparseMatrix sparseMatrix = new SparseMatrix(g.getSize(),g.getSize());
		for (Integer node1 : g.getNodes()){
			Iterator<Integer> it = g.getNeighbors(node1);
			while (it.hasNext()){
				Integer node2 = it.next();
				sparseMatrix.set(node1, node2, g.getEdge(node1, node2));
			}
		}
		return sparseMatrix;
	}
	
}
