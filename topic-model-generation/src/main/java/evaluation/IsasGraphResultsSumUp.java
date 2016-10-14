package evaluation;

import graph.Graph;

import java.io.BufferedReader;
import java.io.File;
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

public class IsasGraphResultsSumUp {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("dir").hasArg()
				.withDescription("dir where isasGraph results files are.")
				.isRequired()
				.create("dir"));
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
		String dirName = cl.getOptionValue("dir");
		File dir = new File(dirName);
		if (!dir.exists()) {
			System.err.println("dir "+dirName+" not found.");
			System.exit(1);
		}
		if (!dir.isDirectory()) {
			System.err.println("dir "+dirName+" is not a directory.");
			System.exit(1);
		}
		if (dirName.charAt(dirName.length()-1)=='/') dirName = dirName.substring(0, dirName.length()-1);
		String outputFile = dirName+"/isasGraphResultsAllFiles.txt";
		try {
			FileWriter fw = new FileWriter(outputFile);
			fw.write("file\tratio non zero elements\tavg score non zero elements\tavg score all elements\n");
			for (File f : dir.listFiles()){
				//System.out.print(f.getName());
				if (!f.getName().contains("IsasGraphPS59g.txt")) continue;
				System.out.println("OK");

				System.out.println("File "+f.getName());
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = null;
				Double sumScores = 0.;
				Double nbNonZeroElements = 0.;
				int nbClusters = 0;
				while ((line=br.readLine()) != null){
					Double scoreCluster = Double.valueOf(line.split("\t")[1]);
					if (scoreCluster!=0.0) nbNonZeroElements++;
					sumScores += scoreCluster;
					nbClusters++;
				}
				Double avgScore = sumScores / nbClusters;
				Double ratioNonZeroElements = nbNonZeroElements / nbClusters;
				Double avgScoreNonZeroElements = 0.0;
				if (ratioNonZeroElements==0.0) avgScoreNonZeroElements = 0.;
				else avgScoreNonZeroElements = sumScores / nbNonZeroElements;
				fw.write(f.getName()+"\t"+ratioNonZeroElements+"\t"+avgScoreNonZeroElements+"\t"+avgScore+"\n");
				br.close();
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
