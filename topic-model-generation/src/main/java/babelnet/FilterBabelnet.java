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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FilterBabelnet {

	/*
	 * filters domain_index.csv file
	 */
	
	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option fileOption = Option.builder()
				.longOpt("file")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to Babelnet domain_index.csv file")
				.build();
		Option percentsToRemoveOption = Option.builder()
				.longOpt("percents")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("percents of total nb words to remove from each babelent topic (remove least significatn words)")
				.build();
		options.addOption(fileOption);
		options.addOption(percentsToRemoveOption);
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
		String fileName = cl.getOptionValue("file");
		Integer percentsToRemove = Integer.valueOf(cl.getOptionValue("percents"));
		if (percentsToRemove<0 || percentsToRemove>100) {
			System.err.println("Invalid percentage, must be between 0 and 100");
			System.exit(1);
		}
		Integer percentsToKeep = 100-percentsToRemove;
		String outputFileName = fileName.substring(0,fileName.length()-4)+"-p"+percentsToKeep+".csv";
		try {
			FileWriter fw = new FileWriter(outputFileName);
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			fw.write(line+"\n");
			while ((line=br.readLine())!=null){
				String topicTitle = line.split("\t")[0];
				Integer topicSize = Integer.valueOf(line.split("\t")[1]);
				String topicWords = line.split("\t")[2];
				topicWords = filterTopicWords(topicWords);
				topicSize = topicWords.split(", ").length;
				Integer nbWordsToKeep = topicSize * percentsToKeep / 100;
				System.out.println("topicSize = "+topicSize);
				System.out.println("nbWordsToKeep = "+nbWordsToKeep);
				String topicWordsKept = removeSomeWords(topicWords, nbWordsToKeep);
				String newLine = buildLine(topicTitle, topicWordsKept);
				fw.write(newLine+"\n");
				fw.flush();
			}
			fw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static String filterTopicWords(String topicWords) {

		StringWriter sw = new StringWriter();
		for (String wordWithScore : topicWords.split(", ")){
			String word = wordWithScore.split(":")[0];
			if (word.length()<3) continue;
			if (word.contains(" ")) continue;
			sw.append(wordWithScore+", ");
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-2);
		return sw.toString();
	}
	
	private static String removeSomeWords(String topicWords,
			Integer nbWordsToKeep) {
		
		StringWriter sw = new StringWriter();
		int writtenWords = 0;
		for (String wordWithScore : topicWords.split(", ")){			
			sw.append(wordWithScore+", ");
			writtenWords++;
			if (writtenWords==nbWordsToKeep) break;
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-2);
		return sw.toString();
	}

	private static String buildLine(String topicTitle, String topicWordsKept) {
		StringWriter sw = new StringWriter();
		sw.append(topicTitle);
		sw.append("\t");
		sw.append(Integer.valueOf(topicWordsKept.split(", ").length).toString());
		sw.append("\t");
		sw.append(topicWordsKept);
		return sw.toString();
	}
}
