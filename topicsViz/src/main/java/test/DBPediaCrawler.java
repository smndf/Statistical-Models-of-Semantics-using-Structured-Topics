package test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DBPediaCrawler {

	public String getRandomAbstract(){
		String url = "http://en.wikipedia.org/wiki/Special:Random";
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String uri = doc.baseUri();
		//System.out.println(uri);
		String title = uri.split("/")[uri.split("/").length-1];
		System.out.print(title + "\t");
		url = "http://dbpedia.org/resource/" + title;
		//System.out.println(url);
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(doc);
		String selector = "[property=dbo:abstract]";
		String docAbstract = doc.select(selector).text();
		return docAbstract;
	}

	public String getArticleText(String title){
		Document doc = null;
		title = title.replace(" ","_");
		String url = "http://dbpedia.org/resource/" + title;
		System.out.println(url);

		try {
			doc = Jsoup.connect(url).get();
		} catch (SocketTimeoutException e){
			System.out.println("timeout");
		} catch (org.jsoup.HttpStatusException e){
			System.out.println("not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(doc);
		String selector = "span[property=dbo:abstract][xml:lang=en]";
		if (doc!=null){
			Elements elements = doc.select(selector);
			String docAbstract = elements.text();
			return docAbstract;			
		} else return "";
	}



}
