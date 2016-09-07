package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import classes.CreationEntryForm;
import classes.ElasticSearch;
import classes.Topic;

public class TextServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3938396190945978854L;

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		
		String noTopic = request.getParameter("noTopic");
		String esIndex = CreationEntryForm.esIndex;
		SearchHits hits = ElasticSearch.retrieveTopic(esIndex, noTopic);
		SearchHit hit = hits.getHits()[0];
		String idString0 = hit.fields().get("id").value();
		Integer id0 = Integer.valueOf(idString0);
		String content0 = hit.fields().get("content").value();
		content0 = content0.replaceAll(",", ", ");
		String hypernyms0 = hit.fields().get("hypernyms").value();
		String isas0 = hit.fields().get("isas").value();
		Topic topic = new Topic(id0, content0, hypernyms0, isas0);
		request.setAttribute("topic", topic);
		this.getServletContext().getRequestDispatcher( "/WEB-INF/topicText.jsp" ).forward( request, response );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
	}
	
}
