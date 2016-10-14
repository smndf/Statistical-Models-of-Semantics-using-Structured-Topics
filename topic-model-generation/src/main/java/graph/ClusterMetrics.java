package graph;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import util.Dijkstra;

public class ClusterMetrics {

	public class TrianglesAndTriplets{

		private Map<Integer, Integer> triangles = new HashMap<Integer, Integer>();
		private Map<Integer, Integer> triplets = new HashMap<Integer, Integer>();

		public TrianglesAndTriplets() {
			super();
			this.triangles = new HashMap<Integer, Integer>();
			this.setTriplets(new HashMap<Integer, Integer>());
			// TODO Auto-generated constructor stub
		}

		public TrianglesAndTriplets(Map<Integer, Integer> triangles,
				Map<Integer, Integer> triplets) {
			super();
			this.setTriangles(triangles);
			this.setTriplets(triplets);
		}

		public String clusteringCoefficient(int clusterIndex){
			if (this.getTriplets().get(clusterIndex)==0) return "0.0";
			else {

				Double res = (this.getTriangles().get(clusterIndex).floatValue()/this.getTriplets().get(clusterIndex).doubleValue());
				DecimalFormat df = new DecimalFormat("0.00");
				return (df.format(res));
			}
		}

		public Map<Integer, Integer> getTriangles() {
			return triangles;
		}

		public void setTriangles(Map<Integer, Integer> triangles) {
			this.triangles = triangles;
		}

		public Map<Integer, Integer> getTriplets() {
			return triplets;
		}

		public void setTriplets(Map<Integer, Integer> triplets) {
			this.triplets = triplets;
		}
	}

	public Map<Integer,Map<Integer,Integer>> centrality(Graph<Integer, Float> g,
			Map<Integer, Set<Integer>> clusters) {
		Map<Integer,Map<Integer,Integer>> centrality = new HashMap<Integer,Map<Integer,Integer>>();

		for (Integer noCluster : clusters.keySet()){

			Map<Integer,Integer> centrality1 = new HashMap<Integer,Integer>();
			Map<Integer,Integer> centrality2 = new HashMap<Integer,Integer>();

			for (Integer noNode : clusters.get(noCluster)){

				Iterator<Integer> it1 = g.getNeighbors(noNode);
				int scoreSum = 0;
				while(it1.hasNext()){
					int node2 = it1.next();
					if (clusters.get(noCluster).contains(node2)){
						scoreSum += 1;
					}
				}
				centrality1.put(noNode,scoreSum);
			}

			for (Integer noNode : clusters.get(noCluster)){

				Iterator<Integer> it1 = g.getNeighbors(noNode);
				int scoreSum = 0;
				while(it1.hasNext()){
					int node2 = it1.next();
					if (clusters.get(noCluster).contains(node2)){
						scoreSum += centrality1.get(node2);
					}
				}
				centrality2.put(noNode,scoreSum);
			}

			centrality.put(noCluster,centrality2);
		}
		return centrality;
	}

	public Map<Integer,Map<Integer,Integer>> centralityAndGetListOfEdges(Graph<Integer, Float> g,
			Map<Integer, Set<Integer>> clusters, List<Edge<Integer, Float>> edges) {
		Map<Integer,Map<Integer,Integer>> centrality = new HashMap<Integer,Map<Integer,Integer>>();

		for (Integer noCluster : clusters.keySet()){

			Map<Integer,Integer> centrality1 = new HashMap<Integer,Integer>();
			Map<Integer,Integer> centrality2 = new HashMap<Integer,Integer>();

			for (Integer node1 : clusters.get(noCluster)){

				Iterator<Integer> it1 = g.getNeighbors(node1);
				int scoreSum = 0;
				while(it1.hasNext()){
					int node2 = it1.next();
					// for edges
					if (node2>node1){
						Float weight = g.getEdge(node1, node2);
						edges.add(new Edge<Integer, Float>(node1, node2, weight));
					}
					if (clusters.get(noCluster).contains(node2)){
						scoreSum += 1;
					}
				}
				centrality1.put(node1,scoreSum);
			}

			for (Integer noNode : clusters.get(noCluster)){

				Iterator<Integer> it1 = g.getNeighbors(noNode);
				int scoreSum = 0;
				while(it1.hasNext()){
					int node2 = it1.next();
					if (clusters.get(noCluster).contains(node2)){
						scoreSum += centrality1.get(node2);
					}
				}
				centrality2.put(noNode,scoreSum);
			}

			centrality.put(noCluster,centrality2);
		}
		return centrality;
	}
	
	public TrianglesAndTriplets trianglesAndTriplets(Graph<Integer, Float> g,
			Map<Integer, Set<Integer>> clusters) {


		TrianglesAndTriplets trianglesTriplets = new TrianglesAndTriplets();
		Boolean b = false;
		if (b){
			for (Entry<Integer, Set<Integer>> entry : clusters.entrySet()) {
				trianglesTriplets.getTriangles().put(entry.getKey(), 0);
				trianglesTriplets.getTriplets().put(entry.getKey(), 0);
			}
		} else {
			//for each cluster
			int nbClusters = 0,lastNbClusters = 0;
			for (Entry<Integer, Set<Integer>> entry : clusters.entrySet()) {
				//Set<Set<Integer>> triangles = new HashSet<Set<Integer>>();
				//Set<Set<Integer>> connectedTriplets = new HashSet<Set<Integer>>(); 

				int nbTriangles = 0,nbTriplets=0;
				//int nbTests = 0;
				//for each node in the cluster
				for (Integer node1 : (Set<Integer>)entry.getValue()){
					Iterator<Integer> it1 = g.getNeighbors(node1);
					while(it1.hasNext()){
						int node2;
						if (entry.getValue().contains((node2 = it1.next())) && node2 != node1){
							Iterator<Integer> it2 = g.getNeighbors(node2);
							while(it2.hasNext()){
								Iterator<Integer> it1bis = g.getNeighbors(node1);
								int node3;
								if (entry.getValue().contains((node3 = it2.next())) && node3 != node1 && node3 != node2){							
									//Set<Integer> set = new HashSet<Integer>();
									//set.add(node1);
									//set.add(node2);
									//set.add(node3);
									//connectedTriplets.add(set);
									nbTriplets++;
									//System.out.println("triplet : " + nodesMapItoS.get(node1) + " " + nodesMapItoS.get(node2) + " " + nodesMapItoS.get(node3));
									while(it1bis.hasNext()){
										//nbTests++;
										if (it1bis.next().intValue() == node3){
											//System.out.println("node1="  +node1 +  "  node2="  +node2 + "  node3="  +node3);
											//triangles.add(set);
											nbTriangles++;
											break;
										}
									}
								}
							}
						}
					}
				}

				nbTriangles /= 6;
				nbTriplets = ((nbTriplets - (nbTriangles*6))/2)+nbTriangles;
				//System.out.println("triangles : " + triangles.size() + " "+nbTriangles);
				//System.out.println("triplets : " + connectedTriplets.size() + " " + nbTriplets + "\n");

				trianglesTriplets.getTriangles().put(entry.getKey(), nbTriangles);
				trianglesTriplets.getTriplets().put(entry.getKey(), nbTriplets);

				nbClusters++;
				if (nbClusters>lastNbClusters+100){
					System.out.println("cluster " + nbClusters);
					lastNbClusters=nbClusters;
				}
			}

		}
		return trianglesTriplets;
	}

	public Map<Integer, List<Integer>> sortCentralityScores(
			Map<Integer, Map<Integer, Integer>> centrality) {

		Map<Integer,List<Integer>> sortedScores = new HashMap<Integer,List<Integer>>();
		
		for (Integer noCluster : centrality.keySet()){
			Map<Integer, Integer> centralityScores = centrality.get(noCluster);
			Map<Integer,Set<Integer>> mapScores2Id = new HashMap<Integer,Set<Integer>>();
			List<Integer> scoresList = new ArrayList<Integer>();
			for (Integer id : centralityScores.keySet()){
				if (!mapScores2Id.containsKey(centralityScores.get(id))){
					Set<Integer> set = new HashSet<Integer>();
					set.add(id);
					mapScores2Id.put(centralityScores.get(id),set);					
				} else {
					mapScores2Id.get(centralityScores.get(id)).add(id);
				}
				if (!scoresList.contains(centralityScores.get(id))){
					scoresList.add(centralityScores.get(id));					
				}
			}
			Collections.sort(scoresList);
			Collections.reverse(scoresList);
			List<Integer> sortedIds = new ArrayList<Integer>();
			for (Integer score : scoresList){
				for (Integer node : mapScores2Id.get(score)){
					sortedIds.add(node);					
				}
			}
			sortedScores.put(noCluster,sortedIds);
		}
		
		return sortedScores;
	}

	public Double computeDaviesBouldinIndex(Graph<Integer, Float> g,
			Map<Integer, Set<Integer>> clusters,
			Map<Integer, List<Integer>> centralityScoresSorted, List<Edge<Integer,Float>> edges) {

		Map<Integer,Integer> nodeIdToClusterId = new HashMap<Integer, Integer>();
		for (Integer clusterId : clusters.keySet()) for (Integer nodeId : clusters.get(clusterId)) nodeIdToClusterId.put(nodeId, clusterId);
		Map<Integer, Integer> centroids = new HashMap<Integer, Integer>();
		for (Integer clusterId : centralityScoresSorted.keySet()) {
			if (centralityScoresSorted.get(clusterId).size()==0) continue;
			centroids.put(clusterId, centralityScoresSorted.get(clusterId).get(0));
		}
		Map<Integer, List<Float>> distancesFromCentroid = new HashMap<Integer, List<Float>>();
		Map<Integer,Map<Integer,Float>> distancesBetweenCentroids = new HashMap<Integer,Map<Integer,Float>>();
		Map<Integer, Set<Integer>> seenNodes = new HashMap<Integer, Set<Integer>>();
		for (Integer clusterId : clusters.keySet()) {
			seenNodes.put(clusterId, new HashSet<Integer>());
			distancesFromCentroid.put(clusterId, new ArrayList<Float>());
			distancesBetweenCentroids.put(clusterId, new HashMap<Integer,Float>());
			for (Integer clusterId2 : clusters.keySet()) distancesBetweenCentroids.get(clusterId).put(clusterId2, Float.MAX_VALUE);
		}
		for (Edge<Integer, Float> edge : edges){
			Integer node1 = edge.getSource();
			Integer node2 = edge.getDest();
			if (node1==node2) continue;
			Float weight = edge.getWeight();
			Integer clusterIdNode1 = nodeIdToClusterId.get(node1);
			Integer clusterIdNode2 = nodeIdToClusterId.get(node2);
			// inside a cluster
			if (clusterIdNode1==clusterIdNode2){
			/*	Integer clusterId = clusterIdNode1;
				Integer centroidId = centroids.get(clusterId);
				if (centroidId!=node1 && centroidId!=node2) continue;
				if (centroidId==node1){
					Float dist = 1/weight;
					distancesFromCentroid.get(clusterId).add(dist);
					seenNodes.get(clusterIdNode1).add(node1);
					seenNodes.get(clusterIdNode2).add(node2);
				}
				if (centroidId==node2){
					Float dist = 1/weight;
					distancesFromCentroid.get(clusterId).add(dist);
					seenNodes.get(clusterIdNode1).add(node1);
					seenNodes.get(clusterIdNode2).add(node2);
				}
			*/
			}
			// between two clusters
			else {
				/* We consider the minimum distance into two nodes in the two clusters instead of the distance between the centroids of each cluster
				Integer centroidIdCluster1 = centroids.get(clusterIdNode1);
				if (centroidIdCluster1!=node1) continue;
				Integer centroidIdCluster2 = centroids.get(clusterIdNode2);
				if (centroidIdCluster2!=node2) continue;*/
				//System.out.println("weight = "+weight);
				Float dist = 1/weight;
				if (distancesBetweenCentroids.containsKey(clusterIdNode1) && distancesBetweenCentroids.get(clusterIdNode1).containsKey(clusterIdNode2) && dist<distancesBetweenCentroids.get(clusterIdNode1).get(clusterIdNode2)) {
					distancesBetweenCentroids.get(clusterIdNode1).put(clusterIdNode2, dist);
					distancesBetweenCentroids.get(clusterIdNode2).put(clusterIdNode1, dist);
					//System.out.println("dist = "+dist);
				}
				//seenNodes.get(clusterIdNode1).add(node1);
				//seenNodes.get(clusterIdNode2).add(node2);
			}
		}
		Map<Integer, Set<Integer>> notYetSeenNodes = new HashMap<Integer, Set<Integer>>();
		for (Integer clusterId : clusters.keySet()) {
			notYetSeenNodes.put(clusterId, substractSets(clusters.get(clusterId),seenNodes.get(clusterId)));
		}
		/*
		 *  searching connections between nodes not seen yet and the centroid of their cluster
		 *  there has to be one, otherwise they would not be in the same cluster
		 *  it has to be not direct i.e. they are at least separated by one node
		 */
		/*for (Integer clusterId : notYetSeenNodes.keySet()){
			Integer centroid = centroids.get(clusterId);
			Iterator<Integer> itcentroidNeighbours = g.getNeighbors(centroid);
			Set<Integer> centroidNeighbours = new HashSet<Integer>();
			while (itcentroidNeighbours.hasNext()) centroidNeighbours.add(itcentroidNeighbours.next());
			System.out.println("centroidNeighbours.size() = "+centroidNeighbours.size());
			for (Integer nodeNotSeen : notYetSeenNodes.get(clusterId)){
				Iterator<Integer> itNodeNeighbours = g.getNeighbors(nodeNotSeen);
				boolean linkFound = false;
				while (itNodeNeighbours.hasNext() && !linkFound){
					Integer neighbour = itNodeNeighbours.next();
					if (centroidNeighbours.contains(neighbour)) {
						Integer centroidNeighbour = -1;
						for (Integer centroidNeighbourCandidate : centroidNeighbours) 
							if (centroidNeighbourCandidate==neighbour) 
								centroidNeighbour = centroidNeighbourCandidate;
						if (centroidNeighbour>-1){
							linkFound = true;
							seenNodes.get(clusterId).add(nodeNotSeen);
							//TODOq
							boolean stop = false;
							Float weightCentroidToNeighbour = g.getEdge(centroid, centroidNeighbour);
							if (weightCentroidToNeighbour==null) {
								weightCentroidToNeighbour = g.getEdge(centroidNeighbour, centroid);
								if (weightCentroidToNeighbour==null) stop = true;
								else if (weightCentroidToNeighbour==0) stop = true;
							}
							Float weightNeighbourToNode = g.getEdge(nodeNotSeen, centroidNeighbour);
							if (weightNeighbourToNode==null) {
								weightNeighbourToNode = g.getEdge(centroidNeighbour, nodeNotSeen);
								if (weightNeighbourToNode==null) stop = true;
								else if (weightNeighbourToNode==0) stop = true;
							}
							if (!stop){
								Float dist = (1/weightCentroidToNeighbour) + (1/weightNeighbourToNode);
								distancesFromCentroid.get(clusterId).add(dist);							
							}
						}
					}
				}
			}
		}*/
		/*
		 * Note we do not search for further links to keep the runtime ok
		 * since we apply the same for all models, it is ok
		 */
		
		for (Integer clusterId : clusters.keySet()){
			Integer centroid = centroids.get(clusterId);
			Set<Integer> clusterNodes = clusters.get(clusterId);
			Map<Integer,Float> distances = Dijkstra.runAlgoOnSubgraph(g, clusterNodes, centroid);
			List<Float> distancesList = new ArrayList<Float>();
			for (Integer node : clusterNodes) distancesList.add(distances.get(node));
			distancesFromCentroid.get(clusterId).addAll(distancesList);
		}
		
		Map<Integer, Float> avgDistanceFromCentroid = new HashMap<Integer, Float>();
		for (Integer clusterId : distancesFromCentroid.keySet()){
			Float avg = computeAvg(distancesFromCentroid.get(clusterId));
			avgDistanceFromCentroid.put(clusterId, avg);
		}
		Double daviesBouldinIndex = computeIndex(avgDistanceFromCentroid, distancesBetweenCentroids);
		
		return daviesBouldinIndex;
	}

	private Set<Integer> substractSets(Set<Integer> set, Set<Integer> set2) {

		Set<Integer> res = new HashSet<Integer>();
		for (Integer i : set){
			if (!set2.contains(i)){
				res.add(i);
			}
		}
		return res;
	}

	private Float computeAvg(List<Float> list) {
		Float sum = (float) 0.;
		for (Float f : list) sum += f;
		Float avg = sum/list.size();
		return avg;
	}

	private Double computeIndex(Map<Integer, Float> avgDistanceFromCentroid,
			Map<Integer, Map<Integer, Float>> distancesBetweenCentroids) {

		Double sum = 0.;
		for (Integer clusterId1 : avgDistanceFromCentroid.keySet()){
			Float avgDistanceFromCentroid1 = avgDistanceFromCentroid.get(clusterId1);
			//System.out.println("avgDistanceFromCentroid1 = "+avgDistanceFromCentroid1);
			Float max = Float.MIN_VALUE;
			for (Integer clusterId2 : avgDistanceFromCentroid.keySet()){
				if (clusterId2==clusterId1) continue;
				Float avgDistanceFromCentroid2 = avgDistanceFromCentroid.get(clusterId2);
				//System.out.println("avgDistanceFromCentroid2 = "+avgDistanceFromCentroid2);
				Float distanceBetweenTheCentroids = distancesBetweenCentroids.get(clusterId1).get(clusterId2);
				//System.out.println("distanceBetweenTheCentroids = "+distanceBetweenTheCentroids);
				Float valueForTheseClusters = (avgDistanceFromCentroid1+avgDistanceFromCentroid2)/distanceBetweenTheCentroids;
				if (valueForTheseClusters>max && !Float.isInfinite(valueForTheseClusters)) {
					if (valueForTheseClusters<100000000){
						max=valueForTheseClusters;
						//System.out.println("This is a new max: "+max);
						//System.out.println("valueForTheseClusters = "+valueForTheseClusters);
					} else System.out.println("value discarded = "+valueForTheseClusters);
				}
			}
			//System.out.println("previous sum = "+sum);
			//System.out.println("global max is "+max);
			if (max!=Float.MIN_VALUE) sum += max;
			System.out.println("updated sum = "+sum);
		}
		Double avg = sum/avgDistanceFromCentroid.size();
		System.out.println("avg = "+avg+"\n\n");
		return avg;
	}


}
