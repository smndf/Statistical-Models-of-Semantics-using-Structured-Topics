package topicsAsDistributions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import util.Dijkstra;
import util.MapUtil;
import fileFiltering.FilterAfterAlgo2;
import graph.BuildGraph;
import graph.ClusterMetrics;
import graph.Edge;
import graph.Graph;
import graph.ResultatBuildGraph;

public class ConvertTopicsToDistsOverVoc {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option clustersFileOption = Option.builder()
				.longOpt("clusters")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to clustersFile")
				.build();
		Option ddtFileOption = Option.builder()
				.longOpt("ddt")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to ddt file")
				.build();

		options.addOption(ddtFileOption);
		options.addOption(clustersFileOption);
		CommandLine cl = null;
		boolean success = false;
		try {
			cl = clParser.parse(options, args);
			success = true;
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		if (!success) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar ***.jar", options, true);
			System.exit(1);
		}
		String clustersFileName = cl.getOptionValue("clusters");
		String ddtFileName = cl.getOptionValue("ddt");
		ResultatBuildGraph res = null;
		try {
			res = BuildGraph.buildGraph(ddtFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (res==null) System.exit(1);

		try {
			BufferedReader input = new BufferedReader(new FileReader(clustersFileName));

			String[] lineSplit;
			String line = null;
			Map<Integer,String> nodesMapItoS = res.getNodesMapItoS();
			Map<String,Integer> nodesMapStoI = res.getNodesMapStoI();
			Graph<Integer, Float> g = res.getG();

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
			System.out.println(clusters.size()+" clusters");
			System.out.println("Cluster 10 size = "+clusters.get(10).size());
			ClusterMetrics ccc = new ClusterMetrics();
			//System.out.println("Compute number triangles and triplets");
			//TrianglesAndTriplets trianglesTriplets = ccc.trianglesAndTriplets(g,clusters);
			System.out.println("Compute centrality measures");
			List<Edge<Integer,Float>> edges = new ArrayList<Edge<Integer,Float>>();
			Map<Integer,Map<Integer,Integer>> centrality = ccc.centralityAndGetListOfEdges(g, clusters, edges);
			System.out.println("centrality: "+centrality.size());
			System.out.println("centrality (10): "+centrality.get(10).size());
			System.out.println("Find centroids");
			Map<Integer,Integer> centroids = getCentroids(centrality);
			System.out.println(centroids.size()+" centroids");
			System.out.println("Compute distance from centroids");
			Map<Integer, Map<Integer, Float>> distancesFromCentroids = computeDistancesFromCentroids(centroids, g);
			System.out.println("Write into file");
			String outputFile = clustersFileName.substring(0,clustersFileName.length()-4)+".Distributions.csv";
			writeTopicsAsDistributions(outputFile, distancesFromCentroids, nodesMapItoS);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	


	}

	private static void writeTopicsAsDistributions(String outputFile,
			Map<Integer, Map<Integer, Float>> distancesFromCentroids,
			Map<Integer, String> nodesMapItoS) {

		try {
			FileWriter fw = new FileWriter(outputFile);
			for (Integer clusterId : distancesFromCentroids.keySet()){
				fw.write(clusterId+"\t");
				Map<Integer,Float> distancesFromCentroid = distancesFromCentroids.get(clusterId);
				distancesFromCentroid = MapUtil.sortMapByValue(distancesFromCentroid);
				List<Integer> nodes = new ArrayList<Integer>();
				for (Integer node : distancesFromCentroid.keySet()) nodes.add(node);
				Collections.reverse(nodes);
				for (Integer node : nodes){
					fw.write(nodesMapItoS.get(node)+":"+distancesFromCentroid.get(node)+",");
				}
				fw.write("\n");
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Map<Integer, Map<Integer, Float>> computeDistancesFromCentroids(
			Map<Integer, Integer> centroids, final Graph<Integer, Float> g) {

		final Map<Integer, Map<Integer, Float>> distancesFromCentroids = new ConcurrentHashMap<Integer, Map<Integer, Float>>();

		final int nbThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nbThreads);
		try {
			for (final Integer clusterId : centroids.keySet()){
				final Integer centroid = centroids.get(clusterId);
				
				exec.submit(new Runnable() {
					public void run() {
						System.out.println("centroid "+centroid);
						Map<Integer, Float> distancesFromCentroid = Dijkstra.runAlgo(g, centroid);
						distancesFromCentroid.put(centroid, (float) 0);
						distancesFromCentroids.put(clusterId, distancesFromCentroid);
					}
				});
			}
		} finally {
			exec.shutdown();
		}
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return distancesFromCentroids;
	}

	private static Map<Integer, Integer> getCentroids(
			Map<Integer, Map<Integer, Integer>> centrality) {

		Map<Integer, Integer> centroids = new HashMap<Integer,Integer>();
		for (Map.Entry<Integer, Map<Integer,Integer>> entry :centrality.entrySet()){
			Integer clusterId = entry.getKey();
			int max = Integer.MIN_VALUE;
			Integer centroid = -1;
			for (Entry<Integer,Integer> entry2 : entry.getValue().entrySet()){
				if (entry2.getValue()>max){
					max = entry2.getValue();
					centroid = entry2.getKey();
				}
			}
			centroids.put(clusterId, centroid);
		}
		return centroids;
	}

}
