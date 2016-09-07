package servlets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import classes.ClustersReader;
import classes.CreationEntryForm;
import classes.GraphPositions;
import classes.TopicGraph;

public class Visualisation extends HttpServlet {
	/**
	 * 
	 */
	//public static final String esIndex = "7clusters";
	private static final long serialVersionUID = 1L;

	public static final String ATT_GRAPH   = "topicgraph";
	public static final String ATT_POSITIONS_STORED   = "positionsStored";

	public static final boolean frink = true;
	
	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		System.out.println("\ndoPost");

		String sentString = new String();
		try {
			String line = "";
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null){
				sentString += line;
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		System.out.println("total read string = "+sentString);


		String action = request.getParameter("action");
		System.out.println("action = " +action);
		String fileStemWithPositions = "";
		String fileStemWithoutPositions = "";
		String esIndex = CreationEntryForm.esIndex;
		if (esIndex.equals("news200-2000-cw")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft/withPositions/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";
			if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft/withoutPositions/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft-";
			else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withoutPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";
		} else if (esIndex.equals("wiki-200-2000-cw")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft/withPositions/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";
			if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft/withoutPositions/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft-";
			else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withoutPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";
		} else if (esIndex.equals("news-50-2000-cw")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft/withPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";
			if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft/withoutPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-";
			else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withoutPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";
		} else if (esIndex.equals("wiki-30-0-lm")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft/withPositions/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";
			if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft/withoutPositions/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft-";
			else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/withoutPositions/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed-";

		} else {
			fileStemWithPositions = "problemWithElasticSearchIndexName";
		}

		if (action.equals("store")){
			System.out.println("Store positions");

			String clusterNo = sentString.split("#")[0];
			String nodes = sentString.split("#")[1];
			//String edges = sentString.split("#")[2];
			
			String edges = readEdgesFromFileWithoutPositions(fileStemWithoutPositions,clusterNo);
			//nodes = cleanNodesJson(nodes);
			//edges = cleanEdgesJson(edges);
			//System.out.println("nodes = "+nodes);
			//System.out.println("edges = "+edges);
			
			String fileName = fileStemWithPositions+clusterNo+".txt";
			String dirPositions = fileName.substring(0, fileName.lastIndexOf("/"));
			new File(dirPositions).mkdir();
			System.out.println(dirPositions +" dir created");
			File f = new File(fileName);
			FileWriter fw = new FileWriter(f);
			fw.write(nodes);
			fw.write("\n");
			fw.write(edges);
			fw.close();
			System.out.println("file "+fileName+" created in "+f.getAbsolutePath());

		} else if (action.equals("reset")){
			System.out.println("begin reset");
			String clusterNo = sentString;//.split("#")[0];
			System.out.println(clusterNo);
			String fileName = fileStemWithPositions+clusterNo+".txt";
			File f = new File(fileName);
			if(f.exists() && !f.isDirectory()){
				f.delete();
				System.out.println("File "+f.getAbsolutePath()+" deleted.");

			}
			System.out.println("end reset");

		}
	}

	private String readEdgesFromFileWithoutPositions(
			String fileStemWithoutPositions, String clusterNo) {
		String fileName = fileStemWithoutPositions+clusterNo+".txt";
		String edges = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			br.readLine();
			edges = br.readLine();;
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("edges = "+edges);
		return edges;
	}

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		System.out.println("\ndoGet");
		boolean alreadyStored = false;
		
		System.out.println(request.getParameter("noTopic"));
		final int clusterNo = Integer.valueOf(request.getParameter("noTopic"));
		String esIndex = CreationEntryForm.esIndex;
		String fileStemWithPositions = "testfileStem1";
		if (esIndex.equals("news200-2000-cw")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft/withPositions/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-news-n200-345k-closureFiltBef2FiltAft/withPositions/CWddt-news-n200-345k-closureFiltBef2FiltAft-"; 
		} else if (esIndex.equals("wiki-200-2000-cw")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft/withPositions/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft/withPositions/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft-"; 
		} else if (esIndex.equals("news-50-2000-cw")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft/withPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft/withPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-"; 
		} else if (esIndex.equals("wiki-30-0-lm")){
			if (frink) fileStemWithPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft/withPositions/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft-";
			else fileStemWithPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft/withPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-"; 
		} else {
			fileStemWithPositions = "problemWithElasticSearchIndexName";
		}
		String fileName = fileStemWithPositions+clusterNo+".txt";
		File f = new File(fileName);
		if (f.exists()) alreadyStored = true;
		
		
		if (alreadyStored){
			System.out.println("positions already stored");
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String nodes = br.readLine();
			String edges = br.readLine();
			br.close();
			
			GraphPositions gp = new GraphPositions(nodes, edges);
			
			request.setAttribute(ATT_GRAPH, gp);
			request.setAttribute(ATT_POSITIONS_STORED, alreadyStored);

			this.getServletContext().getRequestDispatcher( "/WEB-INF/testGraph.jsp" ).forward( request, response );
			
		} else {
			
			System.out.println("positions not stored");
			String fileStemWithoutPositions = "testfileStem2line185"+esIndex;
			if (esIndex.equals("news200-2000-cw")){
				if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft/withoutPositions/CWddt-news-n200-345k-closure-f2000-e0_0FiltAft-";
				else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-news-n200-345k-closureFiltBef2FiltAft/withPositions/CWddt-news-n200-345k-closureFiltBef2FiltAft-"; 
			} else if (esIndex.equals("wiki-200-2000-cw")){
				if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft/withoutPositions/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft-";
				else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft/withPositions/CWddt-wiki-n200-380k-v3-closure-f2000-e0_0FiltAft-"; 
			} else if (esIndex.equals("news-50-2000-cw")){
				if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft/withoutPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-";
				else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft/withPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-"; 
			} else if (esIndex.equals("wiki-30-0-lm")){
				if (frink) fileStemWithoutPositions =	jetty.Main.DIR_TOPIC_GRAPHS+"/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft/withoutPositions/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft-";
				else fileStemWithoutPositions = "/Users/simondif/Documents/workspace/demoApp/data/topics/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0FiltAft/withPositions/CWddt-news-n50-485k-closure-f2000-e0_0FiltAft-"; 
			} else {
				fileStemWithoutPositions = "problemWithElasticSearchIndexName";
			}
			
			//TopicGraph tg = new TopicGraph();
			//ClustersReader cr = new ClustersReader();
			/*String[] cluster;
			cluster = readCluster(fileStemWithoutPositions + clusterNo +".txt");
			System.out.println(cluster[0]);
			tg.setListJson(cluster[0]);
			tg.setConnectionsJson(cluster[1]);*/
			//String imagesFileUrl = "/WEB-INF/images/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed-" + clusterNo +".txt";
			//tg.setImagesJson("'"+imagesFileToJson(imagesFileUrl)+"'");

			BufferedReader br = new BufferedReader(new FileReader(fileStemWithoutPositions + clusterNo +".txt"));
			String nodes = br.readLine();
			String edges = br.readLine();
			br.close();
			
			GraphPositions gp = new GraphPositions(nodes, edges);
			
			
			request.setAttribute( ATT_GRAPH, gp );
			request.setAttribute(ATT_POSITIONS_STORED, alreadyStored);

			this.getServletContext().getRequestDispatcher( "/WEB-INF/testGraph.jsp" ).forward( request, response );
		}
	}

	public String[] readCluster(String path) throws IOException {

		String[] cluster = new String[2];
		cluster[0] = "error in readClusters Visualisation";
		System.out.println(path);
		/*InputStream clustersStream = getClass().getResourceAsStream(clusterFile);
		BufferedReader input = new BufferedReader(new InputStreamReader(clustersStream));
		 */
		//BufferedReader input = new BufferedReader(new FileReader(clusterFile));
		//InputStream is = getServletContext().getResourceAsStream(path);
		//BufferedReader input = new BufferedReader(new InputStreamReader(is));
		
		BufferedReader input = new BufferedReader(new FileReader(path));
		String[] lineSplit;
		String line = null;

		while ( (line = input.readLine()) != null){ 
			lineSplit = line.split("\t");
			if (lineSplit.length == 3){
				cluster[0] = lineSplit[1];
				cluster[1] = lineSplit[2];
			}
		}
		input.close();
		return cluster;
	}


	/* returns a json with image url for each word
	 * '{"images":{
	 * 		"Marib(4)":"https://farm5.static.flickr.com/4038/5076300893_b0c4c2d3e8_n.jpg",
	 * 		"Abyan(4)":"https://farm7.static.flickr.com/6096/6275540451_e35d2cb655_n.jpg",
	 * 		"Ibb(4)":"https://farm9.static.flickr.com/8490/8171949580_6aebe77d94_n.jpg"}
	 * 	}'
	 */
	public String imagesFileToJson(String imagesFileUrl){
		Map<String,String> map = new HashMap<String,String>();
		InputStream is = getServletContext().getResourceAsStream(imagesFileUrl);
		BufferedReader input = new BufferedReader(new InputStreamReader(is));
		String[] lineSplit;
		String line = null;
		try {
			while ( (line = input.readLine()) != null){ 
				lineSplit = line.split("\t");
				if (lineSplit.length == 2){
					map.put(lineSplit[0].split("\\(")[0], lineSplit[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(imagesFileUrl);
		}
		JSONObject json = new JSONObject();
		json.put("images", map );
		return json.toString();
	}

	public static String cleanNodesJson(String nodes) {
		nodes = nodes.substring("{\"_options\":{},\"_data\":{".length());
		nodes = nodes.substring(0, nodes.lastIndexOf("},\"length\""));
		nodes = nodes.replaceAll("\".{8}-.{4}-.{4}-.{4}-.{12}\":", "");
		nodes = "["+nodes+"]";
		return nodes;
	}

	public static String cleanEdgesJson(String edges) {
		edges = edges.substring("{\"_options\":{},\"_data\":{".length());
		edges = edges.substring(0, edges.lastIndexOf("},\"length\""));
		edges = edges.replaceAll("\".{8}-.{4}-.{4}-.{4}-.{12}\":", "");
		edges = "["+edges+"]";
		return edges;
	}

}
