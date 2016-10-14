package babelnet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class ToBabelnetTopicFormat {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("input ddt-file: word0#POS#0	word1#POS#0:0.54,word2#POS#3:0.44,word3#POS#1:0.41").isRequired().create("in"));
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
		String outputFile = inputFile.substring(0, inputFile.length()-4)+"Babelnet.txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			FileWriter fw = new FileWriter(outputFile);
			String line;
			while ((line = br.readLine())!=null){
				String id = line.split("\t")[0];
				String words = line.split("\t")[1];
				Integer size = words.split(",").length;
				String newWords = adaptFormat(words);
				fw.write(id+"\t"+size.toString()+"\t"+newWords+"\n");
			}
			
			
			fw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String adaptFormat(String words) {

		StringWriter sw = new StringWriter();
		for (String word : words.split(",")){
			if (word.split("#").length==3) sw.append(word.split("#")[0]+"#"+word.split("#")[2]+", ");
		}
		return sw.toString();
	}

}
