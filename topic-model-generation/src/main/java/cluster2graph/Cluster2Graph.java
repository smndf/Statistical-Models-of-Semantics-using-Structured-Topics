package cluster2graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import graph.BuildGraph;
import graph.Graph;
import graph.ResultatBuildGraph;

public class Cluster2Graph {

	public static Map<String, Integer> mapNodeToCentrality;

	/* CODE BELOW IS USED TO PROCESS THE 7 TOPICS
	 * public static void main(String[] args) {
		try {
				String clustersFile = "/CWddt-wiki-n200-380k-v3-closureFiltBef2FiltAft.txt";
				BufferedReader br = new BufferedReader(new InputStreamReader(Cluster2Graph.class.getResourceAsStream(clustersFile)));
				int noline = 1;
				String line;
				File f = new File("7clustersNotFiltered.txt");
				FileWriter fw = new FileWriter(f);
				while((line= br.readLine())!=null){
					if (noline==41 || noline==183 || noline==301 || noline==396 || noline==452 || noline==586 || noline==588){
						fw.write(noline+"\t"+removeFirstPartAndSpace(line)+"\n");
						fw.flush();
					}
				noline ++;	
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String removeFirstPartAndSpace(String line) {
		StringWriter sw = new StringWriter();
		String[] lineSplit = line.split("\t");
		StringWriter sw2 = new StringWriter();
		String words = lineSplit[lineSplit.length-1];
		for (String s : words.split(", ")){
			sw2.append(s+",");
		}
		sw2.getBuffer().setLength(sw2.getBuffer().length()-1);
		lineSplit[lineSplit.length-1] = sw2.toString();
		for (int i = 1 ; i<lineSplit.length ; i++){
			sw.append(lineSplit[i]+"\t");
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-1);
		return sw.toString();
	}
	 */

	public static void main(String[] args) {
		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with clusters e.g. CWddt-wiki-mwe-posFiltBef2FiltAft.txt")
				.isRequired()
				.create("clusters"));
		options.addOption(OptionBuilder.withArgName("dir").hasArg()
				.withDescription("directory with map files word -> image URL")
				.isRequired()
				.create("images"));
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("ddt file not clustered (but filtered) e.g. ddt-wiki-mwe-posFiltBef2.txt")
				.isRequired()
				.create("ddt"));
		options.addOption(OptionBuilder.withArgName("dir").hasArg()
				.withDescription("name of output directory, default = input clusters file -\".xxx\"/withoutPositions")
				.create("out"));

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
			formatter.printHelp("java -jar clusterToGraph.jar", options, true);
			System.exit(1);
		}
		String clustersFile = cl.getOptionValue("clusters");
		String ddtFile = cl.getOptionValue("ddt");
		String imagesDir = cl.getOptionValue("images");
		String outDir = cl.hasOption("out") ? cl.getOptionValue("out") : clustersFile.substring(0,clustersFile.length()-4)+"/withoutPositions";


		try {
			ResultatBuildGraph graph = BuildGraph.buildGraph(ddtFile);
			Map<Integer, Set<Integer>> clusters = getClusters(clustersFile,graph);
			Map<String,String> imagesMap = getImageURLs(imagesDir);
			for (String url : imagesMap.keySet()) System.out.print(url);
			Map<Integer, String[]> res = getStringGraphs(clusters,imagesMap,graph);
			writeNodesAndEdges(res,outDir);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private static Map<String, String> getImageURLs(String imagesDir) {

		File dir = new File(imagesDir);
		if (!dir.exists()){
			System.err.println("Directory for images not found.");
			System.exit(1);
		} else if (!dir.isDirectory()){
			System.err.println(imagesDir +" is not a directory.");
			System.exit(1);
		}

		Map<String,String> imagesMap = new HashMap<String,String>();
		File[] imagesFiles = dir.listFiles();
		for (File imagesFile : imagesFiles){
			try {
				if (imagesFile.isFile()){					
					BufferedReader br = new BufferedReader(new FileReader(imagesFile));
					String line;
					while ((line = br.readLine()) != null){ 
						if (line.split("\t").length==2){
							imagesMap.put(line.split("\t")[0].replaceAll(" ", "_"), line.split("\t")[1]);
						} else System.out.println("line.split(\"\t\").length = "+line.split("\t").length);
					}
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}


		return imagesMap;
	}


	private static void writeNodesAndEdges(Map<Integer, String[]> res, String outDir) {

		/* e.g. inputFile = "CWddt-news-n200-345k-closureFiltBef2FiltAft.txt"
		outputFiles = CWddt-news-n200-345k-closureFiltBef2FiltAft-0.txt , CWddt-news-n200-345k-closureFiltBef2FiltAft-1.txt etc.
		 */
		new File(outDir.split("/")[0]).mkdir();
		// withoutPositions sub dir
		new File(outDir).mkdir();
		// withPositions sub dir
		new File(outDir.split("/")[0]+"/withPositions").mkdir();
		for (Integer noCluster : res.keySet()){
			String outputFile = outDir+"/" + outDir.split("/")[0] + "-" + noCluster + ".txt";
			File fo = new File(outputFile);
			try{
				FileWriter fw = new FileWriter (fo);
				//fw.write (MapUtil.toString(clusters,trianglesTriplets,nodesMapItoS,eigenvectorCentralityScores,"\t","\n"));
				//System.out.println("results written in " + outputFile);
				fw.write (res.get(noCluster)[0] + "\n" + res.get(noCluster)[1]);
				fw.flush();
				fw.close();
			}
			catch (IOException exception)
			{
				System.out.println (exception.getMessage());
			}
		}
	}

	private static Map<Integer, String[]> getStringGraphs(
			Map<Integer, Set<Integer>> clusters, Map<String, String> imagesMap, ResultatBuildGraph rbg) {

		Map<Integer, String[]> res = new HashMap<Integer, String[]>();

		Map<Integer,String> nodesMapItoS = rbg.getNodesMapItoS();
		Graph<Integer, Float> g = rbg.getG();

		for (Integer key : clusters.keySet()){
			Set<Integer> cluster = clusters.get(key);
			System.out.println("cluster "+key+" : "+cluster.size()+" elements");
			String[] graphStrings = new String[2];

			List<Integer> nodesList = new ArrayList<Integer>();
			for (Integer node : cluster){
				nodesList.add(node);
			}
			Collections.sort(nodesList);

			/*System.out.println("liste :");
			for (Integer i : nodesList){
				System.out.print(i+ " ");
			}
			System.out.println();
			 */

			// MAP USED FOR NEW NODES INDEX (from 0 to nbNodesInThisCluster-1)
			Map<Integer,Integer> mapItoI = new HashMap<Integer,Integer>();
			StringWriter nodes = new StringWriter();
			nodes.append("[");
			int ctr = 0;
			for(Integer node : nodesList){
				String nodeAsKey = nodesMapItoS.get(node).replaceAll(" ","_");
				String nodeString = cleanMWE(nodesMapItoS.get(node));
				if (nodeString==null) System.out.println("nodeString null");
				if (nodeString.contains("'")){
					System.out.print("' replaced in "+nodeString+ " : ");
					nodeString = nodeString.replace("'"," ");
					System.out.println(nodeString);
				}
				String imageURL = "http://serelex.org/image/question";
				if (imagesMap==null) System.out.println("imagesMap null");
				if (imagesMap.containsKey(nodeString.toLowerCase())) {
					imageURL = imagesMap.get(nodeString.toLowerCase());
					System.out.println("imageURL = imagesMap.get(nodeAsKey) = "+imagesMap.get(nodeString.toLowerCase()));
				}
				else if (imagesMap.containsKey(nodeString)) {
					System.out.println("imageURL = imagesMap.get(nodeAsKey.toLowerCase()) = "+imagesMap.get(nodeString));
					imageURL = imagesMap.get(nodeString);
				}
				else if (nodeString.split(" ").length>0) {
					System.out.println("imageURL = \"http://serelex.org/image/\"+nodeString.split(\" \")[0] = "+"http://serelex.org/image/"+nodeString.split(" ")[0]);
					imageURL = "http://serelex.org/image/"+nodeString.split(" ")[0];
				}
				System.out.println(imageURL);
				nodes.append("{\"id\":"+ctr+",\"label\":\""+nodeString+"\",\"value\":\""+mapNodeToCentrality.get(nodesMapItoS.get(node))+"\",\"image\":\""+imageURL+"\",\"shape\":\"image\"}");
				nodes.append(",");
				mapItoI.put(node,ctr);
				ctr++;
			}
			// remove last ,
			nodes.getBuffer().setLength(nodes.getBuffer().length()-1);
			nodes.append("]");
			graphStrings[0] = nodes.toString();


			StringWriter edges = new StringWriter();
			edges.append("[");
			for(Integer node : nodesList){
				Iterator<Integer> it = g.getNeighbors(node);
				while(it.hasNext()){
					Integer neighbour = it.next();
					if (neighbour>node && cluster.contains(neighbour)){
						edges.append("{\"from\":\""+mapItoI.get(node) + "\",\"to\":\"" + mapItoI.get(neighbour) + "\"}");
						edges.append(",");
					}
				}
			}
			// remove last ,
			edges.getBuffer().setLength(edges.getBuffer().length()-1);
			edges.append("]");
			graphStrings[1] = edges.toString();

			res.put(key,graphStrings);
		}
		return res;
	}

	private static String cleanMWE(String mwe) {
		if (mwe.contains(" ")){			
			String[] array = mwe.split(" ");
			StringWriter sw = new StringWriter();
			for (String s : array){
				sw.append(s.split("#")[0]+" ");
			}
			sw.getBuffer().setLength(sw.getBuffer().length()-1);
			return sw.toString();
		} else {
			return mwe.split("#")[0];
		}
	}


	private static Map<Integer, Set<Integer>> getClusters(String clustersFile, ResultatBuildGraph graph) throws FileNotFoundException {

		Map<Integer, Set<Integer>> clusters = new HashMap<Integer, Set<Integer>>();

		BufferedReader input = new BufferedReader(new FileReader(clustersFile));
		String[] lineSplit;
		String line = null;

		Map<String,Integer> nodesMapStoI = graph.getNodesMapStoI();

		try {
			mapNodeToCentrality = new HashMap<String,Integer>();
			while ( (line = input.readLine()) != null){ 
				lineSplit = line.split("\t");
				Set<Integer> cluster = new HashSet<Integer>();
				ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[5].split(",")));
				//System.out.println("size="+neighboursSplit.size());
				for (String neighbour : neighboursSplit){	
					String centralityAsString = neighbour.substring(neighbour.lastIndexOf("(")+1, neighbour.length() - 1);
					if (centralityAsString.length()!=0){
						Integer centrality = Integer.valueOf(centralityAsString);
						neighbour = neighbour.substring(0,neighbour.lastIndexOf('('));
						if (nodesMapStoI.get(neighbour) != null){							
							cluster.add(nodesMapStoI.get(neighbour));
							//System.out.println(neighbour);
							mapNodeToCentrality.put(neighbour, centrality);
						}
					}
				}
				Integer size = Integer.valueOf(lineSplit[1]);
				Integer clusterNumber = Integer.valueOf(lineSplit[0]);
				if (size!=cluster.size()) System.out.println("size(cluster "+clusterNumber+") = "+cluster.size()+" instead of "+size);
				clusters.put(clusterNumber, cluster);
			}
			input.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return clusters;
	}

}
