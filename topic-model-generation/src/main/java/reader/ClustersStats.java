package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ClustersStats {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("input clusters file: 1\tGretzky#NP#1,Beckham#NP#1,Ronaldo#NP#1,Kewe").isRequired().create("in"));
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
		String inputFile = cl.getOptionValue("in");
		String outputFile = inputFile+"Stats.txt";
		Map<Integer,Integer> clustersSizes = new HashMap<Integer,Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = null;
			while ((line=br.readLine())!=null){
				clustersSizes.put(clustersSizes.size(),line.split("\t")[1].split(",").length);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Integer nbClusters = clustersSizes.size();
		Double avSizeClusters = 0.0;
		Integer minSize = Integer.MAX_VALUE;
		Integer maxSize = Integer.MIN_VALUE;
		for (Integer clusterSize : clustersSizes.values()) {
			avSizeClusters += clusterSize;
			if (clusterSize>maxSize) {
				maxSize = clusterSize;
			}
			if (clusterSize<minSize) {
				minSize = clusterSize;
			}
		}
		avSizeClusters /= nbClusters.doubleValue();
		Double standardDeviation = 0.0;
		for (Integer clusterSize : clustersSizes.values()) standardDeviation += (clusterSize-avSizeClusters) * (clusterSize-avSizeClusters);
		standardDeviation /= nbClusters.doubleValue();
		standardDeviation = Math.sqrt(standardDeviation);
		try {
			FileWriter fw = new FileWriter(outputFile);
			fw.write(inputFile.split("\\.")[0]+"\t"+nbClusters+"\t"+avSizeClusters+"\t"+standardDeviation+"\t"+minSize+"\t"+maxSize+"\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
