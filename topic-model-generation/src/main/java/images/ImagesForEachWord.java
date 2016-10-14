package images;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import util.CleanTopicWords;
import classification.DBPediaCrawler;

public class ImagesForEachWord {

	public static void main(String[] args){

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("file with processed topics")
				.isRequired()
				.create("clusters"));
		CommandLine cl = null;
		boolean success = false;
		try {
			cl = clParser.parse(options, args);
			success = true;
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		if (!success) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar imagesURLs.jar", options, true);
			System.exit(1);
		}
		final String clustersFile = cl.getOptionValue("clusters");
		try {
			BufferedReader br = new BufferedReader(new FileReader(clustersFile));
			String line;
			Map<Integer,Set<String>> clusters = new HashMap<Integer,Set<String>>();
			while ((line = br.readLine()) != null){
				Set<String> cluster = new HashSet<String>();
				String[] lineSplit = line.split("\t");
				Integer noCluster = Integer.valueOf(lineSplit[0]);
				for (String s : lineSplit[1].split(",")){
					cluster.add(s);
				}
				clusters.put(noCluster,cluster);
			}
			br.close();
			Integer nbThreads = Runtime.getRuntime().availableProcessors();
			ExecutorService exec = Executors.newFixedThreadPool(nbThreads);
			try {
				for (final Entry<Integer,Set<String>> entry : clusters.entrySet()) {
					exec.submit(new Runnable() {
						@Override
						public void run() {
							Integer noCluster = entry.getKey();
							Set<String> cluster = entry.getValue();
							Map<String,String> image_urls = new HashMap<String,String>();
							for (String word : cluster){
								String url = getImageURLForWord(CleanTopicWords.cleanExpression(word.split("\\(")[0]));
								if (url != null && !url.isEmpty()){
									//System.out.println(word + " => " + url);
									image_urls.put(word,url);
								} else {
									String urlSerelex = "http://serelex.org/image/"+word.split("\\(")[0];
									image_urls.put(word,urlSerelex);
								}
							}
							String outputFile = "images/" + clustersFile.substring(0,clustersFile.length()-4) + "-" + noCluster + ".txt";
							new File("images").mkdir();
							writeImagesURLs(outputFile, image_urls);
						}
					});
				}
			} finally {
				exec.shutdown();
			}
			/*
			// OLD CODE WITHOUT PARALLELISM
			for (Integer noCluster : clusters.keySet()){
				Set<String> cluster = clusters.get(noCluster);
				Map<String,String> image_urls = new HashMap<String,String>();
				for (String word : cluster){
					String url = getImageURLForWord(CleanTopicWords.cleanExpression(word.split("\\(")[0]));
					if (url != null && !url.isEmpty()){
						//System.out.println(word + " => " + url);
						image_urls.put(word,url);
					} else {
						String urlSerelex = "http://serelex.org/image/"+word.split("\\(")[0];
						image_urls.put(word,urlSerelex);
					}
				}
				String outputFile = "images/" + clustersFile.substring(0,clustersFile.length()-4) + "-" + noCluster + ".txt";
				new File("images").mkdir();
				writeImagesURLs(outputFile, image_urls);
			}*/
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeImagesURLs(String outputFile,
			Map<String, String> image_urls) {

		File fo = new File(outputFile);
		try{
			FileWriter fw = new FileWriter (fo);
			for (Entry<String, String> entry : image_urls.entrySet()){
				fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");				
			}
			fw.flush();
			fw.close();
		}
		catch (IOException exception)
		{
			System.out.println (exception.getMessage());
		}

	}

	private static String getImageURLForWord(String word) {

		String url;
		// try with Serelex database
		//if (word.split(" ").length>1) lastPart = word.split(" ")[word.split(" ").length-1];
		if ((url = getImageURLForWordFromSerelex(word))!=null && !url.isEmpty()){
			System.out.println("url serelex = " + url);
			return url;
		}
		// try with dbpedia
		if ((url = getImageURLForWordFromDBPedia(word))!=null && !url.isEmpty()){ 
			System.out.println("url dbpedia = " + url);
			return url;
		}
		// try with FlickrSearch
		if ((url = getImageURLForWordFromFlickr(word))!=null && !url.isEmpty()){ 
			System.out.println("url flickr = " + url);
			return url;
		}
		return "http://serelex.org/image/"+word;
	}

	private static String getImageURLForWordFromSerelex(String word) {
		String url = "http://serelex.org/image/"+word.toLowerCase();
		while (isRedirected(url)) url = followRedirection(url);
		if (imageExists(url)) return url;
		return null;
	}

	private static boolean isRedirected(String url) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			int response = -1;
			try {
			response = con.getResponseCode();
			} catch (java.net.SocketException e) {
				return false;
			}
			return (response == HttpURLConnection.HTTP_MOVED_PERM || response == HttpURLConnection.HTTP_MOVED_TEMP);
		}
		catch (Exception e) {
			e.printStackTrace();
			//return false;
		}
		return false;
	}

	private static String followRedirection(String url) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			int response = con.getResponseCode();
			//System.out.println("response = "+response);
			if (response == 302){
				String newUrl = con.getHeaderField("Location");
				//System.out.println("new url = "+newUrl);
				return newUrl;
			}
			//return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			//return false;
		}
		return url;
	}

	private static String getImageURLForWordFromDBPedia(String word) {
		String url = DBPediaCrawler.getArticleImageURL(word.toLowerCase());
		if (!url.isEmpty()) return url;
		return null;
	}

	private static String getImageURLForWordFromFlickr(String word) {
		return FlickrSearch.search(word);
	}

	private static boolean imageExists(String url) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			//        HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con =
					(HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			int response = con.getResponseCode();
			System.out.println("response = "+response);
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}



}
