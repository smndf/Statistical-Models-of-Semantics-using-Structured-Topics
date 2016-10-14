package hypernyms;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import util.MapUtil;

import com.google.gson.Gson;

import frequency.FrequentWords;

public class IsasTFIDF {

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
		Option isasStemOption = Option.builder()
				.longOpt("isasStem")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("stem of isasFiles")
				.build();
		options.addOption(isasStemOption);
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
		
		final String isasStem = cl.getOptionValue("isasStem");
		String outputFileName = clustersFileName.substring(0, clustersFileName.length()-4)+"IsasDistribution.txt";

		try {
			BufferedReader br = new BufferedReader(new FileReader(clustersFileName));
			String line;
			File fo = new File(outputFileName);
			FileWriter fw = new FileWriter(fo);
			Map<String,Integer> clusterFrequencyMap = new HashMap<String,Integer>();
			Map<Integer,Map<String,Integer>> isasMaps = new HashMap<Integer,Map<String,Integer>>();
			Integer nbClusters = 0;
			final Map<Integer,Set<String>> clusters = new ConcurrentHashMap<Integer,Set<String>>();
			while ( (line = br.readLine()) != null){
				nbClusters++;
				final Integer noCluster = Integer.valueOf(line.split("\t")[0]);
				clusters.put(noCluster, new HashSet<String>());
				List<String> words = Arrays.asList(line.split("\t")[1].split(","));
				System.out.println(words.size()+" words");
				final Map<String,Integer> isasMap = new ConcurrentHashMap<String, Integer>();
				final int nbThreads = Runtime.getRuntime().availableProcessors();
				ExecutorService exec = Executors.newFixedThreadPool(nbThreads);
				try {
					try {
						for (final String wordWithTagAndSense : words){
							final String word = wordWithTagAndSense.split("#")[0];
							clusters.get(noCluster).add(word);
							exec.submit(new Runnable() {
								public void run() {
									System.out.println("word = "+word);
									//System.out.println("clusters.get(noCluster).size() = "+clusters.get(noCluster).size());
									List<IsA> isas = new ArrayList<>();
									try {
										isas = IsasSearch.searchIsA(isasStem, word);
									} catch (NumberFormatException
											| IOException e) {
										e.printStackTrace();
									}
									if (isas.size()==0) {
										System.out.println("isas.size()==0");
									}
									for (IsA isa : isas){
										if (isasMap.containsKey(isa.getHypernym())){
											isasMap.put(isa.getHypernym(), isasMap.get(isa.getHypernym()) + isa.getWeight());
										} else {
											isasMap.put(isa.getHypernym(), isa.getWeight());
										}
										
									}
								}
							});
						}
					} finally {
						exec.shutdown();
					}
					exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Map<String,Integer> isasMap2 = new HashMap<String,Integer>();
				for (Map.Entry<String, Integer> e : isasMap.entrySet()) isasMap2.put(e.getKey(), e.getValue());
				isasMap2 = MapUtil.sortMapByValue(isasMap2);
				System.out.println("isasMap2.size() = "+isasMap2.size());
				isasMap2 = keepNfirstEntries(isasMap2, 100);//isasMap.entrySet().stream().limit(100).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				System.out.println("isasMap2.size() = "+isasMap2.size());
				Map<IsA,Integer> isasWithFreq = new HashMap<IsA,Integer>();
				for (Entry<String, Integer> entry : isasMap2.entrySet()){
					MapUtil.incrementMapForKey(clusterFrequencyMap, entry.getKey());
					int freq = 0;
					isasWithFreq.put(new IsA(entry.getKey(), isasMap2.get(entry.getKey()), freq), isasMap2.get(entry.getKey()));
				}
				isasMaps.put(noCluster, isasMap2);
				new Gson().toJson(isasWithFreq, fw);
				fw.write("\n");
				fw.flush();
				//writeTop1000Isas(isasClusterFreqFileName, clusterFrequencyMap);
			}
			fw.close();
			String isasIDFFileName = clustersFileName.substring(0, clustersFileName.length()-4)+"IsasIDFWeight.txt";
			Map<String,Double> idfValues = getIDFIsas(isasIDFFileName, clusterFrequencyMap, nbClusters);
			FileWriter fwIsasIDF = new FileWriter(isasIDFFileName);
			for (Map.Entry<Integer,Map<String,Integer>> entry : isasMaps.entrySet()){
				Integer noCluster = entry.getKey();
				Map<String,Integer> isasMapRawValues = entry.getValue();
				Map<String,Double> isasMapAdjustedValues = new HashMap<String, Double>();
				for (Map.Entry<String,Integer> isaEntry : isasMapRawValues.entrySet()){
					String isa = isaEntry.getKey();
					if (idfValues.containsKey(isa)){
						Double score = isasMapRawValues.get(isa) * idfValues.get(isa);
						isasMapAdjustedValues.put(isa, score);
					}
				}
				System.out.println("isasMapAdjustedValues.size() = "+isasMapAdjustedValues.size());
				isasMapAdjustedValues = MapUtil.sortByValue(isasMapAdjustedValues);
				System.out.println("isasMapAdjustedValues.size() = "+isasMapAdjustedValues.size());
				isasMapAdjustedValues = keepNfirstEntries(isasMapAdjustedValues, 3);//.entrySet().stream().limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				System.out.println("isasMapAdjustedValues.size() = "+isasMapAdjustedValues.size());
				isasMapAdjustedValues = MapUtil.sortByValue(isasMapAdjustedValues);
				fwIsasIDF.write(noCluster+"\t"+write20WordsTopic(clusters.get(noCluster))+"\t");
				StringWriter sw = new StringWriter();
				for (String isa : isasMapAdjustedValues.keySet()){
					sw.append(isa/*+"#"+isasMapAdjustedValues.get(isa)*/+", ");
				}
				System.out.println("sw.getBuffer().length() = "+sw.getBuffer().length());
				if (sw.getBuffer().length() > 1) sw.getBuffer().setLength(sw.getBuffer().length()-2);
				else sw.append("NO ISA RELATIONS");
				fwIsasIDF.write(sw.toString()+"\n");
				fwIsasIDF.flush();
			}
			fwIsasIDF.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String write20WordsTopic(Set<String> set) {

		StringWriter sw = new StringWriter();
		int i = 0;
		for (String s : set){
			if (i<20) sw.append(s+", ");
			i++;
		}
		if (sw.getBuffer().length()>2) sw.getBuffer().setLength(sw.getBuffer().length()-2);
		return sw.toString();
	}

	private static <T> Map<String, T> keepNfirstEntries(
			Map<String, T> map, int n) {

		Map<String, T> res = new HashMap<String, T>();
		int i=0;
		for (Map.Entry<String, T> entry : map.entrySet()){
			if (i<n){
				res.put(entry.getKey(), entry.getValue());
			}
			i++;
		}
		
		return res;
	}

	private static Map<String, Integer> readIsasFreq(String isasFreqFileName) {
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(isasFreqFileName));
			while ((line=br.readLine()) != null){
				String[] split = line.split("/t");
				String isa = split[0];
				Integer val = Integer.valueOf(split[1]);
				map.put(isa, val);
			}
			br.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	private static Map<String,Double> getIDFIsas(String isasIDFFileName,
			Map<String, Integer> clusterFrequencyMap, Integer nbClusters) throws IOException {

		//FileWriter fw = new FileWriter(isasIDFFileName);
		int nb = 0;
		clusterFrequencyMap = MapUtil.sortMapByValue(clusterFrequencyMap);
		Map<String,Double> idfValues = new HashMap<String,Double>();
		for (Map.Entry<String, Integer> entry : clusterFrequencyMap.entrySet()){
			double idfValue = Math.log10(nbClusters.doubleValue()/entry.getValue().doubleValue());
			//fw.write(entry.getKey()+"\t"+idfValue+"\n");
			idfValues.put(entry.getKey(),idfValue);
			nb++;
		}
		//fw.close();
		return idfValues;
	}

	private static void writeTop1000Isas(String isasClusterFreqFileName,
			Map<String, Integer> clusterFrequencyMap) throws IOException {

		clusterFrequencyMap = MapUtil.sortMapByValue(clusterFrequencyMap);
		FileWriter fw = new FileWriter(isasClusterFreqFileName);
		int nb = 0;
		for (Map.Entry<String, Integer> entry : clusterFrequencyMap.entrySet()){
			fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			nb++;
			if (nb==1000) break;
		}
		fw.close();
	}

}
