package classes;

import static org.elasticsearch.node.NodeBuilder.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.common.xcontent.XContentFactory.*;


import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.ParseContext.Document;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import com.sun.org.apache.bcel.internal.generic.LoadClass;

import servlets.SetupServlet;
import servlets.Visualisation;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.elasticsearch.index.query.QueryBuilders.*;


public class ElasticSearch {

	private static final String CLUSTER_NAME = "structured_topics";

	public static void main(String[] args){

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with clusters to load e.g. CWddt-wiki-mwe-posFiltBef2FiltAftProcessed.txt")
				.isRequired()
				.create("clusters"));
		options.addOption(OptionBuilder.withArgName("string").hasArg()
				.withDescription("ElasticSearch index name")
				.isRequired()
				.create("index"));
		options.addOption(OptionBuilder.withArgName("integer").hasArg()
				.withDescription("ElasticSearch port number e.g. 9350")
				.isRequired()
				.create("port"));
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
			formatter.printHelp("java -jar loadClusters.jar", options, true);
			System.exit(1);
		}
		String inputFile = cl.getOptionValue("clusters");
		String esIndex = cl.getOptionValue("index");
		Integer esPort = Integer.valueOf(cl.getOptionValue("port"));
		

		//String inputFile = "/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed.txt"; 

		try {
			loadClusters2(true, inputFile, esIndex, esPort);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	public static Map<String, Topic> loadClusters2(boolean externFile, String inputFile, String indexName, int esPort) throws IOException {
		
		Client client;// = node.client();
		
		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();

		client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress("localhost", esPort));


		XContentBuilder englishAnalyzer = null;
		try {
			englishAnalyzer = XContentFactory.jsonBuilder()
				.startObject()
					.startObject("analysis")
						.startObject("filter")
							.startObject("english_stop")
								.field("type", "stop")
								.field("stopwords", "_english_") 
							.endObject()
							.startObject("english_stemmer")
								.field("type", "stemmer")
								.field("language", "english")
							.endObject()
							.startObject("english_possessive_stemmer")
								.field("type", "stemmer")
								.field("language", "possessive_english")
							.endObject()
						.endObject()
						.startObject("tokenizer")
							.startObject("CommaTokenizer")
								.field("type", "pattern")
								.field("pattern",",")
							.endObject()
						.endObject()
						.startObject("analyzer")
							.startObject("customAnalyzer")
				//.field("type","custom")
								.field("tokenizer","standard")
								.array("filter","standard","lowercase","english_stop","english_stemmer")
							.endObject()
						.endObject()
					.endObject()
				.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (client.admin().indices().prepareExists(indexName).get().isExists()){			
			client.admin().indices().prepareDelete(indexName).get();
		}
		client.admin().indices().prepareCreate(indexName).setSettings(englishAnalyzer).get();
		
		BufferedReader br = new BufferedReader(new FileReader(inputFile));

		Map<String,Topic> topicsMap = new HashMap<String,Topic>();
		try{

			String line;

			while((line= br.readLine())!=null){

				//topicsHypsLine = brHyps.readLine();
				//topicsIsaLine = brIsa.readLine();
				String numCluster = line.split("\t")[0];
				String cluster = line.split("\t")[1];
				String hypernyms = line.split("\t")[2];
				String isas = line.split("\t")[3];

				IndexResponse indexResponse = client.prepareIndex(indexName, "topic", numCluster)
						.setSource(jsonBuilder()
								.startObject()
								.field("id",numCluster)
								.field("content", cluster)
								.field("isas", isas)
								.field("hypernyms", hypernyms)
								.endObject()
								)
								.execute()
								.actionGet();
				topicsMap.put(numCluster,new Topic(Integer.valueOf(numCluster),fiftyFirstTerms(cluster),hypernyms, isas));

				// Index name
				String _index = indexResponse.getIndex();
				// Type name
				String _type = indexResponse.getType();
				// Document ID (generated or not)
				String _id = indexResponse.getId();
				// Version (if it's the first time you index this document, you will get: 1)
				long _version = indexResponse.getVersion();
				// isCreated() is true if the document is a new one, false if it has been updated
				boolean created = indexResponse.isCreated();
				if (created) System.out.println("created");
				else System.out.println("updated");

				System.out.println("index " + _index + " type " + _type + " id " + _id + " version "+ _version + " created.");
			}

			br.close();
			// on shutdown
			//node.close();
		} catch (IOException exception){
			System.err.println (exception.getMessage());
		}
		client.close();
		return topicsMap;
	}

	public static Map<String,Topic> loadSWEClusters(boolean externFile, String inputFile, int esPort) throws IOException{

		// on startup

		//NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
		//nodeBuilder.settings().put("http.port", 9250).put("transport.tcp.port", 9350);
		//Node node = nodeBuilder.node();
		String sweIndex = "swe-topics";

		Client client;// = node.client();

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();

		client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress("localhost", esPort));


		XContentBuilder englishAnalyzer = XContentFactory.jsonBuilder()
			.startObject()
				.startObject("analysis")
					.startObject("filter")
						.startObject("english_stop")
							.field("type", "stop")
							.field("stopwords", "_english_") 
						.endObject()
						.startObject("english_stemmer")
							.field("type", "stemmer")
							.field("language", "english")
						.endObject()
						.startObject("english_possessive_stemmer")
							.field("type", "stemmer")
							.field("language", "possessive_english")
						.endObject()
					.endObject()
					.startObject("tokenizer")
						.startObject("CommaTokenizer")
							.field("type", "pattern")
							.field("pattern",",")
						.endObject()
					.endObject()
					.startObject("analyzer")
						.startObject("customAnalyzer")
			//.field("type","custom")
							.field("tokenizer","standard")
							.array("filter","standard","lowercase","english_stop","english_stemmer")
						.endObject()
					.endObject()
				.endObject()
			.endObject();

		if (client.admin().indices().prepareExists(sweIndex).get().isExists()){			
			client.admin().indices().prepareDelete(sweIndex).get();
		}
		client.admin().indices().prepareCreate(sweIndex).setSettings(englishAnalyzer).get();

		BufferedReader br = new BufferedReader(new FileReader(inputFile));

		Map<String,Topic> topicsMap = new HashMap<String,Topic>();
		try{

			String line;

			while((line= br.readLine())!=null){

				//topicsHypsLine = brHyps.readLine();
				//topicsIsaLine = brIsa.readLine();
				String numCluster = line.split("\t")[0];
				String cluster = line.split("\t")[1];
				String hypernyms = line.split("\t")[2];
				String isas = line.split("\t")[3];

				IndexResponse indexResponse = client.prepareIndex(sweIndex, "topic", numCluster)
						.setSource(jsonBuilder()
								.startObject()
								.field("id",numCluster)
								.field("content", cluster)
								.field("isas", isas)
								.field("hypernyms", hypernyms)
								.endObject()
								)
								.execute()
								.actionGet();
				topicsMap.put(numCluster,new Topic(Integer.valueOf(numCluster),fiftyFirstTerms(cluster),hypernyms, isas));

				// Index name
				String _index = indexResponse.getIndex();
				// Type name
				String _type = indexResponse.getType();
				// Document ID (generated or not)
				String _id = indexResponse.getId();
				// Version (if it's the first time you index this document, you will get: 1)
				long _version = indexResponse.getVersion();
				// isCreated() is true if the document is a new one, false if it has been updated
				boolean created = indexResponse.isCreated();
				if (created) System.out.println("created");
				else System.out.println("updated");

				System.out.println("index " + _index + " type " + _type + " id " + _id + " version "+ _version + " created.");
			}

			br.close();
			// on shutdown
			//node.close();
		} catch (IOException exception){
			System.err.println (exception.getMessage());
		}
		client.close();
		return topicsMap;
	}

	public static Map<String,Topic> loadMWEClusters(boolean externFile, String inputFile, String esIndex, int esPort) throws IOException{

		// on startup

		//NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
		//nodeBuilder.settings().put("http.port", 9250).put("transport.tcp.port", 9350);
		//Node node = nodeBuilder.node();

		Client client;// = node.client();

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();

		client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress("localhost", esPort));


		XContentBuilder englishAnalyzer = XContentFactory.jsonBuilder()
		.startObject()
			.startObject("analysis")
				.startObject("filter")
					.startObject("english_stop")
						.field("type", "stop")
						.field("stopwords", "_english_") 
					.endObject()
					.startObject("english_stemmer")
						.field("type", "stemmer")
						.field("language", "english")
					.endObject()
					.startObject("english_possessive_stemmer")
						.field("type", "stemmer")
						.field("language", "possessive_english")
					.endObject()
				.endObject()
				.startObject("tokenizer")
					.startObject("CommaTokenizer")
						.field("type", "pattern")
						.field("pattern",",")
					.endObject()
				.endObject()
				.startObject("analyzer")
					.startObject("customAnalyzer")
		//.field("type","custom")
						.field("tokenizer","standard")
						.array("filter","standard","lowercase","english_stop","english_stemmer")
					.endObject()
				.endObject()
			.endObject()
		.endObject();

		if (client.admin().indices().prepareExists(esIndex).get().isExists()){			
			client.admin().indices().prepareDelete(esIndex).get();
		}
		client.admin().indices().prepareCreate(esIndex).setSettings(englishAnalyzer).get();

		BufferedReader br = new BufferedReader(new FileReader(inputFile));

		Map<String,Topic> topicsMap = new HashMap<String,Topic>();
		try{

			String line;

			while((line= br.readLine())!=null){

				//topicsHypsLine = brHyps.readLine();
				//topicsIsaLine = brIsa.readLine();
				String numCluster = line.split("\t")[0];
				String cluster = line.split("\t")[1];
				String hypernyms = line.split("\t")[2];
				String isas = line.split("\t")[3];

				IndexResponse indexResponse = client.prepareIndex(esIndex, "topic", numCluster)
						.setSource(jsonBuilder()
								.startObject()
								.field("id",numCluster)
								.field("content", cluster)
								.field("isas", isas)
								.field("hypernyms", hypernyms)
								.endObject()
								)
								.execute()
								.actionGet();
				topicsMap.put(numCluster,new Topic(Integer.valueOf(numCluster),fiftyFirstTerms(cluster),hypernyms, isas));

				// Index name
				String _index = indexResponse.getIndex();
				// Type name
				String _type = indexResponse.getType();
				// Document ID (generated or not)
				String _id = indexResponse.getId();
				// Version (if it's the first time you index this document, you will get: 1)
				long _version = indexResponse.getVersion();
				// isCreated() is true if the document is a new one, false if it has been updated
				boolean created = indexResponse.isCreated();
				if (created) System.out.println("created");
				else System.out.println("updated");

				System.out.println("index " + _index + " type " + _type + " id " + _id + " version "+ _version + " created.");
			}
			br.close();
			// on shutdown
			//node.close();
		} catch (IOException exception){
			System.err.println (exception.getMessage());
		}
		client.close();
		return topicsMap;
	}

	public static Map<String,Topic> load7Clusters(boolean externFile, String inputFile, int esPort) throws IOException{

		// on startup

		//NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
		//nodeBuilder.settings().put("http.port", 9250).put("transport.tcp.port", 9350);
		//Node node = nodeBuilder.node();
		Client client;// = node.client();

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();

		client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress("localhost", esPort));


		XContentBuilder englishAnalyzer = XContentFactory.jsonBuilder()
				.startObject()
				.startObject("analysis")
					.startObject("filter")
						.startObject("english_stop")
							.field("type", "stop")
							.field("stopwords", "_english_") 
						.endObject()
						.startObject("english_stemmer")
							.field("type", "stemmer")
							.field("language", "english")
						.endObject()
						.startObject("english_possessive_stemmer")
							.field("type", "stemmer")
							.field("language", "possessive_english")
						.endObject()
					.endObject()
					.startObject("tokenizer")
						.startObject("CommaTokenizer")
							.field("type", "pattern")
							.field("pattern",",")
						.endObject()
					.endObject()
					.startObject("analyzer")
						.startObject("customAnalyzer")
			//.field("type","custom")
							.field("tokenizer","standard")
							.array("filter","standard","lowercase","english_stop","english_stemmer")
						.endObject()
					.endObject()
				.endObject()
			.endObject();

		client.admin().indices().prepareDelete("7clusters").get();
		client.admin().indices().prepareCreate("7clusters").setSettings(englishAnalyzer).get();

		String topicsContentFile = "/good_clustersEnrichedSynonymsAnd40Hyps.txt";
		String topicsHypsFile = "/hyps_good_clusters.txt";
		String topicsIsaFile = "/isa_good_clusters.txt";

		BufferedReader brWords = new BufferedReader(new InputStreamReader(ElasticSearch.class.getResourceAsStream(topicsContentFile)));
		BufferedReader brHyps = new BufferedReader(new InputStreamReader(ElasticSearch.class.getResourceAsStream(topicsHypsFile)));
		BufferedReader brIsas = new BufferedReader(new InputStreamReader(ElasticSearch.class.getResourceAsStream(topicsIsaFile)));
		Map<String,Topic> topicsMap = new HashMap<String,Topic>();
		try{

			String line;

			while((line= brWords.readLine())!=null){

				String topicsHypsLine = brHyps.readLine();
				String topicsIsaLine = brIsas.readLine();

				String numCluster = line.split("\t")[0];
				String cluster = line.split("\t")[1];
				String hypernyms = topicsHypsLine.split("\t")[1];
				String isas = topicsIsaLine.split("\t")[1];
				System.out.println("isas = "+isas);

				IndexResponse indexResponse = client.prepareIndex("7clusters", "topic", numCluster)
						.setSource(jsonBuilder()
								.startObject()
								.field("id",numCluster)
								.field("content", cluster)//cleanCluster(cluster))
								.field("isas", isas)
								.field("hypernyms", hypernyms)
								.endObject()
								)
								.execute()
								.actionGet();
				topicsMap.put(numCluster,new Topic(Integer.valueOf(numCluster),fiftyFirstTerms(cluster),hypernyms, isas));

				// Index name
				String _index = indexResponse.getIndex();
				// Type name
				String _type = indexResponse.getType();
				// Document ID (generated or not)
				String _id = indexResponse.getId();
				// Version (if it's the first time you index this document, you will get: 1)
				long _version = indexResponse.getVersion();
				// isCreated() is true if the document is a new one, false if it has been updated
				boolean created = indexResponse.isCreated();
				if (created) System.out.println("created");
				else System.out.println("updated");

				System.out.println("index " + _index + " type " + _type + " id " + _id + " version "+ _version + " created.");
			}

			// on shutdown
			//node.close();
		} catch (IOException exception){
			System.err.println (exception.getMessage());
		}
		client.close();
		return topicsMap;

	}

	public static Map<String,Topic> loadClusters(boolean externFile, String inputFile, int esPort) throws IOException{

		// on startup

		//NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
		//nodeBuilder.settings().put("http.port", 9250).put("transport.tcp.port", 9350);
		//Node node = nodeBuilder.node();
		Client client;// = node.client();

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();

		client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress("localhost", esPort));


		XContentBuilder englishAnalyzer = XContentFactory.jsonBuilder()
				.startObject()
				.startObject("analysis")
					.startObject("filter")
						.startObject("english_stop")
							.field("type", "stop")
							.field("stopwords", "_english_") 
						.endObject()
						.startObject("english_stemmer")
							.field("type", "stemmer")
							.field("language", "english")
						.endObject()
						.startObject("english_possessive_stemmer")
							.field("type", "stemmer")
							.field("language", "possessive_english")
						.endObject()
					.endObject()
					.startObject("tokenizer")
						.startObject("CommaTokenizer")
							.field("type", "pattern")
							.field("pattern",",")
						.endObject()
					.endObject()
					.startObject("analyzer")
						.startObject("customAnalyzer")
			//.field("type","custom")
							.field("tokenizer","standard")
							.array("filter","standard","lowercase","english_stop","english_stemmer")
						.endObject()
					.endObject()
				.endObject()
			.endObject();

		//client.admin().indices().prepareDelete(CLUSTER_NAME).get();
		client.admin().indices().prepareCreate(CLUSTER_NAME).setSettings(englishAnalyzer).get();

		BufferedReader br;
		if (externFile) br = new BufferedReader(new FileReader(inputFile));
		else br = new BufferedReader(new InputStreamReader(ElasticSearch.class.getResourceAsStream(inputFile)));

		Map<String,Topic> topicsMap = new HashMap<String,Topic>();
		try{

			String line;

			while((line= br.readLine())!=null){

				//topicsHypsLine = brHyps.readLine();
				//topicsIsaLine = brIsa.readLine();
				String numCluster = line.split("\t")[0];
				String cluster = line.split("\t")[1];
				String hypernyms = line.split("\t")[2];
				String isas = line.split("\t")[3];
				System.out.println("isas = "+isas);

				IndexResponse indexResponse = client.prepareIndex("structured_topics", "topic", numCluster)
						.setSource(jsonBuilder()
								.startObject()
								.field("id",numCluster)
								.field("content", cleanCluster(cluster))
								.field("isas", isas)
								.field("hypernyms", hypernyms)
								.endObject()
								)
								.execute()
								.actionGet();
				topicsMap.put(numCluster,new Topic(Integer.valueOf(numCluster),fiftyFirstTerms(cluster),hypernyms, isas));

				// Index name
				String _index = indexResponse.getIndex();
				// Type name
				String _type = indexResponse.getType();
				// Document ID (generated or not)
				String _id = indexResponse.getId();
				// Version (if it's the first time you index this document, you will get: 1)
				long _version = indexResponse.getVersion();
				// isCreated() is true if the document is a new one, false if it has been updated
				boolean created = indexResponse.isCreated();
				if (created) System.out.println("created");
				else System.out.println("updated");

				System.out.println("index " + _index + " type " + _type + " id " + _id + " version "+ _version + " created.");
			}

			// on shutdown
			//node.close();
		} catch (IOException exception){
			System.err.println (exception.getMessage());
		}
		client.close();
		return topicsMap;

	}

	private static String cleanCluster(String cluster) {
		StringWriter sw = new StringWriter();
		for (String s : cluster.split(",")){
			sw.append(s.substring(0, s.lastIndexOf("("))+",");
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-1);
		return sw.toString();
	}

	private static String fiftyFirstTerms(String cluster) {
		String[] words = cluster.split(", ");
		StringWriter sw = new StringWriter();
		for (int i=0;i<50 && i<words.length;i++){
			sw.append(words[i]+", ");
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-2);
		return sw.toString();
	}

	public SearchHits search(String indexName, String stringQuery, int nbResults) {
		
		String[] nounsQuery = NLPUtil.keepOnlyNounsAndNE(stringQuery);
		
		QueryBuilder termQuery = termsQuery("content",    
				nounsQuery).boost((float) 10.); 
		
		MatchQueryBuilder matchQuery = matchQuery("content", stringQuery).analyzer("customAnalyzer"); 

		QueryBuilder boolQuery = boolQuery()
			    .should(termQuery)    
			    .should(matchQuery);

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();
		TransportClient tc = new TransportClient(settings);
		Client client;
		if (Visualisation.frink) {
			client = tc.addTransportAddress(new InetSocketTransportAddress("localhost",9350));			
		} else {
			client = tc.addTransportAddress(new InetSocketTransportAddress("localhost",9300));
		}
		SearchResponse response = client.prepareSearch(indexName)
				.setTypes("topic")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQuery)             // Query
				.setFrom(0).setSize(nbResults).setExplain(true)
				.addFields("id","isas","hypernyms","content")
				.addHighlightedField("content",10,10)
				.execute()
				.actionGet();
		
		System.out.println(response.getHits().getHits().length + " results for \"" + stringQuery + "\"" );
		for (SearchHit hit : response.getHits().getHits()) {
			System.out.println("score:" + hit.getScore() + " id:" + hit.getId());
			//System.out.println(hit.getSourceAsString());
		}
		return response.getHits();
	}

	public SearchHits search2(String index, String stringQuery, int nbResults) {
		//Node node = nodeBuilder().node();

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();
		TransportClient tc = new TransportClient(settings);
		Client client;
		if (Visualisation.frink) {
			client = tc.addTransportAddress(new InetSocketTransportAddress("localhost",9350));			
		} else {
			client = tc.addTransportAddress(new InetSocketTransportAddress("localhost",9300));
		}
		String[] nounsQuery = NLPUtil.keepOnlyNounsAndNE(stringQuery);
		QueryBuilder qb = termsQuery("content",    
				nounsQuery); 

		System.out.println(index);
		SearchResponse response = client.prepareSearch(index)
				.setTypes("topic")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(qb)             // Query
				.setFrom(0).setSize(nbResults).setExplain(true)
				.addFields("id","isas","hypernyms","content")
				.addHighlightedField("content",10,10)
				//.setHighlighterNumOfFragments(10)
				.execute()
				.actionGet();
		client.close();
		tc.close();
		//node.close();
		return response.getHits();
	}
	
	public static SearchHits retrieveTopic(String index, String noTopic) {
		//Node node = nodeBuilder().node();

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put("cluster.name","elasticsearch")
				.build();
		TransportClient tc = new TransportClient(settings);
		Client client;
		if (Visualisation.frink) {
			client = tc.addTransportAddress(new InetSocketTransportAddress("localhost",9350));			
		} else {
			client = tc.addTransportAddress(new InetSocketTransportAddress("localhost",9300));
		}

		QueryBuilder qb = termQuery("id", noTopic); 

		System.out.println(index);
		SearchResponse response = client.prepareSearch(index)
				.setTypes("topic")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(qb)             // Query
				.setFrom(0).setSize(1).setExplain(true)
				.addFields("id","isas","hypernyms","content")
				.addHighlightedField("content",10,10)
				//.setHighlighterNumOfFragments(10)
				.execute()
				.actionGet();
		client.close();
		tc.close();
		return response.getHits();
	}
}
