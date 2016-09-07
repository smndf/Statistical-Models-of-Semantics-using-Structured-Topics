package classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {

	public Map<String,Integer> buildFreqMap(String fileName) throws IOException {

		System.out.print("Building frequency map...");
		InputStream is = getClass().getResourceAsStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		Map<String,Integer> freqMap = new HashMap<String,Integer>();
		br.readLine();
		String line = null;
		String[] split = null;
		while ( (line = br.readLine()) != null){ 
			if ((split = line.split("\t"))!=null && split.length>1){
				//System.out.println(split[0]);
				if (split[0].split("#").length>0){					
					String word = split[0].split("#")[0].toLowerCase();
					int currentFreq = Integer.valueOf(split[1]);
					if ((freqMap.containsKey(word) && freqMap.get(word) < currentFreq) || !freqMap.containsKey(word)){
						freqMap.put(word, Integer.valueOf(split[1]));					
					}
				}
			}
		}
		System.out.println("OK");
		return freqMap;
	}

	public String sortClusterByFrequency(Set<String> wordsSet, Map<String,Integer> freqMap){
		System.out.println("Sorting cluster words by frequency...");
		
		Map<Integer,Set<String>> wordsMap = new HashMap<Integer,Set<String>>();
		List<Integer> freqList = new ArrayList<Integer>();
		
		for (String s : wordsSet){
			
			if (freqMap == null){
				System.out.println("freqmap null");
			}
			
			if (freqMap.containsKey(s)){				
				int score = freqMap.get(s);
				if (wordsMap.containsKey(score)){
					wordsMap.get(score).add(s);
				} else {
					Set<String> set = new HashSet<String>();
					set.add(s);
					wordsMap.put(score,set);
				}
				freqList.add(score);
			}
		}

		Collections.sort(freqList);
		Collections.reverse(freqList);
		
		StringWriter sw = new StringWriter();
		for (Integer score : freqList){
			sw.append(toString(wordsMap.get(score)));
		}
		sw.getBuffer().setLength(sw.toString().length()-2);
		System.out.println("OK");
		return sw.toString();
	}

	private String toString(Set<String> set) {
		StringWriter sw = new StringWriter();
		for (String word : set) {
			sw.append(word+ ", ");
		}
		return sw.toString();
	}

}
