package hypernyms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;

public class HypernymsSearch {

	public static Dictionary wordnet;

	public void wordNetSearch(String inputFile, Boolean externFiles) throws FileNotFoundException{

		String outputFile = inputFile.substring(0, inputFile.length()-4) + "WithHyp.csv";
		Set<String> cluster = new HashSet<String>();

		BufferedReader clusters = getBufferedReaderForFile(inputFile,externFiles);
		//inputFile = "CWddt-news-n50-485k-closureFiltBef2FiltAf.txt";
		String line = null;
		try {
			File fo = new File(outputFile);
			FileWriter fw = new FileWriter (fo);

			while ( (line = clusters.readLine()) != null){ 
				if (line.split("\t").length>0){
					cluster.clear();
					int noLine = Integer.valueOf(line.split("\t")[0]);
					System.out.println("nouvelle ligne" + noLine);
					String words = line.split("\t")[1];
					for (int i=0;i<words.split(",").length;i++){
						cluster.add(words.split(",")[i].trim());
					}
					Map<Hypernym,Integer> frequentHypernyms = frequentHypernyms(cluster);
					//System.out.println("Best hypernyms : " + frequentHypernyms.toString());
					// inputFile = *ddt-*FiltBef*FiltAf and we need ddt-*FiltBef*.txt
					//String ddtFile = inputFile.substring(inputFile.indexOf("ddt"));
					//DoublesAndString res = averageSemanticSimilarity(ddtFile,frequentHypernyms, externFiles);
					Double[] averageSimilarity = averageSemanticSimilarity(frequentHypernyms, externFiles);
					//String foundWith = res.getS();
					//System.out.println("Semantic similarity found with "+foundWith);							
					//System.out.println("outputFile "+outputFile);							
					writeFile(outputFile, fw, noLine, averageSimilarity, frequentHypernyms, cluster);

				}
			}
			fw.close();
			clusters.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private Double[] averageSemanticSimilarity(Map<Hypernym, Integer> frequentHypernyms, Boolean externFiles) throws IOException {

		Double averageSimilarity[] = new Double[4];
		for (int i = 0; i<4;i++){
			averageSimilarity[i] = 0.0;
		}
		/*
		//System.out.println(ddtFile);
		BufferedReader br = getBufferedReaderForFile(ddtFile,externFiles);

		// try with ddt file
		for (Hypernym hyp1 : frequentHypernyms.keySet()){
			for (Hypernym hyp2 : frequentHypernyms.keySet()){
				if (!hyp1.equals(hyp2)){
					Double semSim = getSemanticSimilarityFromDDT(hyp1,hyp2,br);
					averageSimilarity[0] += semSim;
				}
			}
		}
		br.close();
		 */

		// try with wordnet
		Boolean wordnet = false;
		wordnet = true;
		for (Hypernym hyp1 : frequentHypernyms.keySet()){
			for (Hypernym hyp2 : frequentHypernyms.keySet()){
				if (!hyp1.equals(hyp2)){
					Double semSim[] = getWordNetSimilarity(hyp1,hyp2);
					for (int i = 1; i<4;i++){
						averageSimilarity[i] += semSim[i-1];
					}
				}
			}
		}

		for (int i = 0; i<4;i++){
			averageSimilarity[i] /= (frequentHypernyms.size()*(frequentHypernyms.size()-1));
			//System.out.println("average similarity : "+averageSimilarity[i]);
		}
		String foundWith;
		/*if (wordnet){
			if (averageSimilarity[] != 0.0){
				foundWith = "WordNet";
			} else {
				foundWith = "None";
			}
		} else {
			foundWith = "ddt file";
		}
		 */
		return averageSimilarity;
	}

	private Double getSemanticSimilarityFromDDT(Hypernym hyp1, Hypernym hyp2,
			BufferedReader br) throws IOException {
		int noLine = 0;
		String line;
		Double semSim = 0.0;
		// by using ddt file
		while ( (line = br.readLine()) != null){
			if ((semSim = lookForRelationship(hyp1,hyp2,line)) > 0){
				//System.out.println("relationship found : "+hyp1+","+hyp2);
				break;
			}
			if ((semSim = lookForRelationship(hyp2,hyp1,line)) > 0){
				//System.out.println("relationship found : "+hyp2+","+hyp1);
				break;
			}
		}
		return semSim;
	}

	private Double[] getWordNetSimilarity(Hypernym hyp1, Hypernym hyp2) {
		WS4JConfiguration.getInstance().setMFS(true);
		Double[] res = new Double[3];
		RelatednessCalculator rc;
		rc = new JiangConrath(db);
		res[0] = rc.calcRelatednessOfWords(hyp1.getWord().toLowerCase(), hyp2.getWord().toLowerCase());
		rc = new Resnik(db);
		res[1] = rc.calcRelatednessOfWords(hyp1.getWord().toLowerCase(), hyp2.getWord().toLowerCase());
		rc = new WuPalmer(db);
		res[2] = rc.calcRelatednessOfWords(hyp1.getWord().toLowerCase(), hyp2.getWord().toLowerCase());
		return res;
	}

	private Double lookForRelationship(Hypernym hyp1, Hypernym hyp2, String line) {
		String s = line.split("\t")[0].split("#")[0].toLowerCase();
		if (s.equals(hyp1.getWord().toLowerCase())){
			if (line.split("\t").length>1){
				for (String word : line.split("\t")[1].split(",")){
					if (word.split("#")[0].toLowerCase().equals(hyp2.getWord().toLowerCase())){
						return Double.valueOf(word.split(":")[1]);
					}
				}
			}
		}		
		return 0.0;
	}

	private static ILexicalDatabase db = new NictWordNet();
	/*private static RelatednessCalculator[] rcs = {
		new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db), 
		new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
	};*/

	private BufferedReader getBufferedReaderForFile(String file, Boolean externFiles) throws FileNotFoundException {

		BufferedReader br;
		if (externFiles){
			br= new BufferedReader(new FileReader(file));			
		} else {			
			InputStream is2 = getClass().getResourceAsStream("/"+file);
			br = new BufferedReader(new InputStreamReader(is2));
		}
		return br;
	}

	private void writeFile(String outputFile, FileWriter fw , int noLine, Double[] semSimilarity, Map<Hypernym,Integer> frequentHypernyms,
			Set<String> cluster) throws IOException {

		String line = noLine +"\t" + toString(frequentHypernyms, semSimilarity, cluster.size()) +"\n";
		fw.write (line);

	}

	private String toString(Set<String> set) {
		StringWriter res = new StringWriter();
		for (String s : set){
			if (s.split("#").length>1) s = s.split("#")[0];
			res.write(s + ", ");
		}
		if (res.toString().length()>1) res.getBuffer().setLength( res.toString().length()-2);
		return res.toString();
	}

	private String toString(Map<Hypernym,Integer> frequentHypernyms, Double[] semSimilarity, int clusterSize) {
		StringWriter res = new StringWriter();
		float f;
		double average = 0.0;
		for (Hypernym h : frequentHypernyms.keySet()){
			average += h.getDepth();
		}
		average /= frequentHypernyms.keySet().size();
		res.write(average + "\t");
		for (Hypernym h : frequentHypernyms.keySet()){
			if (h.getWord().split("#").length>1) h.setWord(h.getWord().split("#")[0]);
			res.write(h.getWord() + ",d=" + h.getDepth() + ":" + frequentHypernyms.get(h) + "/" + clusterSize  +/* "=" + (f=h.nb/cluster.size()) +*/ ", ");
		}
		if (res.toString().length()>1) res.getBuffer().setLength( res.toString().length()-2);
		res.write("\t" + semSimilarity[0] + "\t" + semSimilarity[1] + "\t" + semSimilarity[2] + "\t" + semSimilarity[3]);
		return res.toString();
	}

	public static Map<Hypernym,Integer> frequentHypernyms(Set<String> set) throws IOException {
		Map<Hypernym,Integer> hMap = new HashMap<Hypernym,Integer>();
		TreeMap<Integer,Set<Hypernym>> hypRankMap = new TreeMap<Integer,Set<Hypernym>>();

		//String inputIsasFile = "/wikipedia.patterns_lemmatized";

		//InputStream is = getClass().getResourceAsStream(inputIsasFile);
		//BufferedReader isasText = new BufferedReader(new InputStreamReader(is));
		/*String line = null;
		try {

			for (String word : set){

				word = word.split("#")[0].toLowerCase();
				if (word.length() > 0){
					// ADD EACH WORD IN THE SET
					//addToMaps(hMap,hypRankMap,word);

					String letter = word.substring(0,1);
					while ( (line = isasText.readLine()) != null){ 
						if (line.toLowerCase().startsWith(letter) && line.split(" ")[0].toLowerCase().equals(word.toLowerCase())){
							Hypernym hyp = new Hypernym(line.split(" ")[2].split("\t")[0]);
							hyp.addToMaps(hMap,hypRankMap,1);
						}
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	*/	


		try {
			Boolean found = false;

			try {
				//Thread.currentThread().getContextClassLoader().getResourceAsStream("styles/standard.css");
				JWNL.initialize(Thread.currentThread().getContextClassLoader().getResourceAsStream("properties.xml"));
				found = true;
			} catch (JWNLException e) {
				e.printStackTrace();
			}


			wordnet = Dictionary.getInstance();
			IndexWord indexWord = null;
			int i = 0;
			//Set<String> set1 = new HashSet<String>();
			Set<Hypernym> set1 = new HashSet<Hypernym>();
			for (String word : set){
				//System.out.println(i++);
				//if (i==18) System.out.println(word);
				//if (i==19) System.out.println(word);
				POS pos = POS.NOUN;
				if (word.split("#").length>1){					
					pos = POS.NOUN;
					switch (word.split("#")[1]) {
					case "NN" : pos = POS.NOUN;
					break;
					case "NP" : pos = POS.NOUN;
					break;
					case "VP" : pos = POS.VERB;
					break;
					case "JJ" : pos = POS.ADJECTIVE;
					break;
					case "RB" : pos = POS.ADVERB;
					break;
					}

					if (pos == POS.NOUN){	

						word = word.split("#")[0].toLowerCase();

						//addToMaps(hMap,hypRankMap,word);
						//System.out.println("word " + pos);
						indexWord = wordnet.lookupIndexWord(pos, word.toLowerCase());
						getHypernyms(indexWord, hMap, hypRankMap, set1, 1);
					}
				}
			}
			File fo = new File("res.txt");
			FileWriter fw = new FileWriter (fo);
			for (Hypernym h : set1){
				fw.write(h.getWord() + "\t" + h.getDepth() + "\n");
			}
			fw.close();
			wordnet.close();
		} catch (JWNLException e) {
			e.printStackTrace();
		}

		//System.out.println(hypRankMap.size());
		Map<Hypernym,Integer> res = findBestHypernyms(hypRankMap,set.size());

		if (res.isEmpty()){
			res.put(new Hypernym("NO HYPERNYMS",0),0);
		}

		return res;
	}


	private static void getHypernyms(IndexWord indexWord, Map<Hypernym, Integer> hMap,
			TreeMap<Integer, Set<Hypernym>> hypRankMap, Set<Hypernym> set, int ctr) throws JWNLException {
		if (indexWord != null  && ctr > 0) {
			Synset[] synset = indexWord
					.getSenses();
			if (synset != null) {
				for (Synset s : synset) {
					Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
					if (pointerArr != null){
						for (Pointer x : pointerArr) {
							for (Word hypernym : x.getTargetSynset().getWords()) {
								Boolean b = false;
								for (Hypernym h : set){
									if(h.getWord().equals(hypernym.getLemma()) && h.getDepth()==getTreeDepth(getWord(indexWord.getPOS(),hypernym.getLemma()))){
										b = true;
									}
								}
								Hypernym hyp = new Hypernym(hypernym.getLemma(),getTreeDepth(getWord(indexWord.getPOS(),hypernym.getLemma())));
								if (!b){
									set.add(hyp);
								}
								hyp.addToMaps(hMap,hypRankMap,1);
								IndexWord iw = wordnet.lookupIndexWord(indexWord.getPOS(), hypernym.getLemma());
								if (iw != null && iw.getSenseCount() != 0){									
									getHypernyms(iw, hMap, hypRankMap, set,ctr-1);							
								}
								//}
							}
						}
					}
				}
			}
		}		
	}

	public static Set<String> getHypernymsNoDepth(IndexWord indexWord, Dictionary wordnet) throws JWNLException {
		if (indexWord == null) return null;
		else {
			Set<String> hypernyms = new HashSet<String>();
			Synset[] synset = indexWord
					.getSenses();
			if (synset != null) {
				for (Synset s : synset) {
					Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
					if (pointerArr != null){
						for (Pointer x : pointerArr) {
							for (Word hypernym : x.getTargetSynset().getWords()) {
								if (wordnet==null) System.out.println("wordnet null");
								IndexWord iw = wordnet.lookupIndexWord(indexWord.getPOS(), hypernym.getLemma());
								if (iw != null && iw.getSenseCount() != 0){									
									hypernyms.add(iw.getLemma());								}
							}
						}
					}
				}
			}
			return hypernyms;
		}	
	}

	public static Set<String> getHypernymsWithDepth(IndexWord indexWord,
			Dictionary wordnet, int depth) throws JWNLException {
		if (indexWord == null || depth == 0) return null;
		else {
			Set<String> hypernyms = new HashSet<String>();
			Synset[] synset = indexWord.getSenses();
			if (synset != null) {
				for (Synset s : synset) {
					Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
					if (pointerArr != null){
						for (Pointer x : pointerArr) {
							for (Word hypernym : x.getTargetSynset().getWords()) {
								if (wordnet==null) System.out.println("wordnet null");
								IndexWord iw = wordnet.lookupIndexWord(indexWord.getPOS(), hypernym.getLemma());
								if (iw != null && iw.getSenseCount() != 0){									
									hypernyms.add(iw.getLemma());
									Set<String> set = getHypernymsWithDepth(iw,wordnet,depth-1);
									if (set!=null) hypernyms.addAll(set);
								}
							}
						}
					}
				}
			}
			return hypernyms;
		}	
	}

	private static Map<Hypernym,Integer> findBestHypernyms(TreeMap<Integer,Set<Hypernym>> hypRankMap, int clusterSize) {

		Map<Hypernym,Integer> map = new HashMap<Hypernym,Integer>();
		int lastIndex = 0;
		while (map.size()<3 && !hypRankMap.isEmpty()){
			for (Hypernym s : hypRankMap.get(hypRankMap.lastKey())){
				map.put(s,hypRankMap.lastKey());
				//System.out.println("s.getWord() = "+s.getWord());
				//System.out.println("hypRankMap.lastKey() = "+hypRankMap.lastKey());
			}
			lastIndex = hypRankMap.lastKey();
			hypRankMap.remove(hypRankMap.lastKey());
		}
		// keep only 3 hypernyms and remove the longest ones first
		int length = 15;
		Map<Hypernym,Integer> mapcp = new HashMap<Hypernym,Integer>();

		for (Hypernym h : map.keySet()){
			mapcp.put(h, map.get(h));
		}
		Set<Hypernym> set = new HashSet<Hypernym>();
		while (map.size()>3){			
			for (Hypernym h : mapcp.keySet()){
				//System.out.println("h = "+h.getWord());
				if (map.size()>3 && map.get(h)==lastIndex && h.getWord().length() > length){
					map.remove(h);
					set.add(h);
				}
			}
			length--;
			for(Hypernym h : set){
				mapcp.remove(h);
			}
			set.clear();
		}
		return map;
	}

	public static Map<Integer, Map<Integer, Set<String>>> getHypernymsPerNodePerCluster(
			Map<Integer, Set<Integer>> clusters, Map<Integer, String> mapItoS,
			Map<String, Integer> mapStoI) {

		try {
			JWNL.initialize(Thread.currentThread().getContextClassLoader().getResourceAsStream("properties.xml"));
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		wordnet = Dictionary.getInstance();
		Map<Integer,Map<Integer,Set<String>>> wordnetHypernymsPerNodePerCluster = new HashMap<Integer,Map<Integer,Set<String>>>(); 
		for (Integer clusterId : clusters.keySet()){
			System.out.print("cluster "+clusterId+"/"+clusters.size()+"...");
			Set<Integer> cluster = clusters.get(clusterId);
			try {
				wordnetHypernymsPerNodePerCluster.put(clusterId, getHypernymsPerNode(cluster, mapItoS, mapStoI));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("OK");
		}
		return wordnetHypernymsPerNodePerCluster;
	}

	public static Map<Integer, Set<String>> getHypernymsPerNode(
			Set<Integer> cluster, final Map<Integer, String> mapItoS,
			Map<String, Integer> mapStoI) throws InterruptedException {

		final Map<Integer, Set<String>> hypernymsPerNode = new ConcurrentHashMap<Integer, Set<String>>();
		//final int nbThreads = Runtime.getRuntime().availableProcessors();
		//ExecutorService exec = Executors.newFixedThreadPool(nbThreads);
		//try {
			for (final Integer node : cluster){
			//	exec.submit(new Runnable() {
				//	public void run() {
				IndexWord iw;
				try {
					String word = mapItoS.get(node);
					iw = getWord(POS.NOUN, word);
					if (iw == null) {
						//System.out.println("iw null for word "+word);
						continue;
					}
					//System.out.println("iw not null for word "+word);
					hypernymsPerNode.put(node, HypernymsSearch.getHypernyms(iw));
				} catch (JWNLException e) {
					e.printStackTrace();
					return null;
				}
				//	}
				//});
			}
	//	} finally {
	//		exec.shutdown();
	//	}
	//	exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		return hypernymsPerNode;
	}

	// Get the IndexWord object for a String and POS
	public static IndexWord getWord(POS pos, String s) throws JWNLException {
		if (s==null) System.out.println("s == null");
		if (wordnet==null) System.out.println("wordnet == null");

		IndexWord word = wordnet.getIndexWord(pos,s);
		return word;
	}

	public static Set<String> getHypernyms(IndexWord iw){

		Set<String> hypernyms = new HashSet<String>();
		Synset[] synset;
		try {
			synset = iw.getSenses();
			if (synset != null) {
				for (Synset s : synset) {
					Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
					if (pointerArr != null){
						for (Pointer x : pointerArr) {
							for (Word hypernym : x.getTargetSynset().getWords()) {
								IndexWord hyp = wordnet.lookupIndexWord(iw.getPOS(), hypernym.getLemma());
								if (hyp != null && hyp.getSenseCount() != 0){									
									hypernyms.add(hyp.getLemma());
								}
							}
						}
					}
				}
			}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return hypernyms;

	}

	// This method looks for any possible relationship
	public static Relationship getRelationship (IndexWord start, IndexWord end, PointerType type) throws JWNLException {
		// All the start senses
		Synset[] startSenses = start.getSenses();
		// All the end senses
		Synset[] endSenses = end.getSenses();
		// Check all against each other to find a relationship
		for (int i = 0; i < startSenses.length; i++) {
			for (int j = 0; j < endSenses.length; j++) {
				RelationshipList list = RelationshipFinder.getInstance().findRelationships(startSenses[i], endSenses[j], type);
				if (!list.isEmpty())  {
					return (Relationship) list.get(0);
				}
			}
		}
		return null;
	}

	public static int getTreeDepth(IndexWord node) throws JWNLException {
		Set<IndexWord> entityHyponyms = new HashSet<IndexWord>();
		entityHyponyms.add(getWord(POS.NOUN, "physical entity"));
		entityHyponyms.add(getWord(POS.NOUN, "abstract entity"));
		entityHyponyms.add(getWord(POS.NOUN, "thing"));
		int minDepth = Integer.MAX_VALUE;
		int depth;
		for (IndexWord word : entityHyponyms){
			Relationship rel = getRelationship(node, word, PointerType.HYPERNYM);
			if (rel != null && (depth = rel.getDepth()) < minDepth){
				minDepth = depth;
			}
		}
		return minDepth+1; // +1 for entity from one of its hyponyms
	}

}
