package toAppFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TopicList {

	/*
	 * Generate lists of topics
	 * sorted by id, size, score
	 */
	public static void main(String[] args) {
		
		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with topics processed, format: topicId	topicWords	wordnetHypernyms	isas	size	score")
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
			formatter.printHelp("java -jar generateTopicsLists.jar", options, true);
			System.exit(1);
		}
		String clustersFile = cl.getOptionValue("clusters");
		Map<String, Integer> mapLineToId = new HashMap<String, Integer>();
		Map<String, Integer> mapLineToSize = new HashMap<String, Integer>();
		Map<String, Double> mapLineToScore = new HashMap<String, Double>();
		try {
			readFile(clustersFile, mapLineToId, mapLineToScore, mapLineToSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mapLineToId = util.MapUtil.sortMapByValue(mapLineToId);
		mapLineToSize = util.MapUtil.sortMapByValue(mapLineToSize);
		mapLineToScore = util.MapUtil.sortMapByValue(mapLineToScore);
		try {
			writeValuesIntoFile(clustersFile.substring(0,clustersFile.length()-4)+"ById.txt", mapLineToId);
			writeValuesIntoFile(clustersFile.substring(0,clustersFile.length()-4)+"BySize.txt", mapLineToSize);
			writeValuesIntoFile(clustersFile.substring(0,clustersFile.length()-4)+"ByScore.txt", mapLineToScore);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static <T> void writeValuesIntoFile(String fileName,
			Map<String, T> map) throws IOException {
		File fo = new File(fileName);
		FileWriter fw = new FileWriter(fo);
		for (Entry<String, T> entry : map.entrySet()){
			fw.write(entry.getKey()+"\n");
		}
		fw.close();
	}

	private static void readFile(String clustersFile,
			Map<String, Integer> mapLineToId,
			Map<String, Double> mapLineToScore,
			Map<String, Integer> mapLineToSize) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(clustersFile));
		String line;
		while ((line = br.readLine()) != null){
			Integer id = Integer.valueOf(line.split("\t")[0]);
			Integer size = Integer.valueOf(line.split("\t")[4]);
			Double score = Double.valueOf(line.split("\t")[5]);
			mapLineToId.put(line, id);
			mapLineToSize.put(line, size);
			mapLineToScore.put(line, score);
		}
		br.close();
		
	}

}
