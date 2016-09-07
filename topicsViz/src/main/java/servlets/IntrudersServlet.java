package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import classes.CreationEntryForm;
import classes.ElasticSearch;
import classes.Entry;

public class IntrudersServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3567008208772787270L;
	
	private static final String FILE_PATH_LOCAL = "/Users/simondif/Documents/workspace/demoApp/src/main/resources/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessedWithIntruders.txt";
	private static final String FILE_PATH_FRINK = "/home/simondif/structured-topics/CWddt-news-n200-345k-closureFiltBef2FiltAftProcessedWithIntruders.txt";
	private static final String INTRUDERS_JSP = "/WEB-INF/intruders.jsp";
	private static final String MSG_RIGHT_ANSWER = "Your previous answer was correct!";
	private static final String MSG_WRONG_ANSWER = "Your previous answer was incorrect, but maybe the topic is not so good... Try with the next one!";

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		System.out.println("get");
		if (request.getParameterMap().containsKey("item")){			
			String selectedIntruder = request.getParameter("item");
			boolean rightAnswer = writeResult(selectedIntruder);
			if (rightAnswer) request.setAttribute("msg_prev_answer", MSG_RIGHT_ANSWER);
			else request.setAttribute("msg_prev_answer", MSG_WRONG_ANSWER);
		}
		
		List<String> words = getWordsWithIntruder();
		request.setAttribute("words", words);
		this.getServletContext().getRequestDispatcher( INTRUDERS_JSP ).forward( request, response );
	}

	private List<String> getWordsWithIntruder() throws IOException {
		List<String> words = new ArrayList<String>();
		String file_path = FILE_PATH_LOCAL;
		if (Visualisation.frink) file_path = FILE_PATH_FRINK;
		BufferedReader br = new BufferedReader(new FileReader(file_path));
		String line = null;
		while ((line = br.readLine()) != null){
			if (line.split("\t").length==6){
				words.add(line.split("\t")[0]);
				words.add(line.split("\t")[1]);
				words.add(line.split("\t")[2]);
				words.add(line.split("\t")[3]);
				words.add(line.split("\t")[4]);
				words.add(line.split("\t")[5]);
				break;
			}
		}
		Collections.shuffle(words);
		br.close();
		return words;
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		System.out.println("post");
		String selectedIntruder = request.getParameter("item");
		writeResult(selectedIntruder);
		List<String> words = getWordsWithIntruder();
		request.setAttribute("words", words);
		this.getServletContext().getRequestDispatcher( INTRUDERS_JSP ).forward( request, response );
	  
	}

	private boolean writeResult(String selectedIntruder) throws IOException {

		List<String> lines = new ArrayList<String>();
		String file_path = FILE_PATH_LOCAL;
		if (Visualisation.frink) file_path = FILE_PATH_FRINK;
		BufferedReader br = new BufferedReader(new FileReader(file_path));
		String line = null;
		boolean lineFound = false;
		boolean rightAnswer = false;
		while ((line = br.readLine()) != null){
			if (line.split("\t").length==6 && !lineFound){
				lineFound = true;
				lines.add(line+"\t"+selectedIntruder);
				if (line.split("\t")[5].equals(selectedIntruder)){
					rightAnswer = true;
				}
			} else {
				lines.add(line);
			}
		}
		br.close();
		File f = new File(file_path);
		FileWriter fw = new FileWriter(f);
		for (String line2 : lines){
			fw.write(line2+"\n");
		}
		fw.close();
		return rightAnswer;
	}
	
}
