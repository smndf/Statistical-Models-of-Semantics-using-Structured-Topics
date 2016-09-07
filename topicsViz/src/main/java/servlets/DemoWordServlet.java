package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import classes.CreationEntryForm;
import classes.Entry;
import classes.Results;

public class DemoWordServlet extends HttpServlet  {

	private static final String CHAMP_TEXT       = "unstructuredtext";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String ATT_RESULTS = "results";
	public static final String ATT_FORM   = "form";
	public static final String VUE_INPUT = "/WEB-INF/InputForm.jsp";
	public static final String VUE_RESULT   = "/WEB-INF/ResultDisplay.jsp";

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		String index = request.getParameter("index");
		CreationEntryForm.updateIndexToQuery(index);
		
		/* Préparation de l'objet formulaire */
		CreationEntryForm form = new CreationEntryForm();
		
		String query = "Jaguar (/ˈdʒæɡjuː.ər/ jag-ew-ər) is the luxury vehicle brand of Jaguar Land Rover,[6] a British multinational car manufacturer with its headquarters in Whitley, Coventry, England, owned by the Indian company Tata Motors[7] since 2008. Jaguar's business was founded as the Swallow Sidecar Company in 1922, originally making motorcycle sidecars before developing bodies for passenger cars. Under the ownership of S. S. Cars Limited the business extended to complete cars made in association with Standard Motor Co many bearing Jaguar as a model name. The company's name was changed from S. S. Cars to Jaguar Cars in 1945. A merger with the British Motor Corporation followed in 1966,[8] the resulting enlarged company now being renamed as British Motor Holdings (BMH), which in 1968 merged with Leyland Motor Corporation and became British Leyland, itself to be nationalised in 1975. Jaguar was de-merged from British Leyland and was listed on the London Stock Exchange in 1984, becoming a constituent of the FTSE 100 Index until it was acquired by Ford in 1990. [9] Jaguar has, in recent years, manufactured cars for the British Prime Minister, the most recent delivery being an XJ in May 2010. [10] The company also holds royal warrants from Queen Elizabeth II and Prince Charles.[11]Jaguar cars today are designed in Jaguar Land Rover's engineering centres at the Whitley plant in Coventry and at their Gaydon site in Warwickshire, and are manufactured in Jaguar's Castle Bromwich assembly plant in Birmingham with some manufacturing expected to take place in the Solihull plant. In September 2013 Jaguar Land Rover announced plans to open a 100 million GBP (160 million USD) research and development centre in the University of Warwick, Coventry to create a new generation of vehicle technologies. The carmaker said around 1,000 academics and engineers would work there and that construction would start in 2014.[12][13]";
		
		/* Traitement de la requête et récupération du bean en résultant */
		Results results = form.createEntry( query, 3);
		
		
		/* Ajout du bean et de l'objet métier à l'objet requête */
		request.setAttribute( "text", results.getText() );
		request.setAttribute( "entries", results.getEntries() );

		if ( form.getErrors().isEmpty() ) {
			/* Si aucune erreur, alors affichage de la fiche récapitulative */
			this.getServletContext().getRequestDispatcher( VUE_RESULT ).forward( request, response );
		} else {
			/* Sinon, ré-affichage du formulaire de création avec les erreurs */
			this.getServletContext().getRequestDispatcher( VUE_INPUT ).forward( request, response );
		}    }


	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		//System.out.println(SetupServlet.map.get(1));

	}
}
