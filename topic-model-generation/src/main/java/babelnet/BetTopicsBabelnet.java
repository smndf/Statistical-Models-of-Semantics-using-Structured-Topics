package babelnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import util.MapUtil;

public class BetTopicsBabelnet {

	public static void main(String[] args) {

		String fileName = "/Users/simondif/Documents/workspace/structured-topics/LMddt-wiki-n30-1400k-v3-closure-f0-e0_0BabelnetMapping.txt";
		try {
				File f = new File(fileName);
				BufferedReader br = new BufferedReader(new FileReader(f));
				Map<Integer,Double> topDomainScoresMap = new HashMap<Integer,Double>();
				readBabelnetMapping(br, topDomainScoresMap);
				br.close();
				
				topDomainScoresMap = MapUtil.sortByValue(topDomainScoresMap);
				int i = 0;
				for (Integer clusterId : topDomainScoresMap.keySet()){
					System.out.println(clusterId+"\t"+topDomainScoresMap.get(clusterId));
					i++;
					if (i>20) break;
				}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void readBabelnetMapping(BufferedReader br,
			Map<Integer, Double> topDomainScoresMap) throws IOException {

		String line = null;
		while ((line = br.readLine())!=null){
			Integer clusterId = Integer.valueOf(line.split("\t")[0]);
			if (line.contains("\t\t") || line.split("\t").length<10) {
				topDomainScoresMap.put(clusterId, 0.);
			} else {
				try{
					Double topDomainScore = Double.valueOf(line.split("\t")[9]);
					System.out.println(topDomainScore);
					topDomainScoresMap.put(clusterId, topDomainScore);
				} catch (java.lang.NumberFormatException e){
					System.err.println(e);
					topDomainScoresMap.put(clusterId, 0.);
				}
			}
		}

	}

}
