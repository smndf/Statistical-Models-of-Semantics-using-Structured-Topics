package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
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

import util.CleanTopicWords;

public class MergeDataAndFilterTopics {

	/*
	 * In this class, clusters are evaluated and bad ones are removed from the set of clusters
	 * Result files from metrics computation and post processing, hypernyms research and IsA relationships research
	 * are merged into one file
	 */

	@SuppressWarnings({ "deprecation", "static-access" })
	public static void main( String[] args){

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with topics, format ex: 0       175     17658   105044  0.17    circumstance#NN#1(4258),tutelage#NN#0(4109),guidance#NN#3(4101)")
				.isRequired()
				.create("clusters"));
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with wordnet hypernyms, format ex: 0       rod, bid, bar")
				.isRequired()
				.create("hyps"));
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with IsA relationships, format ex: 0       fish, animal, water")
				.isRequired()
				.create("isas"));

		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("name of output file, default = input clusters file +\"Processed\"")
				.create("out"));
		options.addOption(OptionBuilder.withArgName("integer").hasArg()
				.withDescription("max. size of each cluster")
				.create("maxSize"));
		options.addOption(OptionBuilder.withArgName("integer").hasArg()
				.withDescription("min. size of each cluster")
				.create("minSize"));
		options.addOption(OptionBuilder.withArgName("double").hasArg().withDescription("min. score").create("minScore"));
		
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
			formatter.printHelp("java -jar processClusters.jar", options, true);
			System.exit(1);
		}
		String clustersFile = cl.getOptionValue("clusters");
		String hypsFile = cl.getOptionValue("hyps");
		String isasFile = cl.getOptionValue("isas");
		String outputFile = cl.hasOption("out") ? cl.getOptionValue("out") : 
			clustersFile.split("/")[clustersFile.split("/").length-1].substring(0, clustersFile.split("/")[clustersFile.split("/").length-1].length()-4)+"Processed.txt";
		double minScore = cl.hasOption("minScore") ? Double.parseDouble(cl.getOptionValue("minScore")) : 0.0;
		int maxSize = cl.hasOption("maxSize") ? Integer.parseInt(cl.getOptionValue("maxSize")) : Integer.MAX_VALUE;
		int minSize = cl.hasOption("minSize") ? Integer.parseInt(cl.getOptionValue("minSize")) : 3;

		try {
			processClusters(clustersFile, hypsFile, isasFile, minScore, minSize, maxSize, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static class HypernymsAndMetrics{
		private Set<String> hypernyms;
		private Map<String,Integer> mapHypCount;
		private Double avDepth;
		private Double similarity;

		public HypernymsAndMetrics(Set<String> hypernyms,
				Map<String, Integer> mapHypCount, Double avDepth, Double similarity) {
			super();
			this.hypernyms = hypernyms;
			this.mapHypCount = mapHypCount;
			this.avDepth = avDepth;
			this.similarity = similarity;
		}
		public Set<String> getHypernyms() {
			return hypernyms;
		}
		public void setHypernyms(Set<String> hypernyms) {
			this.hypernyms = hypernyms;
		}
		public Map<String, Integer> getMapHypCount() {
			return mapHypCount;
		}
		public void setMapHypCount(Map<String, Integer> mapHypCount) {
			this.mapHypCount = mapHypCount;
		}
		public Double getAvDepth() {
			return avDepth;
		}
		public void setAvDepth(Double avDepth) {
			this.avDepth = avDepth;
		}
		public Double getSimilarity() {
			return similarity;
		}
		public void setSimilarity(Double similarity) {
			this.similarity = similarity;
		}

	}

	private static void processClusters(String nodesFile, String hypernymsFile,
			String isasFile, double minScore, int minSize, int maxSize, String outputFile) throws IOException {

		BufferedReader nodesBR = new BufferedReader(new FileReader(nodesFile));
		BufferedReader hypernymsBR = new BufferedReader(new FileReader(hypernymsFile));
		BufferedReader isasBR = new BufferedReader(new FileReader(isasFile));

		String nodesLine;
		String hypernymsLine;
		String isasLine;
		FileWriter fw = new FileWriter (new File(outputFile));

		Map<Integer,String> mapHypernyms = new HashMap<Integer,String>();
		while ((hypernymsLine = hypernymsBR.readLine()) != null){
			mapHypernyms.put(Integer.valueOf(hypernymsLine.split("\t")[0]), hypernymsLine.split("\t")[1]);
		}

		Map<Integer,String> mapIsas = new HashMap<Integer,String>();
		while ((isasLine = isasBR.readLine()) != null){
			mapIsas.put(Integer.valueOf(isasLine.split("\t")[0]), isasLine.split("\t")[2]);
		}


		while ((nodesLine = nodesBR.readLine()) != null){ 
			//HypernymsAndMetrics hypernyms = mapHypernyms.get(Integer.valueOf(nodesLine.split("\t")[0]));
			Integer clusterId = Integer.valueOf(nodesLine.split("\t")[0]);
			String isas = mapIsas.get(clusterId);
			String hypernyms = mapHypernyms.get(clusterId);
			String processedLine = processCluster(nodesLine, hypernyms, isas, minScore, minSize, maxSize);
			if (processedLine != null){
				fw.write(nodesLine.split("\t")[0]+"\t"+processedLine + "\n");
			}

		}
		nodesBR.close();
		hypernymsBR.close();
		isasBR.close();
		fw.flush();
		fw.close();
	}

	private static String toString(Set<String> hypernyms) {
		StringWriter sw = new StringWriter();
		for (String s : hypernyms){
			sw.append(s+", ");
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-2);
		return sw.toString();
	}

	private static HypernymsAndMetrics createHypernymsAndMetrics(
			String hypernymsLine) {

		Set<String> hypernyms = new HashSet<String>();
		Map<String,Integer> map = new HashMap<String,Integer>();
		String[] constituents = hypernymsLine.split("\t")[2].split(", ");
		//String[] constituents = hypernymsLine.split("\t")[1].split(", ");
		for (int i=0;i<constituents.length;i++){
			hypernyms.add(constituents[i].split(",")[0]);
			try {				
				map.put(constituents[i].split(",")[0], Integer.valueOf(constituents[i].split(",")[1].split(":")[1].split("/")[0]));
			} catch (java.lang.ArrayIndexOutOfBoundsException e){
				System.err.println("line = "+hypernymsLine);
				e.printStackTrace();
			}
		}
		Double depth = Double.valueOf(hypernymsLine.split("\t")[1]);
		//Double depth = (Double.valueOf(constituents[0].split("=")[1].split(":")[0]) + Double.valueOf(constituents[1].split("=")[1].split(":")[0]) + Double.valueOf(constituents[2].split("=")[1].split(":")[0]))/3;
		String similarity = hypernymsLine.split("\t")[6];
		Double similarityValue = 0.;
		if (similarity.equals("NaN")) similarityValue = 0.;
		else if (similarity.equals("Infinity")) similarityValue = 1.;
		else similarityValue = Double.valueOf(similarity);
		return new HypernymsAndMetrics(hypernyms, map, depth,similarityValue);
	}

	private static String processCluster(String nodesLine, String hypernyms,
			String isas, double minScore, int minSize, int maxSize) {

		nodesLine = CleanTopicWords.cleanLine(nodesLine);
		StringWriter sw = new StringWriter();
		Integer size = Integer.valueOf(nodesLine.split("\t")[1]);
		Integer nbTriangles = Integer.valueOf(nodesLine.split("\t")[2]);
		Integer nbTriplets = Integer.valueOf(nodesLine.split("\t")[3]);
		boolean goodCluster = isItAGoodCluster(size,nbTriangles,nbTriplets, minScore, minSize, maxSize);
		if (!goodCluster){
			return null;
		} else {
			Double score = computeScore(size, nbTriangles, nbTriplets);
			String nodes = nodesLine.split("\t")[5];
			Set<String> nodesSet = new HashSet<String>();
			for (String s : nodes.split(",")){
				//sw.append(s.split("#")[0]+s.substring(s.lastIndexOf("("))+",");
				//sw.append(s.split("#")[0]+",");
				if (!nodesSet.contains(s)){					
					if (s.contains("\\(")){
						sw.append(s.split("\\(")[0]+",");
					} else {
						sw.append(s+",");
					}
					nodesSet.add(s);
				}
				
			}
			sw.getBuffer().setLength(sw.getBuffer().length()-1);

			sw.append("\t"+hypernyms+"\t"+isas+"\t"+size+"\t"+score);
			return sw.toString();
		}
	}

	private static Double computeScore(Integer size, Integer nbTriangles,
			Integer nbTriplets) {
		Integer nbTripletsMax = size*(size-1)*(size-2)/6;
		double clusterCoef = nbTriangles.doubleValue()/nbTripletsMax.doubleValue();
		double tripletsCoef = nbTriplets.doubleValue()/nbTripletsMax.doubleValue();
		double score = size*clusterCoef*tripletsCoef;
		return score;
	}

	private static boolean isItAGoodCluster(Integer size, Integer nbTriangles,
			Integer nbTriplets, double minScore, int minSize, int maxSize) {
		double score = computeScore(size, nbTriangles, nbTriplets);
		if (score<minScore || size<minSize || size>maxSize)	{
			System.out.println(nbTriangles + " " + nbTriplets+ " "+score + " "+size);
			return false;
		}
		return true;
	}

}
