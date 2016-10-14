package babelnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeBabelnetResults {

	public static void main(String[] args) {

		try {
		File f = new File("/Users/simondif/Documents/workspace/structured-topics/BabelnetMappingScores.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		Map<String,List<Double>> scoresPerAlgo = new HashMap<String,List<Double>>();
		Map<String,List<Double>> scoresPerDDT = new HashMap<String,List<Double>>();
		Map<String,List<Double>> scoresPerFreq = new HashMap<String,List<Double>>();
		
		Map<String,List<Double>> avgScoreNonZeroElPerAlgo = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreNonZeroElPerDDT = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreNonZeroElPerFreq = new HashMap<String,List<Double>>();
		
		Map<String,List<Double>> avgScoreAllElPerAlgo = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreAllElPerDDT = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreAllElPerFreq = new HashMap<String,List<Double>>();

		while ((line=br.readLine())!=null){
				
			System.out.println(line);
			String[] split = line.split("\t");
				
				String algo = split[0];
				String ddt = split[1];
				String freq = split[2];
				//System.out.println(line);
				Double score = Double.valueOf(split[3]);
				
				
				addToMap(scoresPerAlgo, algo, score);
				addToMap(scoresPerDDT, ddt, score);
				addToMap(scoresPerFreq, freq, score);
				
			}
		
		System.out.println("per algo:\tCW\tLM");
		System.out.println("avg ratioNonZeroEl\t"+computeAvg(scoresPerAlgo.get("CW"))+"\t"+computeAvg(scoresPerAlgo.get("LM")));

		System.out.println("\n\nper DDT:\tnews200\tnews50\twiki200\twiki30");
		System.out.println("avg ratioNonZeroEl\t"+computeAvg(scoresPerDDT.get("news200"))+"\t"+computeAvg(scoresPerDDT.get("news50"))+"\t"+computeAvg(scoresPerDDT.get("wiki200"))+"\t"+computeAvg(scoresPerDDT.get("wiki30")));

		System.out.println("\n\nper freq:\t0\t500\t1200\t2000");
		System.out.println("avg ratioNonZeroEl\t"+computeAvg(scoresPerFreq.get("0"))+"\t"+computeAvg(scoresPerFreq.get("500"))+"\t"+computeAvg(scoresPerFreq.get("1200"))+"\t"+computeAvg(scoresPerFreq.get("2000")));
		
		br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Double computeAvg(List<Double> list) {

		Double sum = 0.;
		for (Double d : list){
			sum += d;
		}
		return sum/list.size();
	}

	private static void addToMap(
			Map<String, List<Double>> map, String key,
			Double value) {

		if (!map.containsKey(key)) map.put(key, new ArrayList<Double>());
		map.get(key).add(value);
	}

}
