package hypernyms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import classification.PipelineEvaluation;


/*
 * In this class, we want to enrich a topic by adding to it the synonyms and the most relevant hypernyms
 * e.g. for a topic containing fish species, we would add words like fish, species, shark, maybe water, see, lake, river
 * this is meant to improve search results
 * NOTE enriching a topic can harm for other further uses e.g. suggesting new hyponym-hypernym relationships to WordNet
 */
public class EnrichTopic {

	public static Dictionary wordnet;

	public static void main(String[] args){
		try {
			JWNL.initialize(Thread.currentThread().getContextClassLoader().getResourceAsStream("properties.xml"));
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		wordnet = Dictionary.getInstance();
		addHypernyms();
	}
	
	public static void enrichWithSynonyms(){
		String inputFile = "/good_clusters.txt";
		String outputFile = "good_clustersEnrichedSynonyms.txt";

		try {
			Map<Integer,Set<String>> topics = readTopics(inputFile);
			Map<Integer,Set<String>> enrichedTopics = addSynonyms(topics);
			writeTopicsWithSynonyms(enrichedTopics,outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	private static void writeTopicsWithSynonyms(
			Map<Integer, Set<String>> enrichedTopics, String outputFile) throws IOException {
		File fo = new File(outputFile);
		FileWriter fw = new FileWriter(fo);
		for (Integer noTopic = 0 ; noTopic<enrichedTopics.size() ; noTopic++){
			fw.write((noTopic+1)+"\t"+toString(enrichedTopics.get(noTopic))+"\n");
		}
		fw.close();
	}

	private static void testSynonyms(String word) throws JWNLException {

		IndexWord iw = wordnet.lookupIndexWord(POS.NOUN, word);
		if (iw==null) System.out.println("indexword null for "+word);
		else {
			Synset[] ss = iw.getSenses();
			for (Synset s : ss){
				for (Word w : s.getWords()){
					System.out.println(w.getLemma());
				}
			}
		}
	}

	public static void addHypernyms(){	

		String inputFile = "/good_clustersEnrichedSynonyms.txt";
		String outputFileStem = "good_clustersEnrichedSynonymsAnd";
		List<Integer> nbHypernymsToBeAdded = new ArrayList<Integer>();
		nbHypernymsToBeAdded.add(5);
		nbHypernymsToBeAdded.add(10);
		nbHypernymsToBeAdded.add(15);
		nbHypernymsToBeAdded.add(20);
		nbHypernymsToBeAdded.add(25);
		nbHypernymsToBeAdded.add(30);
		nbHypernymsToBeAdded.add(35);
		nbHypernymsToBeAdded.add(40);
		nbHypernymsToBeAdded.add(45);
		nbHypernymsToBeAdded.add(50);

		try {
			Map<Integer,Set<String>> topics = readTopics(inputFile);
			Map<Integer,Map<Integer,Set<String>>> enrichedTopics = new HashMap<Integer,Map<Integer,Set<String>>>();
			int depth = 2;
			for (Integer i = 0 ; i<topics.size() ; i++){
				enrichedTopics.put(i, enrich(topics.get(i),nbHypernymsToBeAdded,depth));
			}
			writeTopics(enrichedTopics,outputFileStem,nbHypernymsToBeAdded);
		} catch (IOException | JWNLException e) {
			e.printStackTrace();
		}
	}

	private static Map<Integer,Set<String>> addSynonyms(Map<Integer,Set<String>> topics){
		Map<Integer,Set<String>> newTopics = new HashMap<Integer,Set<String>>();
		for (Integer i : topics.keySet()){
			Set<String> newTopic = new HashSet<String>();
			for (String word : topics.get(i)){
				try {
					newTopic.add(word);
					newTopic.addAll(getSynonyms(word));
				} catch (JWNLException e) {
					e.printStackTrace();
				}
			}
			System.out.println("synonyms("+i+") = "+toString(newTopic));
			newTopics.put(i,newTopic);
		}
		return newTopics;
	}

	private static Collection<String> getSynonyms(String word) throws JWNLException {
		Set<String> synonyms = new HashSet<String>();
		IndexWord iw = wordnet.lookupIndexWord(POS.NOUN, word);
		if (iw==null) ;//System.out.println("indexword null for "+word);
		else {
			Synset[] ss = iw.getSenses();
			for (Synset s : ss){
				for (Word w : s.getWords()){
					synonyms.add(w.getLemma());
				}
			}
		}
		return synonyms;
	}

	private static void writeTopics(Map<Integer, Map<Integer, Set<String>>> enrichedTopics,
			String outputFile, List<Integer> nbHypernymsToBeAdded) throws IOException {
		for (Integer i : nbHypernymsToBeAdded){
			File fo = new File(outputFile+i+"Hyps.txt");
			FileWriter fw = new FileWriter(fo);
			for (Integer noTopic = 0 ; noTopic<enrichedTopics.size() ; noTopic++){
				fw.write((noTopic+1)+"\t"+toString(enrichedTopics.get(noTopic).get(i))+"\n");
			}
			fw.close();
		}
	}

	private static Map<Integer, Set<String>> readTopics(String inputFile) throws IOException {
		Map<Integer, Set<String>> topics = new HashMap<Integer, Set<String>>();
		InputStream is = PipelineEvaluation.class.getResourceAsStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while((line = br.readLine()) != null){
			Set<String> topic = new HashSet<String>();
			for (String s : line.split("\t")[1].split(", ")){
				topic.add(s);				
			}
			topics.put(topics.size(),topic);
		}
		return topics;
	}

	public static Map<Integer,Set<String>> enrich(Set<String> topic, List<Integer> nbHypernymsToBeAdded, int depth) throws JWNLException{
		Map<Integer,Set<String>> enrichedTopic = new HashMap<Integer,Set<String>>();
		Map<String,Integer> freqHypernyms = new HashMap<String, Integer>();
		for (String s : topic){
			String word = s;
			IndexWord iw = wordnet.lookupIndexWord(POS.NOUN, word);
			//if (iw==null) System.out.println("indexword null for "+word);	
			//else System.out.println("indexword NOT null for "+word);
			Set<String> hypernyms = HypernymsSearch.getHypernymsWithDepth(iw,wordnet,depth);//HypernymsSearch.getHypernymsNoDepth(iw,wordnet);
			if (hypernyms==null) ;//System.out.println("set hypernyms null");
			else {				
				for (String hypernym : hypernyms){
					if (freqHypernyms.containsKey(hypernym)){
						freqHypernyms.put(hypernym, freqHypernyms.get(hypernym)+1);
					} else {
						freqHypernyms.put(hypernym, 1);
					}
				}
			}
		}
		for (Integer i : nbHypernymsToBeAdded){
			Set<String> hypernymsToBeAdded = getNBestHypernyms(freqHypernyms,i);
			System.out.println(i+" added hypernyms: "+toString(hypernymsToBeAdded));
			enrichedTopic.put(i, hypernymsToBeAdded);
			enrichedTopic.get(i).addAll(topic);
		}		
		return enrichedTopic;
	}

	private static Set<String> getNBestHypernyms(
			Map<String, Integer> freqHypernyms, Integer n) {
		Set<String> nBestHypernyms = new HashSet<String>();
		TreeMap<Integer,Set<String>> mapItoS = new TreeMap<Integer,Set<String>>();
		for (String s : freqHypernyms.keySet()){
			if (!mapItoS.containsKey(freqHypernyms.get(s))){
				Set<String> set = new HashSet<String>();
				set.add(s);
				mapItoS.put(freqHypernyms.get(s), set);
			} else {
				mapItoS.get(freqHypernyms.get(s)).add(s);
			}
		}
		while (nBestHypernyms.size()<n && mapItoS.keySet().size()>0){
			Integer i = mapItoS.lastKey();
			Set<String> set = mapItoS.get(i);
			mapItoS.remove(i);
			for (String s : set){
				if (nBestHypernyms.size()<n){
					nBestHypernyms.add(s);
				}
			}
		}
		return nBestHypernyms;
	}

	private static String toString(Set<String> set) {
		StringWriter res = new StringWriter();
		for (String s : set){
			res.write(s + ", ");
		}
		if (res.toString().length()>1) res.getBuffer().setLength( res.toString().length()-2);
		return res.toString();
	}

}
