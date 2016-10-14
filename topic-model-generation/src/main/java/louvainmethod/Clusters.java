package louvainmethod;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


public class Clusters {

	public void convertFromLM(String inputFile, String mapFile, String outputFile) throws IOException {
		System.out.println("");
		Map<Integer,Set<Integer>> clustersMap = buildClustersMap(inputFile);

		Map<Integer,String> mapItoS = readNodesNames(mapFile);

		//String outputFile = "clusters" +inputFile.substring(0, inputFile.length()-4) + ".txt";
		writeClusters(clustersMap,mapItoS,outputFile);
		String statsFile = outputFile+"Stats.txt";
		writeClustersStats(clustersMap,statsFile );
	}

	private void writeClustersStats(Map<Integer, Set<Integer>> clusters,
			String statsFile) throws IOException {
		Integer nbClusters = clusters.size();
		Double avSizeClusters = 0.0;
		Integer minSize = Integer.MAX_VALUE;
		Integer maxSize = Integer.MIN_VALUE;
		for (Set<Integer> cluster : clusters.values()) {
			avSizeClusters += cluster.size();
			if (cluster.size()>maxSize) {
				maxSize = cluster.size();
			}
			if (cluster.size()<minSize) {
				minSize = cluster.size();
			}
		}
		avSizeClusters /= nbClusters.doubleValue();
		Double standardDeviation = 0.0;
		for (Set<Integer> cluster : clusters.values()) standardDeviation += (cluster.size()-avSizeClusters) * (cluster.size()-avSizeClusters);
		standardDeviation /= nbClusters.doubleValue();
		standardDeviation = Math.sqrt(standardDeviation);
		FileWriter fw = new FileWriter(statsFile);
		fw.write("nbClusters\t"+nbClusters+"\n");
		fw.write("avSizeClusters\t"+avSizeClusters+"\n");
		fw.write("standardDeviation\t"+standardDeviation+"\n");
		fw.write("minSize\t"+minSize+"\n");
		fw.write("maxSize\t"+maxSize+"\n");
		fw.write(statsFile.split("\\.")[0]+"\t"+nbClusters+"\t"+avSizeClusters+"\t"+standardDeviation+"\t"+minSize+"\t"+maxSize+"\n");
		fw.close();		
	}

	private void writeClustersMultiFiles(Map<Integer, Set<Integer>> clustersMap,Map<Integer, String> mapItoS, String outputFile) {

		int ctr = 0;
		File fo;
		FileWriter fw;
		StringWriter writer = new StringWriter();
		try{
			while(!clustersMap.isEmpty()){
				Set<Integer> set = clustersMap.remove(ctr);
				if(!set.isEmpty()){
					fo = new File(outputFile + ctr + ".txt");
					fw = new FileWriter (fo);
					writer.getBuffer().setLength(0);
					writer.write(toString(ctr,set,mapItoS));
					fw.write (writer.toString());
					fw.close();
					ctr++;
				}
			}
			System.out.println (ctr + " files have been created ("+outputFile +"....txt).");
		}
		catch (IOException exception)
		{
			System.err.println ("Erreur lors de la lecture : " + exception.getMessage());
		}		
	}

	private String toString(int ctr, Set<Integer> set, Map<Integer, String> mapItoS) {

		StringWriter writer = new StringWriter();
		writer.write(ctr);
		writer.write("\t");
		for (Integer node : set){
			writer.write(mapItoS.get(node));
			writer.write(",");
		}
		writer.getBuffer().setLength(writer.toString().length()-1);
		return writer.toString();
	}

	private Map<Integer, String> readNodesNames(String mapFile) throws IOException {

		//InputStream is2 = getClass().getResourceAsStream(mapFile);
		//BufferedReader mapText = new BufferedReader(new InputStreamReader(is2));
		BufferedReader mapText = new BufferedReader(new FileReader(mapFile));
		Map<Integer,String> mapItoS = new HashMap<Integer,String>();
		String[] lineSplit = new String[2];
		String line = null;
		try {
			while ( (line = mapText.readLine()) != null){ 
				lineSplit = line.split(" ");
				if (lineSplit.length>1){
					mapItoS.put(Integer.parseInt(lineSplit[0]), lineSplit[1]);
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mapText.close();
		return mapItoS;
	}

	private void writeClusters(Map<Integer, Set<Integer>> clustersMap,
			Map<Integer, String> mapItoS, String outputFile) {

		File fo = new File(outputFile);

		try{

			FileWriter fw = new FileWriter (fo);
			StringWriter writer = new StringWriter();
			writer.write(toString(clustersMap,mapItoS));
			fw.write (writer.toString());
			fw.close();
			System.out.println("fini");
		}
		catch (IOException exception)
		{
			System.err.println (exception.getMessage());
		}
	}

	private String toString(Map<Integer,Set<Integer>> clustersMap,Map<Integer,String> mapItoS){
		StringWriter sw = new StringWriter();

		for (Entry<Integer,Set<Integer>> entry : clustersMap.entrySet()) {
			sw.write(entry.getKey().toString());
			sw.write("\t");
			sw.write("0");
			sw.write("\t");
			sw.write("0");
			sw.write("\t");
			sw.write("0");
			sw.write("\t");
			sw.write("0.0");
			sw.write("\t");
			for (Integer node : entry.getValue()){
				String tmp = mapItoS.get(node);
				/*if (tmp.split("#").length > 0){
					sw.write(mapItoS.get(node).split("#")[0]);					
				}else{
					sw.write(mapItoS.get(node));					
				}*/
				sw.write(mapItoS.get(node));
				sw.write(",");
			}
			sw.getBuffer().setLength(sw.toString().length()-1);
			sw.write("\n");
		}

		return sw.toString();
	}

	private Map<Integer,Set<Integer>> buildClustersMap(String inputFile) throws IOException{
		//InputStream is = getClass().getResourceAsStream(inputFile);
		//BufferedReader br = new BufferedReader(new InputStreamReader(is));
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		Map<Integer,Set<Integer>> clustersMap = new HashMap<Integer,Set<Integer>>();
		String[] lineSplit = new String[2];
		String line = null;
		try {
			while ( (line = br.readLine()) != null){ 

				lineSplit = line.split(" ");

				if (lineSplit.length>1){
					/* from format "node clusterCenter" to map "clusterCenter set of nodes" 
					 * 
					 */
					if (clustersMap.containsKey(Integer.parseInt(lineSplit[1]))){
						clustersMap.get(Integer.parseInt(lineSplit[1])).add(Integer.parseInt(lineSplit[0]));
					} else {
						Set<Integer> set = new HashSet<Integer>();
						set.add(Integer.parseInt(lineSplit[0]));
						clustersMap.put(Integer.parseInt(lineSplit[1]),set);
					}
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br.close();
		return clustersMap;
	}


}
