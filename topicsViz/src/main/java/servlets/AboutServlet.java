package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class AboutServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3197713349324825553L;
	public static final String VUE_ABOUT = "/WEB-INF/About.jsp";

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

		this.getServletContext().getRequestDispatcher( VUE_ABOUT ).forward( request, response );

	}


	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{

	}

}
