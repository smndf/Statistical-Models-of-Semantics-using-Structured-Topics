package hypernyms;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetCoverage {

	public static Dictionary wordnet;

	public static void main( String[] args ) {
		try {
			try {
				JWNL.initialize(new FileInputStream("src/main/resources/properties.xml"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JWNLException e) {
				e.printStackTrace();
			}
			wordnet = Dictionary.getInstance();
			if (wordnet == null){
				System.out.println("wordnet null");
			}

			String topicsContentFile = "/good_clusters.txt";
			String topicsHypsFile = "/hyps_good_clusters.txt";
			String topicsIsaFile = "/isa_good_clusters.txt";
			InputStream isContent = WordNetCoverage.class.getResourceAsStream(topicsContentFile);
			BufferedReader brContent = new BufferedReader(new InputStreamReader(isContent));
			InputStream isHyps = WordNetCoverage.class.getResourceAsStream(topicsHypsFile);
			BufferedReader brHyps = new BufferedReader(new InputStreamReader(isHyps));
			InputStream isIsa = WordNetCoverage.class.getResourceAsStream(topicsIsaFile);
			BufferedReader brIsa = new BufferedReader(new InputStreamReader(isIsa));

			String topicsContentLine;
			String topicsHypsLine;
			String topicsIsaLine;

			try {
				while((topicsContentLine= brContent.readLine())!=null && topicsContentLine.length()>1){ // word	cid	cluster	isas

					topicsHypsLine = brHyps.readLine();
					topicsIsaLine = brIsa.readLine();
					String clusterString = topicsContentLine.split("\t")[topicsContentLine.split("\t").length-1];
					List<String> hypernyms = new ArrayList<String>();
					for (String s : topicsHypsLine.split("\t")[topicsHypsLine.split("\t").length-1].split(", ")){
						hypernyms.add(s.split(",")[0]);
					}
					List<String> isas = new ArrayList<String>();
					for (String s : topicsIsaLine.split("\t")[topicsIsaLine.split("\t").length-1].split(", ")){
						isas.add(s.split("\\(")[0]);
					}
					WordNetSearchResult res;
					double score = 0.0;
					Set<String> wordsWithoutGoodHypernyms;
					double maxScore = 0.0;
					String bestHypernym = "";
					for (int i = 0; (i<3 && i<hypernyms.size());i++){
						String hypernym = hypernyms.get(i);
						res = wordNetSearch(clusterString, hypernym, 1);
						score = res.getScore();
						wordsWithoutGoodHypernyms = res.getWordsWithoutGoodHypernyms();
						System.out.println("score("+hypernym+")="+res.getScore());
						if (score > maxScore){
							maxScore = score;
							bestHypernym = hypernym;
						}
					}
					System.out.println("Best hypernym = "+bestHypernym);

					maxScore = 0.0;
					String bestIsa = "";
					for (int i = 0; (i<3 && i<isas.size());i++){
						String isa = isas.get(i);
						res = wordNetSearch(clusterString, isa, 1);	
						System.out.println("score("+isa+")="+res.getScore());
						if (score > maxScore){
							maxScore = score;
							bestIsa = isa;
						}
					}
					System.out.println("Best isa = "+bestIsa);

					res = wordNetSearch(clusterString, bestHypernym, 3);
					double recall = res.getScore();
					System.out.println("Recall max for "+bestHypernym+ " = "+recall);
					System.out.println("words that might be added to WordNet with "+bestHypernym+ " as hypernym:");
					System.out.println(toString(res.getWordsWithoutGoodHypernyms()));


					res = wordNetSearch(clusterString, bestIsa, 3);
					recall = res.getScore();
					System.out.println("Recall max for "+bestIsa+ " = "+recall);
					System.out.println("words that might be added to WordNet with "+bestIsa+ " as hypernym:");
					System.out.println(toString(res.getWordsWithoutGoodHypernyms()));
					System.out.println();
				}
				wordnet.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}

	private static String toString(Set<String> set) {
		StringWriter res = new StringWriter();
		for (String s : set){
			res.write(s + ", ");
		}
		if (res.toString().length()>1) res.getBuffer().setLength( res.toString().length()-2);
		return res.toString();
	}

	public static WordNetSearchResult wordNetSearch(String clusterString, String hypernym, int depth) throws FileNotFoundException, JWNLException{


		Set<String> cluster = new HashSet<String>();
		for (String s : clusterString.split(", ")){
			cluster.add(s);	
		}
		int clusterSize = cluster.size();
		int hypernymsAndGoodHypernym = 0;
		int hypernymsButNoGoodHypernym = 0;
		//System.out.print("No hypernyms for ");
		Set<String> wordsWithoutGoodHypernyms = new HashSet<String>();
		for (String word : cluster){
			POS pos = POS.NOUN;

			IndexWord indexWord = wordnet.lookupIndexWord(pos, word.toLowerCase());
			Set<String> setString = getHypernyms(indexWord, depth);

			if (setString.size() != 0){
				if (setString.contains(hypernym)){
					//System.out.println("fish as hypernym of "+word);
					hypernymsAndGoodHypernym++;

				} else {
					boolean goodHypernym = false;
					for (String s : setString){
						if (s.contains(hypernym)){
							goodHypernym = true;
						}
					}
					if (goodHypernym){
						hypernymsAndGoodHypernym++;
					} else {
						hypernymsButNoGoodHypernym++;
						wordsWithoutGoodHypernyms.add(word);
					}
					//System.out.println("fish NOT hypernym of "+word);
				}
			}
		}
		/*
	 	System.out.println("HYPERNYM = "+hypernym);
		System.out.println(clusterSize + " words in cluster");
		System.out.println(nbHypernyms + " words with no hypernym");
		System.out.println(hypernymsButNoGoodHypernym + " words with hypernyms but no "+hypernym);
		System.out.println(hypernymsAndGoodHypernym + " words with hypernyms and "+hypernym);
		 */
		int withHypernyms = hypernymsAndGoodHypernym + hypernymsButNoGoodHypernym;
		double ratioWithGoodHypernym = 100 * (double) hypernymsAndGoodHypernym / (double) withHypernyms;
		//System.out.println(withHypernyms + " words (/" + clusterSize + ") with hypernyms and among them " + ratioWithGoodHypernym + "% have "+hypernym+" as hypernym." );
		
		WordNetSearchResult res = new WordNetSearchResult(ratioWithGoodHypernym,wordsWithoutGoodHypernyms);
		
		return res;

	}

	private static  Set<String> getHypernyms(IndexWord indexWord, int ctr) throws JWNLException {
		Set<String> resultSet = new HashSet<String>();
		if (indexWord != null  && ctr > 0) {
			//System.out.println(indexWord.getLemma());
			Synset[] synset = indexWord
					.getSenses();
			if (synset != null) {
				for (Synset s : synset) {
					Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
					if (pointerArr != null){
						for (Pointer x : pointerArr) {
							for (Word hypernym : x.getTargetSynset().getWords()) {
								//if (!set1.contains(hypernym.getLemma())){
								IndexWord iw = wordnet.lookupIndexWord(indexWord.getPOS(), hypernym.getLemma());
								resultSet.add(iw.getLemma());
								//System.out.print(iw.getLemma()+", ");
								if (iw != null && iw.getSenseCount() != 0){									
									resultSet.addAll(getHypernyms(iw, ctr-1));							
								}
							}
						}
					}
				}
			}
		}		
		return resultSet;
	}

}

class WordNetSearchResult {
	private double score;
	private Set<String> wordsWithoutGoodHypernyms;
	
	public WordNetSearchResult(double score,
			Set<String> wordsWithoutGoodHypernyms) {
		super();
		this.score = score;
		this.wordsWithoutGoodHypernyms = wordsWithoutGoodHypernyms;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public Set<String> getWordsWithoutGoodHypernyms() {
		return wordsWithoutGoodHypernyms;
	}
	public void setWordsWithoutGoodHypernyms(Set<String> wordsWithoutGoodHypernyms) {
		this.wordsWithoutGoodHypernyms = wordsWithoutGoodHypernyms;
	}
	
}
