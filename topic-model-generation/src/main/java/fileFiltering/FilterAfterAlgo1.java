package fileFiltering;

import graph.ArrayBackedGraph;
import graph.ClusterMetrics.TrianglesAndTriplets;
import graph.Graph;
import graph.ResultatBuildGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import util.MapUtil;
import graph.ClusterMetrics;

public class FilterAfterAlgo1 {

	public void filter(String inputFile, ResultatBuildGraph res, Map<String, Integer> freqMap, int freqThreshold) throws FileNotFoundException {

		String outputFile = inputFile.substring(0, inputFile.length() - 4) + "FiltAft.txt";
		//File fo = new File(outputFile);

		//InputStream is = getClass().getResourceAsStream(inputFile);
		BufferedReader input = new BufferedReader(new FileReader(inputFile));

		String[] lineSplit = new String[2];
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
				if (lineSplit.length>5){
					Set<Integer> cluster = new HashSet<Integer>();
					//System.out.println("dsfs");
					ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[5].split(",")));
					//sw.write(ctr + "\t");
					ctr++;
					//sw.write(lineSplit[1] + "\t");
					for (String neighbour : neighboursSplit){
						//neighbours.add(neighbourSp.split(":")[0]);
						// if node already added							
						if (neighbour.split("#").length>1){
							String pos = neighbour.split("#")[1];		
							if (pos.equals("NN") || pos.equals("NP") /*|| pos.equals("JJ") || pos.equals("RB")*/){	
								String term = neighbour.split("#")[0] + "#" + neighbour.split("#")[1];
								if (freqMap.containsKey(term) && freqMap.get(term) > freqThreshold){
									//sw.write(neighbour + ",");	
									nbWords++;
									cluster.add(nodesMapStoI.get(neighbour));
									if (nodesMapStoI.get(neighbour)==null){
										System.out.println("nodesMapStoI.get("+neighbour+") = null");
									}
								}
							}
						}
					}
					if (nbWords>2){
						//sw.getBuffer().setLength(sw.toString().length() - 2);
						//fw.write (sw.toString() + "\n");
						clusters.put(ctr,cluster);
					} else {
						ctr--;
					}
				}
				//sw.getBuffer().setLength(0);
			}
			ClusterMetrics ccc = new ClusterMetrics();
			TrianglesAndTriplets trianglesTriplets = ccc.trianglesAndTriplets(g,clusters);
			Map<Integer,Map<Integer,Integer>> centrality = ccc.centrality(g, clusters);
			Map<Integer,List<Integer>> centralityScoresSorted = ccc.sortCentralityScores(centrality);
			
			
			File fo = new File(outputFile);
			try{
				FileWriter fw = new FileWriter (fo);
				//fw.write (MapUtil.toString(clusters,trianglesTriplets,nodesMapItoS,eigenvectorCentralityScores,"\t","\n"));
				fw.write (toString(centralityScoresSorted,trianglesTriplets,nodesMapItoS,"\t","\n"));
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
	
	public static String toString(Map<Integer, List<Integer>> centralityScoresSorted, TrianglesAndTriplets trianglesTriplets, Map<Integer,String> nodesMapItoS, String keyValSep, String entrySep) {
		StringWriter writer = new StringWriter();
		try {
			writeMap3(centralityScoresSorted, trianglesTriplets, nodesMapItoS, writer, keyValSep, entrySep);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
	
	private static void writeMap3(Map<Integer, List<Integer>> centralityScoresSorted,
			TrianglesAndTriplets trianglesTriplets,
			Map<Integer, String> nodesMapItoS, StringWriter writer,
			String keyValSep, String entrySep) throws IOException{
		for (Entry<Integer, List<Integer>> entry : centralityScoresSorted.entrySet()) {
			writer.write(entry.getKey().toString());
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
				writer.write(nodesMapItoS.get(node).toString()+ ",");
				size++;
			}
			writer.write(entrySep);
		}
	}

}
