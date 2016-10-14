package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

public class MergeFiles {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option f1Option = Option.builder()
				.longOpt("f1")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to file 1")
				.build();
		Option f2Option = Option.builder()
				.longOpt("f2")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to file 2")
				.build();
		Option outputOption = Option.builder()
				.longOpt("out")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to output file")
				.build();
		options.addOption(f1Option);
		options.addOption(f2Option);
		options.addOption(outputOption);
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
		String f1Path = cl.getOptionValue("f1");
		String f2Path = cl.getOptionValue("f2");
		String outputFilePath = cl.getOptionValue("out");
		/*
		File f1 = new File(f1Path);
		File f2 = new File(f2Path);
		if (!f1.exists() || !f1.isFile() || !f2.exists() || !f2.isFile()){
			System.err.println("Problem with files");
			System.exit(1);
		}
		*/
		try {
			Map<Integer,String> contentPerLineNo1 = readContentFile(f1Path);
			Map<Integer,String> contentPerLineNo2 = readContentFile(f2Path);
			
			FileWriter fw = new FileWriter(outputFilePath);
			for (Integer topicId : contentPerLineNo1.keySet()){
				fw.write(topicId+"\t"+contentPerLineNo1.get(topicId)+"\t"+contentPerLineNo2.get(topicId)+"\n");
				contentPerLineNo2.remove(topicId);
			}
			fw.close();
			if (contentPerLineNo2.size() > 0){
				System.err.println("Some data were not written into file");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Map<Integer, String> readContentFile(String f1Path) throws NumberFormatException, IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(f1Path));
		Map<Integer,String> contentPerLineNo1 = new HashMap<>();
		String line = null;
		while ((line = br1.readLine()) != null){
			String[] lineSplit = line.split("\t");
			Integer lineNo = Integer.valueOf(lineSplit[0]);
			String restOfLine = line.substring(lineSplit[0].length()+"\t".length());
			contentPerLineNo1.put(lineNo, restOfLine);
		}
		br1.close();		
		return contentPerLineNo1;
	}

}
