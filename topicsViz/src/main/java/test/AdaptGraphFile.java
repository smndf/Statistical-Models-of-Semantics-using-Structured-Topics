package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class AdaptGraphFile {

	public static void main(String[] args) {

		String fileName = "/Users/simondif/Downloads/clusterWithPositions-2.txt";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String nodes = br.readLine();
			String edges = br.readLine();
			nodes = nodes.substring("{\"_options\":{},\"_data\":{".length());
			nodes = nodes.substring(0, nodes.lastIndexOf("},\"length\""));
			nodes = nodes.replaceAll("\"[0-9]*\":", "");
			nodes = "["+nodes+"]";
			//System.out.println(nodes);
			edges = edges.substring("{\"_options\":{},\"_data\":{".length());
			edges = edges.substring(0, edges.lastIndexOf("},\"length\""));
			edges = edges.replaceAll("\".{8}-.{4}-.{4}-.{4}-.{12}\":", "");
			edges = "["+edges+"]";
			//System.out.println(edges);
			
			
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
