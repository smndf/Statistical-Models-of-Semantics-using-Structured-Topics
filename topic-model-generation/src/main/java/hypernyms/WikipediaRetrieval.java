package hypernyms;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.github.kevinsawicki.http.HttpRequest;

public class WikipediaRetrieval {

	public static void main( String[] args )
	{
		for (String arg : args){
			System.out.println(arg + " : "+lookUp(arg));
		}
		if (args.length == 0){
			lookUp("baba");
		}
	}

	public static String lookUp(String word){
		//HttpRequest.get("http://google.com").receive(System.out);
		HttpRequest request = HttpRequest.get("https://en.wikipedia.org/wiki/"+word, false);
		System.out.println(request.toString()); // GET http://google.com?q=baseball%20gloves&size=100
		String response = request.body();
		//System.out.println(response.substring(0, 10000));
		String wikipedia = "en.wikipedia";

		if (response.contains("Category:Surnames")){
			System.out.println("Surnames");
			int begin = 0;
			if (response.contains("people with the surname include")){				
				begin = response.indexOf("people with the surname include");
			} 
			response = response.substring(begin,response.length());
			begin = response.indexOf("<li>");
			int end = response.indexOf("</ul>");
			response = response.substring(begin,end);
			for (String line : response.split("\n")){
				line = line.substring(line.indexOf("</a>")+4, line.indexOf("</li>"));
				System.out.println("line : " + line);				
			}
		} else {
			if (response.contains("Disambiguation_pages")){
				System.out.println("Disambiguation page");
				int begin = 0;
				if (response.contains("refer to")){
					begin = response.indexOf("refer to");
					response = response.substring(begin);
					begin = 0;
				}
				response = response.substring(begin,response.length());
				begin = response.indexOf("<h2>");
				response = response.substring(begin+5,response.length());
				begin = response.indexOf("<h2>");
				response = response.substring(begin+5,response.length());
				begin = 0;
				//begin = response.indexOf("mw-content-text");
				int end = response.indexOf("<table");
				System.out.println("begin = "+begin+ "   end = "+end);
				response = response.substring(begin+4,end);
				for (String line : response.split("\n")){
					//line = line.substring(line.indexOf("</a>"), line.indexOf("</li>"));
					line = clean(line,'(',')');
					line = clean(line,'<','>');
					line = clean(line,'[',']');
					System.out.println("line : " + line);				
				}
			} else {
				System.out.println("else");
				int begin = response.indexOf("<p>");
				int end = response.indexOf("</p>");
				response = response.substring(begin,end);
				for (String line : response.split("\n")){
					//line = clean(line,'(',')');
					line = clean(line,'<','>');
					//line = clean(line,'[',']');
					if (line.contains(".")){
						line = line.substring(0, line.indexOf("."));
					}
					System.out.println("line : " + line);				
				}
			}
		}
		return "";
	}

	private static String clean(String line, char c1, char c2) {
		int pos = 0;
		Set<Integer> set = new HashSet<Integer>();
		boolean newpos1 = false;
		int pos1=0;
		while(line.contains(String.valueOf(c1)) || line.contains(String.valueOf(c2))){
			//System.out.println("essai");
			if (line.charAt(pos)==c1){
				pos1 = pos;
				newpos1 = true;
			}
			if (line.charAt(pos)==c2){
				int pos2 = pos+1;
				if (newpos1){
					//System.out.println("remove : "+line.substring(pos1, pos2));
					line = line.substring(0, pos1)+line.substring(pos2, line.length());
					//System.out.println(line);
					pos = -1;
					newpos1 = false;
				}
			}
			pos++;
			if (pos >= line.length()){
				pos = 0;
			}
		}
		return line;
	}

}
