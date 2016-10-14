package hypernyms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map.Entry;

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

public class IsasAnalysis {

	private static final Integer NB_OCCUR_THRESHOLD = 3;

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
		Option wordsFreqFileOption = Option.builder()
				.longOpt("wordsFreq")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to wordsFreqFile")
				.build();
		options.addOption(wordsFreqFileOption);
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
		String freqMapFileName = cl.getOptionValue("wordsFreq");
		Map<String,Integer> freqMap = null;
		try {
			freqMap = FrequentWords.buildFreqMap(freqMapFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String isasStem = cl.getOptionValue("isasStem");
		String outputFileName = clustersFileName.substring(0, clustersFileName.length()-4)+"IsasDistribution.txt";
		String isasClusterFreqFileName = clustersFileName.substring(0, clustersFileName.length()-4)+"IsasClustersFreq.txt";

		try {
			BufferedReader br = new BufferedReader(new FileReader(clustersFileName));
			String line;
			File fo = new File(outputFileName);
			FileWriter fw = new FileWriter(fo);
			Map<String,Integer> clusterFrequencyMap = new HashMap<String,Integer>();
			Map<Integer,Map<String,Integer>> isasMaps = new HashMap<Integer,Map<String,Integer>>();
			Integer nbClusters = 0;
			while ( (line = br.readLine()) != null){
				nbClusters++;
				Integer noCluster = Integer.valueOf(line.split("\t")[0]);
				List<String> words = Arrays.asList(line.split("\t")[1].split(","));
				Map<String,Integer> isasMap = new HashMap<String, Integer>();
				for (String wordWithTagAndSense : words){
					String word = wordWithTagAndSense.split("#")[0];
					List<IsA> isas = IsasSearch.searchIsA(isasStem, word);
					for (IsA isa : isas){
						if (isasMap.containsKey(isa.getHypernym())){
							isasMap.put(isa.getHypernym(), isasMap.get(isa.getHypernym()) + 1);//isa.getWeight());
						} else {
							isasMap.put(isa.getHypernym(), 1);//isa.getWeight());
						}
						
					}
				}
				Map<IsA,Integer> isasWithFreq = new HashMap<IsA,Integer>();
				for (Entry<String, Integer> entry : isasMap.entrySet()){
					MapUtil.incrementMapForKey(clusterFrequencyMap, entry.getKey());
					int freq = 0;
					if (freqMap.containsKey(entry.getKey()+"#NN")){
						freq = freqMap.get(entry.getKey()+"#NN");
					}
					if (isasMap.get(entry.getKey())>NB_OCCUR_THRESHOLD) isasWithFreq.put(new IsA(entry.getKey(), isasMap.get(entry.getKey()), freq), isasMap.get(entry.getKey()));
				}
				isasMaps.put(noCluster, isasMap);
				new Gson().toJson(isasWithFreq, fw);
				fw.write("\n");
				fw.flush();
				writeTop1000Isas(isasClusterFreqFileName, clusterFrequencyMap);
			}
			String isasIDFFileName = clustersFileName.substring(0, clustersFileName.length()-4)+"IsasIDF.txt";
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
				isasMapAdjustedValues = MapUtil.sortMapByValue(isasMapAdjustedValues);
				fw.write(noCluster+"\t");
				new Gson().toJson(isasMapAdjustedValues, fwIsasIDF);
				fw.write("\n");
				fw.flush();
			}
			fw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
