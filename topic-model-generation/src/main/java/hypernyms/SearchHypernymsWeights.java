package hypernyms;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import util.MapUtil;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class SearchHypernymsWeights {

	public static Integer MAX_DEPTH = 1;
	public static Dictionary wordnet;

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option clustersFileOption = Option.builder()
				.longOpt("clusters")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to clustersFile")
				.build();
		Option depthOption = Option.builder()
				.longOpt("depth")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("max depth of hypernyms")
				.build();
		options.addOption(depthOption);
		options.addOption(clustersFileOption);
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
		String clustersFileName = cl.getOptionValue("clusters");
		MAX_DEPTH = Integer.valueOf(cl.getOptionValue("depth"));
		Map<Integer, Set<String>> clusters = null;
		try {
			clusters = readClusters(clustersFileName);
		} catch (NumberFormatException | IOException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			JWNL.initialize(new FileInputStream("/home/simondif/structured-topics/properties.xml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		wordnet = Dictionary.getInstance();
		String outputFile = clustersFileName.substring(0,clustersFileName.length()-4) + "WordNetHypernyms-"+MAX_DEPTH+".txt";
		FileWriter fw;
		try {
			fw = new FileWriter(outputFile);

			for (Integer clusterId : clusters.keySet()){	
				Set<String> cluster = clusters.get(clusterId);
				Map<String,Double> hypernymsMap = new HashMap<String,Double>();
				//System.out.println("cluster "+clusterId+" : "+cluster.size()+" words");
				for (String word : cluster) {
					IndexWord iw = null;
					try {
						iw = wordnet.lookupIndexWord(POS.NOUN, word);
					} catch (JWNLException e) {
						e.printStackTrace();
					}
					if (iw!=null) {				
						try {
							searchHypernyms(iw, hypernymsMap, 1);
							System.out.println("hyps("+iw.getLemma()+") : size(map) = "+hypernymsMap.size());
						} catch (JWNLException e) {
							e.printStackTrace();
						}
					} else {
						//System.out.println("indexWord == null for word "+word);
					}
				}
				hypernymsMap = MapUtil.sortMapByValue(hypernymsMap);
				writeTop20Hypernyms(fw, hypernymsMap, clusterId);
			}
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void writeTop20Hypernyms(FileWriter fw,
			Map<String, Double> hypernymsMap, Integer clusterId) throws IOException {

		int nbHypernyms = 1;
		StringWriter sw = new StringWriter();
		sw.append(clusterId+"\t");
		if (hypernymsMap == null) sw.append("NO HYPERNYMS");
		else if (hypernymsMap.size()==0) sw.append("NO HYPERNYMS");
		else {		
			for (Map.Entry<String, Double> entry : hypernymsMap.entrySet()){
				sw.append(entry.getKey());//+"#"+entry.getValue());
				if (nbHypernyms++<3) sw.append(", ");
				else break;
			}
		}
		sw.append("\n");
		fw.write(sw.toString());
		
	}

	private static Map<Integer, Set<String>> readClusters(
			String clustersFileName) throws NumberFormatException, IOException {

		Map<Integer,Set<String>> clusters = new HashMap<Integer,Set<String>>();
		BufferedReader br = new BufferedReader(new FileReader(clustersFileName));
		String line = "";
		while ((line = br.readLine())!=null){
			Integer clusterId = Integer.valueOf(line.split("\t")[0]);
			Set<String> cluster = new HashSet<String>();
			for (String wordWithTag : line.split("\t")[1].split(",")){
				cluster.add(wordWithTag.split("#")[0]);
			}
			clusters.put(clusterId, cluster);
			//System.out.println("cluster "+clusterId+" : "+cluster.size()+" words");
		}
		br.close();
		return clusters;
	}

	private static void searchHypernyms(IndexWord iw,
			Map<String, Double> hypernymsMap, int depth) throws JWNLException {

		if (iw != null) {
			//System.out.println(indexWord.getLemma());
			Synset[] synset = iw
					.getSenses();
			if (synset != null) {
				for (Synset s : synset) {
					Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
					if (pointerArr != null){
						for (Pointer x : pointerArr) {
							for (Word hypernym : x.getTargetSynset().getWords()) {
								//if (!set1.contains(hypernym.getLemma())){
								IndexWord iwHyp = null;
								try {
									iwHyp = wordnet.lookupIndexWord(POS.NOUN, hypernym.getLemma());
								} catch (JWNLException e) {
									e.printStackTrace();
								}
								if (iwHyp!=null){
									String hyp = iwHyp.getLemma();
									updateHypernymsMap(hypernymsMap, hyp, depth);
									if (depth<MAX_DEPTH) {
										searchHypernyms(iwHyp, hypernymsMap, depth+1);
									}
								}
							}
						}
					}
				}
			}
		}		
	}

	private static void updateHypernymsMap(Map<String, Double> hypernymsMap, String hyp, Integer depth) {

		Double score = Integer.valueOf(1).doubleValue()/Math.pow(2,depth.doubleValue());
		if (!hypernymsMap.containsKey(hyp)) hypernymsMap.put(hyp, score);
		else hypernymsMap.put(hyp, hypernymsMap.get(hyp)+score);
	}

}
