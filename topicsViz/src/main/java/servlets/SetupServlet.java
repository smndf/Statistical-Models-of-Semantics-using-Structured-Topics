package servlets;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import classes.ElasticSearch;
import classes.Topic;
import classes.Utils;

public class SetupServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String VUE_INPUT = "/WEB-INF/InputForm.jsp";
	public static final String VUE_SETUP   = "/WEB-INF/SetupPage.jsp";

	//public static Map<String,Topic> topicsMap;
	//public static Map<String,Integer> freqMap;

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		String topicsContentFile = "/good_clusters.txt";
		String topicsHypsFile = "/hyps_good_clusters.txt";
		String topicsIsaFile = "/isa_good_clusters.txt";
		String inputFile = "/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessed.txt";
		
		/*Utils u = new Utils();
		freqMap = u.buildFreqMap("/news100M_stanford_cc_word_count");
		if (freqMap == null){
			System.out.println("freqmap null setupServlet");
		} else {
			System.out.println("freqmap not null setupServlet");
		}*/
		//topicsMap = 
				//elasticSearch.loadClusters(inputFile);
		this.getServletContext().getRequestDispatcher( "/WEB-INF/SetupPage.jsp" ).forward( request, response );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

		this.getServletContext().getRequestDispatcher( VUE_INPUT ).forward( request, response );

	}
}
