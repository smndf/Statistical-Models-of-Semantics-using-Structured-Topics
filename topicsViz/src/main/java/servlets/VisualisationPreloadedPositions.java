package servlets;
import java.io.BufferedReader;
import java.io.File;
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

public class VisualisationPreloadedPositions extends HttpServlet {
	/**
	 * 
	 */
	//public static final String esIndex = "7clusters";
	private static final long serialVersionUID = 1L;

	public static final String ATT_GRAPH   = "topicgraph";

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		System.out.println("post");

		String sentString = new String();
		try {
			String line = "";
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null){
				System.out.println("line  = "+line);
				sentString += line;
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		System.out.println("total read string = "+sentString);


		String action = request.getParameter("action");
		System.out.println(action);

		if (action.equals("store")){

			String clusterNo = sentString.split("#")[0];
			String nodes = sentString.split("#")[1];
			String edges = sentString.split("#")[2];
			nodes = Visualisation.cleanNodesJson(nodes);
			edges = Visualisation.cleanEdgesJson(edges);
			
			String fileName = "clusterWithPositions-"+clusterNo+".txt";
			File f = new File("/Users/simondif/Documents/"+fileName);
			FileWriter fw = new FileWriter(f);
			fw.write(nodes);
			fw.write("\n");
			fw.write(edges);
			fw.close();
			System.out.println("file "+fileName+" created in "+f.getAbsolutePath());
		} else if (action.equals("reset")){

			String clusterNo = sentString.split("#")[0];
			System.out.println(clusterNo);
			String fileName = "clusterWithPositions-"+clusterNo+".txt";
			File f = new File("/Users/simondif/Documents/"+fileName);
			if(f.exists() && !f.isDirectory()){
				f.delete();
				System.out.println("File "+f.getAbsolutePath()+" deleted.");

			}
		}
	}

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

		BufferedReader br = new BufferedReader(new FileReader("/Users/simondif/Documents/workspace/structured-topics/CWddt-news-n200-345k-closureFiltBef2FiltAft/CWddt-news-n200-345k-closureFiltBef2FiltAft-161.txt"));
		String nodes = br.readLine();
		String edges = br.readLine();
		br.close();
		
		GraphPositions gp = new GraphPositions(nodes, edges);


		/*TopicGraph tg = new TopicGraph();
		ClustersReader cr = new ClustersReader();
		System.out.println(request.getParameter("noTopic"));
		final int clusterNumber = Integer.valueOf(request.getParameter("noTopic"));
		String[] cluster;
		String fileStem = "";
		String esIndex = CreationEntryForm.esIndex;
		if (esIndex.equals("7clusters")){
			fileStem = "/WEB-INF/7clusters/7clustersNotFiltered-";
		} else if (esIndex.equals("swe-topics")){
			fileStem = "/WEB-INF/CWddt-news-n200-345k-closureFiltBef2FiltAft/CWddt-news-n200-345k-closureFiltBef2FiltAft-";
		} else if (esIndex.equals("mwe-topics")){
			fileStem = "/WEB-INF/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed/CWddt-news-n200-345k-closureFiltBef2FiltAft-";
		} else {
			fileStem = "problemWithElasticSearchIndexName";
		}
		System.out.println(fileStem);
		cluster = readCluster(fileStem + clusterNumber +".txt");
		System.out.println(cluster[0]);
		tg.setListJson(cluster[0]);
		tg.setConnectionsJson(cluster[1]);
		String imagesFileUrl = "/WEB-INF/images/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed-" + clusterNumber +".txt";
		//TODO tg.setImagesJson("'"+imagesFileToJson(imagesFileUrl)+"'");

		request.setAttribute( ATT_GRAPH, tg );
		 */
		request.setAttribute(ATT_GRAPH, gp);

		this.getServletContext().getRequestDispatcher( "/WEB-INF/graphWithPositions.jsp" ).forward( request, response );
	}

	public String[] readCluster(String relativeWebPath) throws IOException {

		String[] cluster = new String[2];

		System.out.println(relativeWebPath);
		/*InputStream clustersStream = getClass().getResourceAsStream(clusterFile);
		BufferedReader input = new BufferedReader(new InputStreamReader(clustersStream));
		 */
		//BufferedReader input = new BufferedReader(new FileReader(clusterFile));
		InputStream is = getServletContext().getResourceAsStream(relativeWebPath);
		BufferedReader input = new BufferedReader(new InputStreamReader(is));
		String[] lineSplit;
		String line = null;

		while ( (line = input.readLine()) != null){ 
			lineSplit = line.split("\t");
			if (lineSplit.length == 3){
				cluster[0] = lineSplit[1];
				cluster[1] = lineSplit[2];
			}
		}
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

}