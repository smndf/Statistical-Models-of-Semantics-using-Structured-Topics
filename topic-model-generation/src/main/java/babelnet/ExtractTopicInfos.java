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

public class ExtractTopicInfos {

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
		String outputFile = "infoTopicsDomainIndex.csv";
		try {
			FileWriter fw = new FileWriter(outputFile);
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			while ((line = br.readLine())!= null){
				String domain = line.split("\t")[0];
				String size = line.split("\t")[1];
				fw.write(domain+"\t"+size+"\n");
			}
			br.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
