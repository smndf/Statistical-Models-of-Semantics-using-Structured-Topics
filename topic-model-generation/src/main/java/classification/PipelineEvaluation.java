package classification;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;

import util.MapUtil;

public class PipelineEvaluation {

	public static StringWriter errors;
	
	public static void main(String[] args) {
		String[] topicsFiles = {"good_clustersProcessed.txt"};
		for (String topicsFile : topicsFiles){
			run(topicsFile);
		}
	}
	
	public static void run(String topicsFile) {
		errors = new StringWriter();
		String[] titlesFiles = {"/list_of_drugs.txt","/list_of_fishes.txt","/list_of_theater_characters.txt","/list_of_explorers.txt","/list_of_cyclists.txt","/list_of_wine_grapes.txt","/list_of_dogs.txt"};
		int nbTopics = titlesFiles.length;
		Map<Integer,Integer[][]> results = new HashMap<Integer,Integer[][]>();
		String inputFile = "/"+topicsFile;
		ElasticSearch es = new ElasticSearch();
		System.out.println("loading topics from "+inputFile+" in index topics\n");
		Map<String,Set<String>> map = es.loadClusters2(inputFile, "topics");
		System.out.println("topics loaded\n");
		Map<Integer,Set<String>> articles = new HashMap<Integer,Set<String>>(); 
		for (Integer i = 0; i<nbTopics; i++){
			InputStream is = PipelineEvaluation.class.getResourceAsStream(titlesFiles[i]);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Set<String> articlesForTopicI = new HashSet<String>();
			String line = "";
			try {
				while((line = br.readLine()) != null){
					articlesForTopicI.add(line.trim().substring("/wiki/".length()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			articles.put(i,articlesForTopicI);
		}
		File outputFile = new File("resultsTestEvalNoStopWordsRemoved"+topicsFile);
		try {
			FileWriter fw = new FileWriter(outputFile);
			Double[] scores = new Double[nbTopics];
			for (Integer i = 0; i<nbTopics; i++){
				results.put(i, runEval(articles.get(i), titlesFiles[i], nbTopics, es, map, fw, i));
				scores[i] = score(results.get(i),i,articles.get(i).size());
			}
			for (int i = 0 ; i<titlesFiles.length ; i++){
				System.out.println(titlesFiles[i]+"   "+articles.get(i).size()+" articles");
				System.out.println("S = "+scores[i]);
				display(results.get(i));
				System.out.println();
			}
			System.out.println("average S = "+average(scores)+" and standard deviation S = "+standardDeviation(scores,average(scores)));
			System.out.println(errors.toString());
			fw.write("average S = "+average(scores)+" and standard deviation S = "+standardDeviation(scores,average(scores)));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Double standardDeviation(Double[] tab, Double av) {
		Double var = 0.0;
		for (int i = 0; i<tab.length ; i++) var += (av-tab[i])*(av-tab[i]);
		return Math.sqrt(var/tab.length);
	}

	private static Double average(Double[] tab) {
		Double av = 0.0;
		for (int i = 0; i<tab.length ; i++) av += tab[i];
		return av/tab.length;
	}

	public static Integer[][] runEval(Set<String> articles, String fileName, int nbTopics, ElasticSearch es, Map<String, Set<String>> map, FileWriter fw, int noTopic) throws IOException{

		String[] titlesFiles = {"/list_of_drugs.txt","/list_of_fishes.txt","/list_of_theater_characters.txt","/list_of_explorers.txt","/list_of_cyclists.txt","/list_of_wine_grapes.txt","/list_of_dogs.txt"};
		Map<String,Integer> fileNumbers = new HashMap<String,Integer>();
		for (int fileNumber = 0; fileNumber<titlesFiles.length ; fileNumber++){
			fileNumbers.put(titlesFiles[fileNumber], fileNumber);
		}
		DBPediaCrawler dbpc = new DBPediaCrawler();
		Node node = nodeBuilder().node();
		Client client = node.client();
		String text;

		Integer[][] results = new Integer[nbTopics][nbTopics];
		String line = "";
		int nbGood = 0;
		int nbBad = 0;
		int emptyTexts = 0;
		int nbTexts = 0;
		for (int i = 0; i<results.length; i++){
			for (int j = 0; j<results[0].length; j++){
				results[i][j] = 0;
			}
		}
		Set<String> toBeRemoved = new HashSet<String>();
		
		Map<String,Integer> mapFragments = new HashMap<String,Integer>();		
		Integer fileNumber = fileNumbers.get(fileName);
		
		Double avTextLength = 0.;
		for (String title : articles){
			if (seemsRelevant(title)){
				text = dbpc.getArticleText(title);
				System.out.println("text:");
				System.out.println(text);
				if (text.isEmpty()){
					emptyTexts++;
					toBeRemoved.add(title);
				} else {
					//System.out.println("text = "+text);
					try{
						avTextLength += text.length();
					SearchHits hits = es.search(text, "topics", client);
					nbTexts++;
					int noHit = 0;
					for (SearchHit hit : hits.getHits()) {
						System.out.println("noHit = "+noHit+"\tInteger.valueOf(hit.getId()) = "+Integer.valueOf(hit.getId()));
						results[noHit][Integer.valueOf(hit.getId())-1]++;
						if (Integer.valueOf(hit.getId())==1){
							nbGood++;
						} else {
							nbBad++;
						}
						/*
						 * process highlights to analyze errors
						 */
						if (Integer.valueOf(hit.getId())!=fileNumber){ // only if error
							String highlights = toString(hit.getHighlightFields());
							List<String> fragmentsList = new ArrayList<String>();
							if (highlights.contains("<em>")){
								for (String s : highlights.split("<em>")){
									if (s.contains("</em>")){
										s = s.split("</em>")[0];
										fragmentsList.add(s);
									}
								}
							}
							for (String fragment : fragmentsList){
								if (mapFragments.containsKey(fragment)){
									mapFragments.put(fragment, mapFragments.get(fragment)+1);
								} else {
									mapFragments.put(fragment, 1);
								}
							}
						}
						
						//System.out.println("topic "+ hit.getId() +" score:" + hit.getScore() + " " + toString(map.get(hit.getId())));
						noHit++;
					}
					System.out.println("article "+nbTexts+"/"+articles.size());
					} catch ( org.elasticsearch.action.search.SearchPhaseExecutionException e){
						e.printStackTrace();
					}
				}
				System.out.println(emptyTexts + " empty texts and "+nbTexts+ " texts with content");
				display(results);
			}
		}
		avTextLength /= nbTexts;
		FileWriter statsFW = new FileWriter("stats"+fileName.substring(1));
		statsFW.write(fileName+"\t"+avTextLength+"\t"+nbTexts);
		statsFW.close();
		mapFragments = MapUtil.sortMapByValue(mapFragments);
		errors.append("\nclassification errors for "+fileName+" come from:\n");
		for (Entry<String, Integer> entry : mapFragments.entrySet()){
			errors.append(entry.getKey()+" : "+entry.getValue()+"\n");
		}
		
		
		for (String s : toBeRemoved){
			articles.remove(s);
		}

		fw.write(fileName+"   "+articles.size()+" articles\n");
		fw.write("S = "+score(results, noTopic, articles.size())+"\n");
		fw.write(display(results)+"\n");
		fw.flush();
		System.out.println();
		node.close();
		return results;


	}
	
	private static Double log2(Integer x){
		return Math.log(x)/Math.log(2);
	}

	private static Double score(Integer[][] results, int noTopic, int nbArticles) {
		int nbTopics = results.length;
		Double score = results[0][noTopic].doubleValue();
		for (int i=1 ; i<nbTopics ; i++){
			score += results[i][noTopic]/log2(1+i);
		}
		return score/nbArticles;
	}

	private static String display(Integer[][] matrix) {
		StringWriter sw = new StringWriter();
		sw.append("\n");
		for (int i=0;i<matrix.length;i++){
			int nb = i+1;
			sw.append(nb+" & ");
			for (int j=0;j<matrix[0].length;j++){
				sw.append(String.valueOf(matrix[i][j]) + " & ");
			}
			sw.getBuffer().setLength(sw.getBuffer().length()-2);
			sw.append(" \\\\ \\hline");
			sw.append("\n");
		}
		sw.append("\n");
		System.out.println(sw.toString());
		return sw.toString();
	}

	private static boolean seemsRelevant(String title) {
		String lower = title.toLowerCase();
		if (lower.contains("list")) return false;
		if (lower.contains("category")) return false;
		return true;
	}

	private static String toString(Set<String> set) {
		StringWriter res = new StringWriter();
		for (String s : set){
			res.write(s + ", ");
		}
		if (res.toString().length()>1) res.getBuffer().setLength( res.toString().length()-2);
		return res.toString();
	}
	
	private static String toString(Map<String, HighlightField> highlightFields) {
		StringWriter sw = new StringWriter();
		for (String s : highlightFields.keySet()){
			sw.append(s+ " "+ highlightFields.get(s).toString()+ "\n" );
		}
		//System.out.println(sw.toString());
		return sw.toString();
	}

}
