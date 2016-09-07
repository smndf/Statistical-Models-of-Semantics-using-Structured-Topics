package classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import beans.NamedEntity;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import servlets.Visualisation;

public class NLPUtil {


	static Properties props = new Properties();
	static StanfordCoreNLP pipeline;  
	public static void initStanfordPipeline() {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.put("ner.useSUTime","0");
		pipeline = new StanfordCoreNLP(props);
	}

	public static String[] keepOnlyNounsAndNE(String text) { 
		Annotation document = new Annotation(text);
		initStanfordPipeline();
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		Set<String> wordsToReturn = new HashSet<String>();
		for(CoreMap sentence: sentences) {
			System.out.println(sentence.toString());
			// GET NOUNS
			Set<String> nounsSentence = new HashSet<String>();
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				if (token.tag().startsWith("N") /*|| token.tag().startsWith("V")*/){
					nounsSentence.add(token.originalText());
				}
			}
			// GET NAMED ENTITIES AND REMOVE NOUNS THAT ARE PARTS OF NAMED ENTITIES
			Set<NamedEntity> namedEntities = getNamedEntities(sentence);
			Set<String> nounsWithoutNE = new HashSet<String>();
			for (String noun : nounsSentence){
				boolean present = false;
				for (NamedEntity ne : namedEntities){
					if (ne.getText().contains(noun)){
						present = true;
					}
				}
				if (!present){
					nounsWithoutNE.add(noun);
				}
			}
			wordsToReturn.addAll(nounsWithoutNE);
			for (NamedEntity ne : namedEntities){
				wordsToReturn.add(ne.getText());
			}
		}
		String[] output = convertToArray(wordsToReturn);
		for (String s : output){
			System.out.print(s+" ");
		}
		return output;
	}
	
	private static String[] convertToArray(Set<String> set) {
		String[] array = new String[set.size()];
		int i = 0;
		for (String s : set){
			array[i]=s;
			i++;
		}
		return array;
	}

	private static Set<NamedEntity> getNamedEntities(CoreMap sentence) {
		Set<NamedEntity> namedEntities = new HashSet<NamedEntity>();
		List<CoreLabel> lcl = sentence.get(TokensAnnotation.class);
		HashMap<String, HashMap<String, Integer>> entities =
				new HashMap<String, HashMap<String, Integer>>();
		Iterator<CoreLabel> iterator = lcl.iterator();

		if (iterator.hasNext()){

		CoreLabel cl;
		String previousTag = "O";
		String currentTag = "O";
		//StringBuilder currentNE = new StringBuilder();
		NamedEntity currentNE = new NamedEntity();
		while (iterator.hasNext()) {
			cl = iterator.next();
			currentTag = cl.ner();
			//System.out.println(cl.originalText()+" "+cl.ner());
			if (isInteressant(currentTag) /*&& isInteressant(previousTag)*/ && currentTag.equals(previousTag)){
				currentNE.setText(currentNE.getText()+" "+cl.originalText());
				//currentNE.append(" "+cl.originalText());
			} else if (!isInteressant(currentTag) && isInteressant(previousTag)){
				if (currentNE.getText().length()>0){
					namedEntities.add(currentNE);
					currentNE = new NamedEntity();
				}
				currentNE.setTag(currentTag);
			} else if (!isInteressant(currentTag) && !isInteressant(previousTag)){

			} else if (isInteressant(currentTag) && !currentTag.equals(previousTag)){
				if (currentNE.getText().length()>0){
					namedEntities.add(currentNE);
					currentNE = new NamedEntity();
				}
				currentNE.setTag(currentTag);
				currentNE.setText(currentNE.getText()+" "+cl.originalText());
				currentNE.setText(currentNE.getText().trim());
			}
			previousTag = currentTag;
		}	
		}
		return namedEntities;
	}

	private static boolean isInteressant(String tag) {
		List<String> notInterestingTags = Arrays.asList(new String[]{"O","TIME","DATE","NUMBER","MONEY","ORDINAL","PERCENT","DURATION","SET"});
		if (!notInterestingTags.contains(tag)) return true;
		return false;
	}
	
}
