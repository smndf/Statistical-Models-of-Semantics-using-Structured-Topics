package fileFiltering;

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

public class DaviesBouldinResultsReader {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option dirOption = Option.builder()
				.longOpt("dir")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to dir with DaviesBouldin results")
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
		Map<String, String> scoresPerFile = new HashMap<String, String>();
		for (File f : dir.listFiles()){
			try {
				if (f.isFile() && f.getName().endsWith("DaviesBouldinIndex.txt")) {
					BufferedReader br = new BufferedReader(new FileReader(f));
					scoresPerFile.put(f.getName(), br.readLine().split("\t")[1]);
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileWriter fw = new FileWriter("daviesBouldinResults.txt");
			fw.write("");
			for (String fileName : scoresPerFile.keySet()){
				fw.write(fileName+"\t"+scoresPerFile.get(fileName)+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
