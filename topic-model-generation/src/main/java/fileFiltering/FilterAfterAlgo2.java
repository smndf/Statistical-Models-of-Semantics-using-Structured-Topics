package fileFiltering;

import graph.ClusterMetrics;
import graph.Edge;
import graph.Graph;
import graph.ResultatBuildGraph;
import graph.ClusterMetrics.TrianglesAndTriplets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class FilterAfterAlgo2 {

	public static void filter(String inputFile, ResultatBuildGraph res) throws FileNotFoundException {

		String outputFile = inputFile.substring(0, inputFile.length() - 4) + "DaviesBouldinIndex.txt";
		//File fo = new File(outputFile);

		//InputStream is = getClass().getResourceAsStream(inputFile);
		BufferedReader input = new BufferedReader(new FileReader(inputFile));

		String[] lineSplit;
		String line = null;

		Map<Integer,String> nodesMapItoS = res.getNodesMapItoS();
		Map<String,Integer> nodesMapStoI = res.getNodesMapStoI();
		Graph<Integer, Float> g = res.getG();

		try {

			//FileWriter fw = new FileWriter (fo);
			//StringWriter sw = new StringWriter();
			int ctr = 0;
			int nbWords = 0;
			Map<Integer, Set<Integer>> clusters = new HashMap<Integer, Set<Integer>>();
			while ( (line = input.readLine()) != null){ 
				lineSplit = line.split("\t");
				nbWords = 0;
				int numCluster = Integer.valueOf(lineSplit[0]);
				if (lineSplit.length>1){
					Set<Integer> cluster = new HashSet<Integer>();
					//System.out.println("dsfs");
					ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[lineSplit.length-1].split(",")));
					//sw.write(ctr + "\t");
					//sw.write(lineSplit[1] + "\t");
					for (String neighbour : neighboursSplit){						

						//sw.write(neighbour + ",");	
						nbWords++;
						if (nodesMapStoI.get(neighbour) == null){
							System.out.println("nodesMapStoI.get("+neighbour+") == null");
						} else {							
							cluster.add(nodesMapStoI.get(neighbour));
						}

					}
					if (nbWords>2){
						//sw.getBuffer().setLength(sw.toString().length() - 2);
						//fw.write (sw.toString() + "\n");
						clusters.put(numCluster,cluster);
					} else {
						ctr--;
					}
					ctr++;
				}
				//sw.getBuffer().setLength(0);
			}
			ClusterMetrics ccc = new ClusterMetrics();
			//System.out.println("Compute number triangles and triplets");
			//TrianglesAndTriplets trianglesTriplets = ccc.trianglesAndTriplets(g,clusters);
			System.out.println("Compute centrality measures");
			List<Edge<Integer,Float>> edges = new ArrayList<Edge<Integer,Float>>();
			Map<Integer,Map<Integer,Integer>> centrality = ccc.centralityAndGetListOfEdges(g, clusters, edges);
			System.out.println("Sort centrality scores");
			Map<Integer,List<Integer>> centralityScoresSorted = ccc.sortCentralityScores(centrality);
			Double daviesBouldinIndex = ccc.computeDaviesBouldinIndex(g, clusters, centralityScoresSorted, edges);

			

			File fo = new File(outputFile);
			try{
				FileWriter fw = new FileWriter (fo);
				//fw.write (MapUtil.toString(clusters,trianglesTriplets,nodesMapItoS,eigenvectorCentralityScores,"\t","\n"));
				System.out.println("Write results in "+outputFile);
				//fw.write (toString(centralityScoresSorted, centrality, trianglesTriplets,nodesMapItoS,"\t","\n"));
				fw.write("daviesBouldinIndex\t"+daviesBouldinIndex);
				fw.flush();
				fw.close();
				System.out.println("results written in " + outputFile);
			}
			catch (IOException exception)
			{
				System.out.println (exception.getMessage());
			}

			//fw.close();
			//sw.close();
			System.out.println("file modified, new file : " + outputFile);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	public static String toString(Map<Integer, List<Integer>> centralityScoresSorted, Map<Integer,Map<Integer,Integer>> centrality, TrianglesAndTriplets trianglesTriplets, Map<Integer,String> nodesMapItoS, String keyValSep, String entrySep) {
		StringWriter writer = new StringWriter();
		try {
			writeMap3(centralityScoresSorted, centrality, trianglesTriplets, nodesMapItoS, writer, keyValSep, entrySep);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
	
	private static void writeMap3(Map<Integer, List<Integer>> centralityScoresSorted,
			Map<Integer, Map<Integer, Integer>> centrality, TrianglesAndTriplets trianglesTriplets,
			Map<Integer, String> nodesMapItoS, StringWriter writer,
			String keyValSep, String entrySep) throws IOException{
		int i = 0;
		for (Entry<Integer, List<Integer>> entry : centralityScoresSorted.entrySet()) {
			//System.out.println("line "+i++);
			Integer noCluster = entry.getKey();
			writer.write(noCluster.toString());
			writer.write(keyValSep);
			writer.write(String.valueOf(entry.getValue().size()));
			writer.write(keyValSep);
			writer.write(trianglesTriplets.getTriangles().get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(trianglesTriplets.getTriplets().get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(trianglesTriplets.clusteringCoefficient(entry.getKey()).toString());
			writer.write(keyValSep);
			//System.out.println(entry.getValue());
			//System.out.println(nodesMapItoS.get(entry.getValue()));
			int size = 0;
			for (Integer node : entry.getValue()){
				if (node != null){
					writer.write(nodesMapItoS.get(node).toString()+ "("+ centrality.get(noCluster).get(node) +")"+",");
					System.out.println("cluster "+noCluster+" word "+node+" centrality "+centrality.get(noCluster).get(node));
				} else {					
					System.out.println("null node");
				}
				//writer.write("node"+node+ ",");
				size++;
			}
			writer.getBuffer().setLength(writer.getBuffer().length()-1);
			writer.write(entrySep);
		}
	}
}