package classes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ClustersReader {

	public String[] readClusters(String clusterFile) throws IOException {

		String[] cluster = new String[2];
		
		System.out.println(clusterFile);
		/*InputStream clustersStream = getClass().getResourceAsStream(clusterFile);
		BufferedReader input = new BufferedReader(new InputStreamReader(clustersStream));
		*/
		BufferedReader input = new BufferedReader(new FileReader(clusterFile));
		
		String[] lineSplit;
		String line = null;

		while ( (line = input.readLine()) != null){ 
			lineSplit = line.split("\t");
			if (lineSplit.length == 3){
				cluster[0] = lineSplit[1];
				cluster[1] = lineSplit[2];
			}
		}
		return cluster;
	}

}
