package evaluation;

import graph.ArrayBackedGraph;
import graph.Graph;
import hypernyms.HypernymsSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Set;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class IsasGraph {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with clusters e.g. CWddt-wiki-mwe-posFiltBef2FiltAft.txt")
				.isRequired()
				.create("clusters"));
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
		Map<Integer,Set<Integer>> clusters = new HashMap<Integer,Set<Integer>>();
		Map<Integer,String> mapItoS = new HashMap<Integer,String>();
		Map<String,Integer> mapStoI = new HashMap<String,Integer>();
		try {
			readClusters(clustersFileName, clusters, mapItoS, mapStoI);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String isasStemCommoncrawl = "isas/isas-commoncrawl";
		String isasStemPatternsim = "isas/isas-en-patternsim-ukwac-wacky";
		String isasStemPs59 = "isas/en-isas-ps59g";
		String outputFile = clustersFileName.substring(0, clustersFileName.length()-4)+"IsasGraphPS59g.txt";
		File fo = new File(outputFile);
		FileWriter fw;
		try {
			fw = new FileWriter(fo);
			/*try {
				JWNL.initialize(Thread.currentThread().getContextClassLoader().getResourceAsStream("properties.xml"));
			} catch (JWNLException e) {
				e.printStackTrace();
			}
			HypernymsSearch.wordnet = Dictionary.getInstance();
			 */
			for (Integer clusterId : clusters.keySet()){
				System.out.println("Cluster "+clusterId);
				Set<Integer> cluster = clusters.get(clusterId);
				//System.out.println("Getting hypernyms from wordnet...");
				//Map<Integer,Set<String>> wordnetHypernymsPerNode = HypernymsSearch.getHypernymsPerNode(cluster, mapItoS, mapStoI);
				//System.out.println("Getting isas from Commoncrawl...");
				//Map<Integer,Set<String>> isasCommoncrawlPerNode = getIsasPerNode(cluster, isasStemCommoncrawl, mapItoS, mapStoI);
				System.out.println("Getting isas from Patternsim...");
				Map<Integer,Set<String>> isasPatternsimPerNode = getIsasPerNode(cluster, isasStemPs59, mapItoS, mapStoI);
				System.out.println("Merge maps hypernyms/isas...");
				//Map<Integer,Set<String>> hypsAndIsasPerNode = mergeMapsPerNode(isasCommoncrawlPerNode, isasPatternsimPerNode, wordnetHypernymsPerNode);
				Map<Integer,Set<String>> hypsAndIsasPerNode = mergeMapsPerNode(new HashMap<Integer,Set<String>>(), isasPatternsimPerNode, new HashMap<Integer,Set<String>>());
				System.out.println("Build graph...");
				Graph<Integer, Float> graph = buildIsasGraph(hypsAndIsasPerNode, mapItoS, mapStoI);
				System.out.println("graph has "+graph.getSize()+" nodes.");
				System.out.println("Compute clustering coeff...");
				Double clusteringCoef = computeClusteringCoef(graph);
				System.out.println("C = "+clusteringCoef);
				appendToFile(fw, clusterId, clusteringCoef);
				/*
				System.out.println("Compute average node degree...");
				Double avgDegree = computeAvgDegree(graph);
				appendToFile(fw, clusterId, avgDegree);
				 */
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static Double computeAvgDegree(Graph<Integer, Float> g) {

		Long nbNeighbours = (long) 0;
		Long nbNodes = (long) 0;
		for (Integer node : g.getNodes()){
			Iterator<Integer> it = g.getNeighbors(node);
			nbNodes++;
			while (it.hasNext()) {
				it.next();
				nbNeighbours++;
			}
		}
		return nbNeighbours.doubleValue()/nbNodes.doubleValue();
	}

	private static void appendToFile(FileWriter fw, Integer clusterId, Double clusteringCoef) throws IOException {
		fw.write(clusterId+"\t"+clusteringCoef+"\n");
		fw.flush();		
	}

	private static Map<Integer, Set<String>> mergeMapsPerNode(
			Map<Integer, Set<String>> isasCommoncrawlPerNode,
			Map<Integer, Set<String>> isasPatternsimPerNode,
			Map<Integer, Set<String>> wordnetHypernymsPerNode) {

		Map<Integer, Set<String>> map = new HashMap<Integer, Set<String>>();
		for (Integer node : isasCommoncrawlPerNode.keySet()){
			map.put(node, isasCommoncrawlPerNode.get(node));
		}
		for (Integer node : isasPatternsimPerNode.keySet()){
			if (!map.containsKey(node)) map.put(node, new HashSet<String>());
			map.get(node).addAll(isasPatternsimPerNode.get(node));
		}
		for (Integer node : wordnetHypernymsPerNode.keySet()){
			if (!map.containsKey(node)) map.put(node, new HashSet<String>());
			map.get(node).addAll(wordnetHypernymsPerNode.get(node));
		}
		return map;
	}

	private static Double computeClusteringCoef(final Graph<Integer, Float> g) throws InterruptedException {
		final Set<Integer> nodes = g.getNodes();
		//for each node in the cluster
		final AtomicLong nbTripletsAtomic = new AtomicLong(0);
		final AtomicLong nbTrianglesAtomic = new AtomicLong(0);
		for (final Integer node1 : nodes){
			Iterator<Integer> it1 = g.getNeighbors(node1);
			
				
				final int nbThreads = Runtime.getRuntime().availableProcessors();
				ExecutorService exec = Executors.newFixedThreadPool(nbThreads);
				try {
					while(it1.hasNext()){
						final int node2 = it1.next();
						exec.submit(new Runnable() {
							public void run() {
								if (nodes.contains(node2) && node2 != node1){
									Iterator<Integer> it2 = g.getNeighbors(node2);
									while(it2.hasNext()){
										Iterator<Integer> it1bis = g.getNeighbors(node1);
										int node3;
										if (nodes.contains((node3 = it2.next())) && node3 != node1 && node3 != node2){							
											nbTrianglesAtomic.addAndGet(1);
											while(it1bis.hasNext()){
												if (it1bis.next().intValue() == node3){
													nbTrianglesAtomic.addAndGet(1);
													break;
												}
											}
										}
									}
								}
							}
						});
					}
				} finally {
					exec.shutdown();
				}
				exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

			}
		Long nbTriangles = nbTrianglesAtomic.get();
		Long nbTriplets = nbTripletsAtomic.get();
		nbTriangles /= 6;
		nbTriplets = ((nbTriplets - (nbTriangles*6))/2)+nbTriangles;
		long nbNodes = nodes.size();
		Long nbTrianglesMax = (nbNodes)*(nbNodes-1)*(nbNodes-2)/6;
		Double clusterCoef;
		Double tripletCoef;
		if (nbTrianglesMax==0){
			clusterCoef = (double) 0;
			tripletCoef = (double) 0;
		}
		else {
			clusterCoef = nbTriangles.doubleValue()/nbTrianglesMax.doubleValue();
			tripletCoef = nbTriplets.doubleValue()/nbTrianglesMax.doubleValue();
		}
		if (clusterCoef<0){
			System.out.println("clusterCoef = "+clusterCoef);
			System.out.println("clusterCoef<0 ... why?");
			System.out.println("nbTriangles = "+nbTriangles);
			System.out.println("nbTriplets = "+nbTriplets);
			System.out.println("nbTrianglesMax = "+nbTrianglesMax);
		}
		return clusterCoef;
	}

	private static Map<Integer, Graph<Integer, Float>> buildIsasGraphs(
			Map<Integer, Map<Integer, Set<String>>> hypsPerNodePerCluster, Map<Integer, String> mapItoS, Map<String, Integer> mapStoI) {

		Map<Integer, Graph<Integer, Float>> isasGraphs = new HashMap<Integer, Graph<Integer, Float>>();
		for (Entry<Integer,Map<Integer, Set<String>>> entry : hypsPerNodePerCluster.entrySet()){
			isasGraphs.put(entry.getKey(), buildIsasGraph(entry.getValue(), mapItoS, mapStoI));
		}
		return isasGraphs;
	}

	private static Graph<Integer, Float> buildIsasGraph(
			Map<Integer, Set<String>> isasPerNode, Map<Integer, String> mapItoS, Map<String, Integer> mapStoI) {

		Graph<Integer, Float> g = new ArrayBackedGraph<Float>(10, 1);
		Set<String> nodesOfCluster = new HashSet<String>();
		for (Integer node : isasPerNode.keySet()){
			nodesOfCluster.add(mapItoS.get(node));
			g.addNode(node);
		}
		Map<String,Set<Integer>> nodesPerIsas = new HashMap<String,Set<Integer>>();
		for (Integer node : isasPerNode.keySet()){
			for (String isa : isasPerNode.get(node)){
				if (!nodesPerIsas.containsKey(isa)){
					nodesPerIsas.put(isa, new HashSet<Integer>());
				}
				nodesPerIsas.get(isa).add(node);
				// add edge between node1 and node2 with isa=node1
				if (nodesOfCluster.contains(isa)){
					int neighbour = mapStoI.get(isa);
					if (!hasEdge(g, node, neighbour)) g.addEdgeUndirected(node, neighbour, Float.valueOf(1));
				}
			}
		}
		for (Set<Integer> nodes : nodesPerIsas.values()){
			if (nodes.size()>1){
				for (Integer node1 : nodes){
					for (Integer node2 : nodes){
						if (node1 != node2 && !hasEdge(g, node1, node2)){
							g.addEdgeUndirected(node1, node2, Float.valueOf(1));
						}
					}
				}
			}
		}
		return g;
	}

	private static boolean hasEdge(Graph<Integer, Float> g, Integer node1, Integer node2) {
		
		Iterator<Integer> it = g.getNeighbors(node1);
		while (it.hasNext()) {
			int neighbour = it.next();
			if (neighbour==node2){
				return true;
			}
		}
		return false;
	}

	private static Map<Integer, Set<String>> getIsasPerNode(Set<Integer> topicNodes,
			final String isasStem, final Map<Integer, String> mapItoS,
			Map<String, Integer> mapStoI) throws InterruptedException {

		final Map<Integer, Set<String>> isasPerNode = new ConcurrentHashMap<Integer, Set<String>>();
		final AtomicInteger currentNumber = new AtomicInteger(0);
		final int nbThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService exec = Executors.newFixedThreadPool(nbThreads);
		try {
			for (final Integer node : topicNodes){
				exec.submit(new Runnable() {
					public void run() {
						String nodeString = mapItoS.get(node);
						try {
							isasPerNode.put(node, getIsasForNode(nodeString, isasStem));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		} finally {
			exec.shutdown();
		}
		exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		return isasPerNode;

	}

	private static Set<String> getIsasForNode(String word, String isasFileStem) throws IOException {

		Set<String> isas = new HashSet<String>();
		if (word.length()>1){
			String isasFile = isasFileStem + "-" + word.substring(0, 2) + ".csv";
			try{
				if (new File (isasFile).exists()){
					BufferedReader isasText = new BufferedReader(new FileReader(isasFile));
					String line;
					while ( (line = isasText.readLine()) != null){ 
						if (line.split("\t")[0].toLowerCase().equals(word)){
							String hyp = line.split("\t")[1];
							isas.add(hyp);
						}
					}
					isasText.close();
				}
			}catch (FileNotFoundException e) {
				System.out.println("no file "+isasFile);
			}
		}
		return isas;
	}

	private static void readClusters(String clustersFileName,
			Map<Integer,Set<Integer>> clusters, Map<Integer, String> mapItoS,
			Map<String, Integer> mapStoI) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(clustersFileName));
		String line;
		while ( (line = br.readLine()) != null){
			Set<Integer> cluster = new HashSet<Integer>();
			String topicNodes = line.split("\t")[1];
			for (String node : topicNodes.split(",")){ //TODO
				//node = node.split("\\(")[0];
				if (node.contains("#")) node = node.split("#")[0];
				if (!mapStoI.containsKey(node)){
					mapStoI.put(node, mapStoI.size());
					mapItoS.put(mapItoS.size(), node);
					cluster.add(mapStoI.get(node));
				}
			}
			clusters.put(Integer.valueOf(line.split("\t")[0]),cluster);
		}
		br.close();
	}

}
