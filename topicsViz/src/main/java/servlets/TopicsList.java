package servlets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import classes.CreationEntryForm;
import classes.Topic;


public class TopicsList extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

		System.out.println("get topic list");
		System.out.println("parameters = "+request.getParameterMap());
		if (request.getParameterMap().containsKey("elasticSearchIndex") && request.getParameterMap().containsKey("criteria")){
			String esIndex = request.getParameter("elasticSearchIndex");
			System.out.println("with param elasticsearch = "+esIndex);
			CreationEntryForm.updateIndexToQuery(esIndex);
			String sortCriteria = request.getParameter("criteria");
			String suffix;
			if (sortCriteria.equals("id")) suffix = "ById.txt";
			else if (sortCriteria.equals("size")) suffix = "BySize.txt";
			else suffix = "ByScore.txt";
			String topicsFile;
			if (esIndex.equals("7clusters")){
				topicsFile = "NOT IMPLEMENTED YET";
			} else if (esIndex.equals("swe-topics")){
				if (Visualisation.frink) topicsFile = "/home/simondif/structured-topics/data/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed"+suffix;
				else topicsFile =	"/Users/simondif/Documents/workspace/structured-topics/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed"+suffix;
			} else if (esIndex.equals("mwe-topics")){
				if (Visualisation.frink) topicsFile = "/home/simondif/structured-topics/data/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed"+suffix;
				else topicsFile =	"/Users/simondif/Documents/workspace/structured-topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed"+suffix;
			} else {
				topicsFile = "problemWithElasticSearchIndexName";
			}
			List<Topic> topicsList = getTopicsList(topicsFile);
			String direction = request.getParameter("direction");
			if (direction.equals("minus")) Collections.reverse(topicsList);
			request.setAttribute("topics", topicsList );
		}
		// TODO change with esIndex value
		//String topicsFile = "/home/simondif/structured-topics/data/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed.txt";
		//List<Topic> topicsList = getTopicsList(topicsFile);
		//request.setAttribute("topics", topicsList );
		this.getServletContext().getRequestDispatcher( "/WEB-INF/topicsList.jsp" ).forward( request, response );
		
	}
	
	private List<Topic> getTopicsList(String topicsFile) {
		
		List<Topic> topics = new ArrayList<Topic>();
		BufferedReader br;

		try {
			br = new BufferedReader(new FileReader(topicsFile));
			String line;
			while ((line = br.readLine()) != null){
				//System.out.println("new line topic id "+line.split("\t")[0]);
				Topic topic = Topic.getTopicFromLine(line);
				if (topic != null) topics.add(topic);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return topics;
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		System.out.println("post topicList");
		if (request.getParameterMap().keySet().contains("elasticSearchIndex")) {
			CreationEntryForm.updateIndexToQuery(CreationEntryForm.getValueField( request, "elasticSearchIndex" ));
			System.out.println("index updated: index = "+CreationEntryForm.esIndex);
		} else {
			System.out.println(request.getParameterMap());
		}
		String sortCriteria = request.getParameter("criteria");
		System.out.println("sortCriteria = "+sortCriteria);
		String suffix;
		if (sortCriteria.equals("id")) suffix = "ById.txt";
		else if (sortCriteria.equals("size")) suffix = "BySize.txt";
		else suffix = "ByScore.txt";
		String topicsFile;
		String esIndex = CreationEntryForm.esIndex;
		if (esIndex.equals("7clusters")){
			topicsFile = "NOT IMPLEMENTED YET";
		} else if (esIndex.equals("swe-topics")){
			if (Visualisation.frink) topicsFile = "/home/simondif/structured-topics/data/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed"+suffix;
			else topicsFile =	"/Users/simondif/Documents/workspace/structured-topics/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed"+suffix;
		} else if (esIndex.equals("mwe-topics")){
			if (Visualisation.frink) topicsFile = "/home/simondif/structured-topics/data/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed"+suffix;
			else topicsFile =	"/Users/simondif/Documents/workspace/structured-topics/CWddt-wiki-mwe-posFiltBef2FiltAftProcessed"+suffix;
		} else {
			topicsFile = "problemWithElasticSearchIndexName";
		}
		String direction = request.getParameter("direction");
		System.out.println(topicsFile);
		List<Topic> topicsList = getTopicsList(topicsFile);
		if (direction.equals("minus")) Collections.reverse(topicsList);
		System.out.println("topic "+topicsList.size());
		request.setAttribute("topics", topicsList );
		this.getServletContext().getRequestDispatcher( "/WEB-INF/topicsList.jsp" ).forward( request, response );
	}
}
