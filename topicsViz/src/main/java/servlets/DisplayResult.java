package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import classes.CreationEntryForm;
import classes.ElasticSearch;
import classes.Results;

public class DisplayResult extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static ElasticSearch elasticSearch;
	public static final String VUE_RESULT   = "/WEB-INF/ResultDisplay.jsp";

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		elasticSearch = new ElasticSearch();

		if (request.getParameterMap().keySet().contains(CreationEntryForm.CHAMP_TEXT) && request.getParameterMap().keySet().contains("elasticSearchIndex")){

			CreationEntryForm form = new CreationEntryForm();

			/* Traitement de la requête et récupération du bean en résultant */
			String queryContent = CreationEntryForm.getValueField( request, CreationEntryForm.CHAMP_TEXT );
			CreationEntryForm.updateIndexToQuery(CreationEntryForm.getValueField( request, "elasticSearchIndex" ));
			System.out.println("elasticSearchIndex = "+CreationEntryForm.getValueField( request, "elasticSearchIndex" ));
			Results results = form.createEntry( queryContent, Integer.MAX_VALUE);
			
			/* Ajout du bean et de l'objet métier à l'objet requête */
			request.setAttribute( "text", results.getText() );
			request.setAttribute( "entries", results.getEntries() );

			
				this.getServletContext().getRequestDispatcher( VUE_RESULT ).forward( request, response );
		} else {
			//this.getServletContext().getRequestDispatcher( VUE_INPUT ).forward( request, response );
		}   
	}
	
	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

		System.out.println("post DisplayResult");
		if (request.getParameterMap().keySet().contains(CreationEntryForm.CHAMP_TEXT)){

			CreationEntryForm form = new CreationEntryForm();

			/* Traitement de la requête et récupération du bean en résultant */
			String queryContent = CreationEntryForm.getValueField( request, CreationEntryForm.CHAMP_TEXT );
			Results results = form.createEntry( queryContent, Integer.MAX_VALUE);
			
			/* Ajout du bean et de l'objet métier à l'objet requête */
			request.setAttribute( "text", results.getText() );
			request.setAttribute( "entries", results.getEntries() );

			
				this.getServletContext().getRequestDispatcher( VUE_RESULT ).forward( request, response );
		} else {
			//this.getServletContext().getRequestDispatcher( VUE_INPUT ).forward( request, response );
		}       }
}
