package fileFiltering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.elasticsearch.watcher.FileWatcher;

import frequency.FrequentWords;

public class DDTFiltering {


	/*
	 * filters word senses in the DDT
	 * NN and NP are kept, and also the word senses whose frequency is above the threshold passed as parameter
	 */
	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Option ddtFileOption = Option.builder()
				.longOpt("ddtFile")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to ddtFile")
				.build();
		Option freqThresholdOption = Option.builder()
				.longOpt("freqThreshold")
				.numberOfArgs(1)
				.required(true)
				.type(Integer.class)
				.desc("freqThreshold")
				.build();
		Option edgeWeightThresholdOption = Option.builder()
				.longOpt("edgeWeightThreshold")
				.numberOfArgs(1)
				.required(true)
				.type(Double.class)
				.desc("edgeWeightThreshold")
				.build();
		Options options = new Options();
		options.addOption(ddtFileOption);
		options.addOption(freqThresholdOption);
		options.addOption(edgeWeightThresholdOption);

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
			formatter.printHelp("java -jar ***.jar ", options, true);
			System.exit(1);
		}
		String ddtFile = cl.getOptionValue("ddtFile");
		Integer freqThreshold = Integer.valueOf(cl.getOptionValue("freqThreshold"));
		Double edgeWeightThreshold = Double.valueOf(cl.getOptionValue("edgeWeightThreshold"));

		//String ddtFile = "/Users/simondif/Documents/workspace/structured-topics/ddt-news-n200-345k-closure.csv";
		//Integer freqThreshold = 100;
		//Double edgeWeightThreshold = 0.0;

		String freqMapFile = "/home/simondif/news100M_stanford_cc_word_count";
		Map<String,Integer> freqMap = null;
		try {
			freqMap = FrequentWords.buildFreqMap(freqMapFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String outputFile = ddtFile.substring(0,ddtFile.length()-".csv".length())+"-NN-f"+freqThreshold+"-e"+edgeWeightThreshold.toString().replace(".", "_")+".csv";
		File fo = new File(outputFile);
		try {
			FileWriter fw = new FileWriter(fo);
			BufferedReader br = new BufferedReader(new FileReader(ddtFile));
			br.readLine();
			String line;
			long nbSimilarWords = 0;
			long nbLines = 0;
			Set<String> wordSenses = new HashSet<String>();
			Map<String,Set<Integer>> wordSensesPerWord = new HashMap<String, Set<Integer>>();
			while ((line = br.readLine())!=null){
				if (line.split("\t").length>2){
					String entry = line.split("\t")[0];
					String cid = line.split("\t")[1];
					if (freq(entry,freqMap)>freqThreshold && entry.split("#")[1].startsWith("NN")){
						updateWordSensesPerWord(wordSensesPerWord, entry+"#"+cid);
						List<String> similarWordsToKeep = new ArrayList<String>();
						List<String> similarWords = Arrays.asList(line.split("\t")[2].split(","));
						for (String similarWord : similarWords){
							if (freq(similarWord, freqMap)>freqThreshold && similarWord.split("#")[1].startsWith("NN") && similarWord.split(":").length>1 && Double.valueOf(similarWord.split(":")[1])>edgeWeightThreshold ) {
								similarWordsToKeep.add(similarWord);
								nbSimilarWords++;
								updateWordSensesPerWord(wordSensesPerWord, similarWord.split(":")[0]);
							}
						}
						if (similarWordsToKeep.size()>0){
							writeLine(fw, entry, cid, similarWordsToKeep);
							wordSenses.add(entry+"#"+cid);
							for (String ws : similarWordsToKeep) wordSenses.add(ws.split(":")[0]);
							nbLines++;
						}
					}
				}
			}
			br.close();
			fw.close();
			Integer nbWordSensesTotal = wordSenses.size();
			Double avWordSensesPerWord = getAvWordSensesPerWord(wordSensesPerWord);
			File foStats = new File (outputFile.substring(0, outputFile.length()-".csv".length())+"Stats.txt");
			FileWriter fwStats = new FileWriter(foStats);
			Integer nbWordsTotal = wordSensesPerWord.size();
			fwStats.write("nbWordsTotal\t"+nbWordsTotal+"\n");
			fwStats.write("nbWordSensesTotal\t"+nbWordSensesTotal+"\n");
			fwStats.write("avWordSensesPerWord\t"+avWordSensesPerWord+"\n");
			fwStats.write("nbLines\t"+nbLines+"\n");
			fwStats.write("nbSimilarWordsTotal\t"+nbSimilarWords+"\n");
			Double averageSimilarWords = Long.valueOf(nbSimilarWords).doubleValue()/Long.valueOf(nbLines).doubleValue();
			fwStats.write("averageSimilarWords\t"+averageSimilarWords+"\n");
			fwStats.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Double getAvWordSensesPerWord(Map<String, Set<Integer>> wordSensesPerWord) {

		Long nbWordSenses = (long)0;
		for (String wordWithTag : wordSensesPerWord.keySet()){
			nbWordSenses += wordSensesPerWord.get(wordWithTag).size();
		}
		double avWordSensesPerWord = nbWordSenses.doubleValue()/wordSensesPerWord.size();
		return avWordSensesPerWord;
	}

	private static void updateWordSensesPerWord(
			Map<String, Set<Integer>> wordSensesPerWord, String wordWithTagAndWordSense) {
		if (wordWithTagAndWordSense.split("#").length!=3) {
			//System.out.println("wordWithTagAndWordSense.split(\"#\").length!=3 for "+wordWithTagAndWordSense);
		} else {			
			String wordWithTag = wordWithTagAndWordSense.substring(0,wordWithTagAndWordSense.length()-"#".length()-wordWithTagAndWordSense.split("#")[2].length());
			try {
				Integer wordSense = Integer.valueOf(wordWithTagAndWordSense.split("#")[2]);
				if (!wordSensesPerWord.containsKey(wordWithTag)) wordSensesPerWord.put(wordWithTag, new HashSet<Integer>());
				wordSensesPerWord.get(wordWithTag).add(wordSense);
			} catch (NumberFormatException e){
				
			}
		}
	}

	private static void writeLine(FileWriter fw, String entry, String cid,
			List<String> similarWordsToKeep) {

		try {
			fw.write(entry+"#"+cid+"\t"+toString(similarWordsToKeep)+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static String toString(List<String> similarWordsToKeep) {

		StringWriter sw = new StringWriter();
		for (String s : similarWordsToKeep){
			sw.append(s+",");
		}
		if (sw.getBuffer().length()>0){
			sw.getBuffer().setLength(sw.getBuffer().length()-1);			
		}
		return sw.toString();
	}

	private static Integer freq(String entry, Map<String, Integer> freqMap) {
		if (freqMap==null) return null;
		String word = entry;
		if (entry.contains(":")) word = entry.split(":")[0];
		if (word.split("#").length==3){
			word = word.split("#")[0]+"#"+word.split("#")[1];
		}
		if (freqMap.containsKey(word)) return freqMap.get(word);
		return 0;

	}

}
