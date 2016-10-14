package babelnet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class BabelnetResultsReader {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option dirOption = Option.builder()
				.longOpt("dir")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to dir with Babelnet mappings")
				.build();
		options.addOption(dirOption);
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
		String dirPath = cl.getOptionValue("dir");
		File dir = new File(dirPath);
		if (!dir.exists()) {
			System.out.println(dirPath+" does not exist.");
			System.exit(1);
		}
		if (!dir.isDirectory()) {
			System.out.println(dirPath+" is not a directory.");
			System.exit(1);
		}
		Map<String,Map<Integer,String>> topDomainNamesPerClusterIdPerFile = new HashMap<String,Map<Integer,String>>();
		Map<String,Map<Integer,Double>> topDomainScoresPerClusterIdPerFile = new HashMap<String,Map<Integer,Double>>();
		for (File f : dir.listFiles()){
			try {
				if (f.isFile() && f.getName().endsWith("BabelnetMapping.txt")) {
					BufferedReader br = new BufferedReader(new FileReader(f));
					Map<Integer,String> topDomainNamesMap = new HashMap<Integer,String>();
					Map<Integer,Double> topDomainScoresMap = new HashMap<Integer,Double>();
					readBabelnetMapping(br, topDomainNamesMap, topDomainScoresMap);
					topDomainNamesPerClusterIdPerFile.put(f.getName(), topDomainNamesMap);
					topDomainScoresPerClusterIdPerFile.put(f.getName(), topDomainScoresMap);
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter fw = new FileWriter("BabelnetMappingScores.txt");
			fw.write("This map contains the average score per file, the score considered is the second domain score: the max simple score which is the sum of weights from matched words in the domain divided by the cluster size\n");
			for (String fileName : topDomainNamesPerClusterIdPerFile.keySet()){
				Double avgScore = 0.;
				for (Double score : topDomainScoresPerClusterIdPerFile.get(fileName).values()){
					avgScore += score;
				}
				avgScore /= topDomainScoresPerClusterIdPerFile.size();
				fw.write(fileName+"\t"+avgScore+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void readBabelnetMapping(BufferedReader br,
			Map<Integer, String> topDomainNamesMap,
			Map<Integer, Double> topDomainScoresMap) throws IOException {

		String line = null;
		while ((line = br.readLine())!=null){
			Integer clusterId = Integer.valueOf(line.split("\t")[0]);
			if (line.contains("\t\t") || line.split("\t").length<10) {
				topDomainNamesMap.put(clusterId, "No domain found");
				topDomainScoresMap.put(clusterId, 0.);
			} else {
				String topDomainName = line.split("\t")[8];
				try{
					Double topDomainScore = Double.valueOf(line.split("\t")[9]);
					topDomainNamesMap.put(clusterId, topDomainName);
					topDomainScoresMap.put(clusterId, topDomainScore);
				} catch (java.lang.NumberFormatException e){
					topDomainNamesMap.put(clusterId, "No domain found");
					topDomainScoresMap.put(clusterId, 0.);
				}
			}
		}

	}

}
