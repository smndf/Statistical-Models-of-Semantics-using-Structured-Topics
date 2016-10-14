package babelnet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class DomainIndexToFoundSenses {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("dir").hasArg()
				.withDescription("domain_index.csv file to convert.")
				.isRequired()
				.create("input"));
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
			formatter.printHelp("java -jar jsonSimplifierReplyGo.jar", options, true);
			System.exit(1);
		}
		String fileName = cl.getOptionValue("input");
		String outputFile = "foundSensesFrom"+fileName.split("/")[fileName.split("/").length-1];
		try {
			FileWriter fw = new FileWriter(outputFile);
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			while ((line = br.readLine())!= null){
				String domain = line.split("\t")[0];
				String words = line.split("\t")[2];
				for (String wordWithScore : words.split(", ")){
					if (wordWithScore.split(":").length!=2) continue;
					String word = wordWithScore.split(":")[0];
					String score = wordWithScore.split(":")[1];
					try {
						Double scoreDouble = Double.valueOf(score);						
					} catch (NumberFormatException e){
						System.err.println("Didn't manage to parse "+score+" as a double");
						continue;
					}
					fw.write(word+"\t"+score+"\t"+domain+"\t"+"abc"+"\t"+"abc"+"\n");
				}
			}
			br.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
