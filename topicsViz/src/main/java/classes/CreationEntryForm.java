package classes;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;

import servlets.InputServlet;
import servlets.SetupServlet;

public class CreationEntryForm {
	public static final String CHAMP_TEXT       = "unstructuredtext";
	/* 
	 * possible indices names: 7clusters structured-topics mwe-topics
	 */
	public static String esIndex = "swe-topics";
	
	public static void updateIndexToQuery(String indexName){
		System.out.println("CreationEntryForm.esIndex = " + indexName);
		CreationEntryForm.esIndex = indexName;
	}
	
	private String result;
	private Map<String, String> errors         = new HashMap<String, String>();
	public Map<String, String> getErrors() {
		return errors;
	}

	public String getResult() {
		return result;
	}

	public Results createEntry(String queryContent, int nbResultsMax) {

		if (queryContent==null || queryContent.isEmpty()) return null;
		
		Results results = new Results();

		try {
			validationText( queryContent );
		} catch ( Exception e ) {
			setError( CHAMP_TEXT, e.getMessage() );
		}
		results.setText( queryContent );

		SearchHits hits = InputServlet.elasticSearch.search(esIndex, queryContent.toLowerCase(), nbResultsMax);
		for (SearchHit hit : hits.getHits()) {
			String topicId = hit.getId();
			Float score = hit.getScore();
			String idString = hit.fields().get("id").value();
			Integer id = Integer.valueOf(idString);
			String content = hit.fields().get("content").value();
			content = content.replaceAll(",", ", ");
			String hypernyms = hit.fields().get("hypernyms").value();
			String isas = hit.fields().get("isas").value();
			Topic topic = new Topic(id, content, hypernyms, isas);
			results.getEntries().add(new Entry(topic, process(toString(hit.highlightFields())), score));
		}
		return results;
		/*
		String[] topics = new String[3];
		int i = 0;
		for (SearchHit hit : hits.getHits()) {
			System.out.print("score:" + hit.getScore() + hit.getId() + "\n");
			topics[i] = hit.getId();
			System.out.println("FIELDS:");
			for (String key : hit.getFields().keySet()){
				System.out.println("field "+key+" : "+hit.getFields().get(key).getValue().toString());
			}
			System.out.println("end");
			switch (i) {
			case 0:  
				//entry.setTopic1(SetupServlet.topicsMap.get(hit.getId()));
				if (hit.getFields()==null) System.out.println("hit.getFields()==null");
				String idString0 = hit.fields().get("id").value();
				Integer id0 = Integer.valueOf(idString0);
				String content0 = hit.fields().get("content").value();
				content0 = content0.replaceAll(",", ", ");
				String hypernyms0 = hit.fields().get("hypernyms").value();
				String isas0 = hit.fields().get("isas").value();
				Topic topic1 = new Topic(id0, content0, hypernyms0, isas0);
				entry.setTopic1(topic1);
				entry.setScoreTopic1(hit.getScore());
				entry.setHighlightTopic1(process(toString(hit.highlightFields())));
				entry.setTextAndTopic1();
				System.out.println("highlight = "+ process(toString(hit.highlightFields()))); 
				break;
			case 1: 
				//entry.setTopic2(SetupServlet.topicsMap.get(hit.getId()));
				if (hit.getFields()==null) System.out.println("hit.getFields()==null");
				String idString = hit.fields().get("id").value();
				Integer id = Integer.valueOf(idString);
				String content1 = hit.fields().get("content").value();
				content1 = content1.replaceAll(",", ", ");
				String hypernyms = hit.fields().get("hypernyms").value();
				String isas = hit.fields().get("isas").value();
				Topic topic2 = new Topic(id, content1, hypernyms, isas);
				entry.setTopic2(topic2);
				entry.setScoreTopic2(hit.getScore());
				entry.setHighlightTopic2(process(toString(hit.highlightFields())));
				entry.setTextAndTopic2();
				break;
			case 2:  
				//entry.setTopic3(SetupServlet.topicsMap.get(hit.getId()));
				String content = hit.fields().get("content").value();
				content = content.replaceAll(",", ", ");
				entry.setTopic3(new Topic(Integer.valueOf((String)hit.fields().get("id").value()), content, (String)hit.fields().get("hypernyms").value(), (String)hit.fields().get("isas").value()));
				entry.setScoreTopic3(hit.getScore());
				entry.setTextAndTopic3();
				entry.setHighlightTopic3(process(toString(hit.highlightFields())));
				break;
			default:
				break;
			}
			i++;
		}

		
		
		if ( errors.isEmpty() ) {
			result = "OK";
		} else {
			result = "ERROR";
		}

		return entry;
		*/
	}

	private String process(String highlights) {
		List<String> fragmentsList = new ArrayList<String>();
		StringWriter sw = new StringWriter();
		if (highlights.contains("<em>")){
			for (String s : highlights.split("<em>")){
				if (s.contains("</em>")){
					s = s.split("</em>")[0];
					fragmentsList.add(s);
					sw.append(s);
					sw.append(", ");
				}
			}
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-2);
		return sw.toString();
	}

	private String toString(Map<String, HighlightField> highlightFields) {
		StringWriter sw = new StringWriter();
		for (String s : highlightFields.keySet()){
			sw.append(s+ " "+ highlightFields.get(s).toString()+ "\n" );
		}
		System.out.println(sw.toString());
		return sw.toString();
	}
/*
	public Entry createEntry( HttpServletRequest request ) {
		String text = getValueField( request, CHAMP_TEXT );

		Entry entry = new Entry();

		try {
			validationText( text );
		} catch ( Exception e ) {
			setError( CHAMP_TEXT, e.getMessage() );
		}
		entry.setText( text );

		SearchHits hits = InputServlet.elasticSearch.search(esIndex, text.toLowerCase());
		String[] topics = new String[3];
		int i = 0;
		for (SearchHit hit : hits.getHits()) {
			System.out.print("score:" + hit.getScore() + hit.field("isas").toString()+ "\n");
			topics[i] = hit.getId();
			switch (i) {
			case 0:  
				entry.setTopic1(new Topic(Integer.valueOf(hit.getId()), hit.field("content").toString(), hit.field("hypernyms").toString(), hit.field("isas").toString()));
				entry.setScoreTopic1(hit.getScore());
				break;
			case 1:  
				entry.setTopic2(new Topic(Integer.valueOf(hit.getId()), hit.field("content").toString(), hit.field("hypernyms").toString(), hit.field("isas").toString()));
				entry.setScoreTopic2(hit.getScore());
				break;
			case 2:  
				entry.setTopic3(new Topic(Integer.valueOf(hit.getId()), hit.field("content").toString(), hit.field("hypernyms").toString(), hit.field("isas").toString()));
				entry.setScoreTopic3(hit.getScore());
				break;
			default:
				break;
			}
			i++;
		}

        if (entry.getText()!=null){
			if (entry.getText().equals("coucou")){
				entry.setTopic1("oiseau");
				entry.setTopic2("salutation");
				entry.setTopic3("mmmmhhh");
			} else {
				entry.setTopic1("...");
				entry.setTopic2("...");
				entry.setTopic3("...");
			}
		}

		if ( errors.isEmpty() ) {
			result = "OK";
		} else {
			result = "ERROR";
		}

		return entry;
	}
	*/

	private void validationText( String text ) throws Exception {
		if ( text != null ) {
			if ( text.trim().length() < 2 ) {
				throw new Exception( "Please enter longer text." );
			}
		} else {
			throw new Exception( "Please enter some text." );
		}
	}

	private void setError( String champ, String message ) {
		errors.put( champ, message );
	}

	public static String getValueField( HttpServletRequest request, String nomChamp ) {
		String valeur = request.getParameter( nomChamp );
		System.out.println("attribute : "+request.getMethod());
		System.out.println(request.getQueryString());
		System.out.println(request.getScheme());
		System.out.println(request.getAttribute("class"));
		if ( valeur == null || valeur.trim().length() == 0 ) {
			return null;
		} else {
			return valeur;
		}
	}

}
