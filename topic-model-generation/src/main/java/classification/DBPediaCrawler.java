package classification;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DBPediaCrawler {

	public static void main(String[] args){
		getArticlesFromList();
	}

	public static String getArticlesFromList(){
		Document doc = null;
		//title = title.replace(" ","_");
		//String url = "http://dbpedia.org/resource/" + title;
		String url = "https://en.wikipedia.org/wiki/WHO_Model_List_of_Essential_Medicines";
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
		//System.out.println(doc.toString());

		// FOR LISTS
		List<String> ulList = selectAllTags(doc.toString(),"<ul>");
		List<String> liList = new ArrayList<String>();
		for (String s : ulList){
			List<String> l = selectAllTags(s,"<li>");
			if (l!=null) liList.addAll(l);
		}
		List<String> links = getLinks(liList);
		for (String link : links){
			System.out.println(link);
		}
		/*
		// FOR TABLES
		List<String> tbodyList = selectAllTags(doc.toString(),"<tbody>");
		List<String> trList = new ArrayList<String>();
		for (String s : tbodyList){
			List<String> l = selectAllTags(s,"<tr>");
			if (l!=null) trList.addAll(l);
		}
		List<String> tdList = new ArrayList<String>();
		for (String s : trList){
			List<String> l = selectFirstTag(s,"<td>");
			if (l!=null) tdList.addAll(l);
		}
		List<String> links = getLinks(tdList);
		for (String link : links){
			System.out.println(link);
		}
		 */
		/*
		getAllLinks(doc.toString());
		 */
		return "";
	}

	private static List<String> getLinks(List<String> list) {
		List<String> links = new ArrayList<String>();
		for (String s : list){
			if (s.contains("href") && !s.contains("class=\"new\"")){
				int hrefPos = s.indexOf("href");
				int begin = s.indexOf("\"", hrefPos);
				int end = s.indexOf("\"", begin+1);
				String link = s.substring(begin+1, end);
				if (!link.toLowerCase().contains("category") && !link.toLowerCase().contains("list")  && !link.toLowerCase().contains("portal")  && !link.toLowerCase().contains("disambiguation")){
					links.add(link);
				}
			}
		}
		return links;
	}

	private static List<String> getAllLinks(String doc) {
		Set<String> set = new HashSet<String>();
		boolean beginPart = true;
		for (String s : doc.split("<a hre")){
			if (!beginPart){
				//s = s.substring(doc.indexOf('"'), doc.indexOf('"', doc.indexOf('"')+1));
				if (s.contains("\"/wiki/") && !s.contains("hist") && !s.contains("myth") && !s.contains("/wiki/Wikipedia") && !s.contains("/wiki/File") && !s.contains("/wiki/List")){
					s = s.substring(s.indexOf('"')+1);
					s = s.substring(0, s.indexOf('"'));
					if (!set.contains(s)) {
						set.add(s);
					}
					System.out.println(s);
				}
			}
			beginPart = false;
		}
		System.out.println(set.size());



		/*
		List<String> links = new ArrayList<String>();

		for (String s : list){
			if (s.contains("href")){
				int hrefPos = s.indexOf("href");
				int begin = s.indexOf("\"", hrefPos);
				int end = s.indexOf("\"", begin+1);
				String link = s.substring(begin+1, end);
				if (!link.toLowerCase().contains("category") && !link.toLowerCase().contains("list")  && !link.toLowerCase().contains("portal")  && !link.toLowerCase().contains("disambiguation")){
					links.add(link);
				}
			}
		}
		 */
		return null;
	}

	public static List<String> selectFirstTag(String doc,String tag){
		if (!doc.contains(tag)){
			return null;
		} 
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		int begin = doc.indexOf(tag);
		String endTag = "</"+tag.substring(1);
		System.out.println(doc);
		int end;
		if (!doc.contains(endTag)) end = doc.length()-1;
		else end = doc.indexOf(endTag)+tag.length();
		System.out.println(begin+" "+end);
		doc = doc.substring(begin, end);
		for (String s : doc.split(tag)){
			list1.add(s);
		}
		for (String s : list1){
			list2.add(s.split(endTag)[0]);
		}
		list1.clear();
		/*for (String s : list2){
			System.out.println(s);
		}*/
		return list2;
	}

	public static List<String> selectAllTags(String doc,String tag){
		if (!doc.contains(tag)){
			return null;
		} 
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		int begin = doc.indexOf(tag);
		String endTag = "</"+tag.substring(1);
		System.out.println(doc);
		int end;
		if (!doc.contains(endTag)) end = doc.length()-1;
		else end = doc.lastIndexOf(endTag)+tag.length();
		System.out.println(begin+" "+end);
		doc = doc.substring(begin, end);
		for (String s : doc.split(tag)){
			list1.add(s);
		}
		for (String s : list1){
			list2.add(s.split(endTag)[0]);
		}
		list1.clear();
		/*for (String s : list2){
			System.out.println(s);
		}*/
		return list2;
	}

	public static String getRandomAbstract(){
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

	public static String getArticleText(String title){
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
	
	public static String getArticleImageURL(String title){
		Document doc = null;
		title = title.replace(" ","_");
		title = title.substring(0, 1).toUpperCase() + title.substring(1, title.length());
		String url = "http://dbpedia.org/page/" + title;
		//System.out.println("dbpedia url = " + url);
		try {
			doc = Jsoup.connect(url).get();
			//System.out.println(doc.text());
		} catch (SocketTimeoutException e){
			System.out.println("timeout");
		} catch (org.jsoup.HttpStatusException e){
			System.out.println("not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(doc);
		String selector = "a[rel=dbo:thumbnail nofollow]";
		if (doc!=null){
			Elements elements = doc.select(selector);
			String imageURL = elements.text();
			//System.out.println("url = "+imageURL);
			return imageURL;			
		} else return "";
	}


}
