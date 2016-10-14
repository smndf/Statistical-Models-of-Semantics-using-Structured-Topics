package evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeIsasGraphResults {

	public static void main(String[] args) {

		try {
		File f = new File("/Users/simondif/Documents/workspace/structured-topics/isasGraphResultsAllFiles.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		Map<String,List<Double>> ratioNonZeroElPerAlgo = new HashMap<String,List<Double>>();
		Map<String,List<Double>> ratioNonZeroElPerDDT = new HashMap<String,List<Double>>();
		Map<String,List<Double>> ratioNonZeroElPerFreq = new HashMap<String,List<Double>>();
		
		Map<String,List<Double>> avgScoreNonZeroElPerAlgo = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreNonZeroElPerDDT = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreNonZeroElPerFreq = new HashMap<String,List<Double>>();
		
		Map<String,List<Double>> avgScoreAllElPerAlgo = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreAllElPerDDT = new HashMap<String,List<Double>>();
		Map<String,List<Double>> avgScoreAllElPerFreq = new HashMap<String,List<Double>>();

		while ((line=br.readLine())!=null){
				
			String[] split = line.split("\t");
				
				String algo = split[0];
				String ddt = split[1];
				String freq = split[2];
				//System.out.println(line);
				Double ratioNonZeroEl = Double.valueOf(split[3]);
				Double avgScoreNonZeroEl = Double.valueOf(split[4]);
				Double avgScoreAllEl = Double.valueOf(split[5]);
				
				addToMap(ratioNonZeroElPerAlgo, algo, ratioNonZeroEl);
				addToMap(avgScoreNonZeroElPerAlgo, algo, avgScoreNonZeroEl);
				addToMap(avgScoreAllElPerAlgo, algo, avgScoreAllEl);
				
				addToMap(ratioNonZeroElPerDDT, ddt, ratioNonZeroEl);
				addToMap(avgScoreNonZeroElPerDDT, ddt, avgScoreNonZeroEl);
				addToMap(avgScoreAllElPerDDT, ddt, avgScoreAllEl);
				
				addToMap(ratioNonZeroElPerFreq, freq, ratioNonZeroEl);
				addToMap(avgScoreNonZeroElPerFreq, freq, avgScoreNonZeroEl);
				addToMap(avgScoreAllElPerFreq, freq, avgScoreAllEl);
			}
		
		System.out.println("per algo:\tCW\tLM");
		System.out.println("avg ratioNonZeroEl\t"+computeAvg(ratioNonZeroElPerAlgo.get("CW"))+"\t"+computeAvg(ratioNonZeroElPerAlgo.get("LM")));
		System.out.println("avg avgScoreNonZeroEl\t"+computeAvg(avgScoreNonZeroElPerAlgo.get("CW"))+"\t"+computeAvg(avgScoreNonZeroElPerAlgo.get("LM")));
		System.out.println("avg avgScoreAllEl\t"+computeAvg(avgScoreAllElPerAlgo.get("CW"))+"\t"+computeAvg(avgScoreAllElPerAlgo.get("LM")));

		System.out.println("\n\nper DDT:\tnews200\tnews50\twiki200\twiki30");
		System.out.println("avg ratioNonZeroEl\t"+computeAvg(ratioNonZeroElPerDDT.get("news200"))+"\t"+computeAvg(ratioNonZeroElPerDDT.get("news50"))+"\t"+computeAvg(ratioNonZeroElPerDDT.get("wiki200"))+"\t"+computeAvg(ratioNonZeroElPerDDT.get("wiki30")));
		System.out.println("avg avgScoreNonZeroEl\t"+computeAvg(avgScoreNonZeroElPerDDT.get("news200"))+"\t"+computeAvg(avgScoreNonZeroElPerDDT.get("news50"))+"\t"+computeAvg(avgScoreNonZeroElPerDDT.get("wiki200"))+"\t"+computeAvg(avgScoreNonZeroElPerDDT.get("wiki30")));
		System.out.println("avg avgScoreAllEl\t"+computeAvg(avgScoreAllElPerDDT.get("news200"))+"\t"+computeAvg(avgScoreAllElPerDDT.get("news50"))+"\t"+computeAvg(avgScoreAllElPerDDT.get("wiki200"))+"\t"+computeAvg(avgScoreAllElPerDDT.get("wiki30")));

		System.out.println("\n\nper freq:\t0\t500\t1200\t2000");
		System.out.println("avg ratioNonZeroEl\t"+computeAvg(ratioNonZeroElPerFreq.get("0"))+"\t"+computeAvg(ratioNonZeroElPerFreq.get("500"))+"\t"+computeAvg(ratioNonZeroElPerFreq.get("1200"))+"\t"+computeAvg(ratioNonZeroElPerFreq.get("2000")));
		System.out.println("avg avgScoreNonZeroEl\t"+computeAvg(avgScoreNonZeroElPerFreq.get("0"))+"\t"+computeAvg(avgScoreNonZeroElPerFreq.get("500"))+"\t"+computeAvg(avgScoreNonZeroElPerFreq.get("1200"))+"\t"+computeAvg(avgScoreNonZeroElPerFreq.get("2000")));
		System.out.println("avg avgScoreAllEl\t"+computeAvg(avgScoreAllElPerFreq.get("0"))+"\t"+computeAvg(avgScoreAllElPerFreq.get("500"))+"\t"+computeAvg(avgScoreAllElPerFreq.get("1200"))+"\t"+computeAvg(avgScoreAllElPerFreq.get("2000")));

		
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
