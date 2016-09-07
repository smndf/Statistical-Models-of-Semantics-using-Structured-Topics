package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ContactServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8795437140870351893L;

	public static final String VUE_CONTACT = "/WEB-INF/Contact.jsp";

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

		this.getServletContext().getRequestDispatcher( VUE_CONTACT ).forward( request, response );

	}


	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

	}

}

