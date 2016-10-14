package frequency;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FrequentWords {

	public static Map<String,Integer> buildFreqMap(String fileName) throws IOException {


		System.out.print("Building frequency map...");
		//InputStream is = FrequentWords.class.getResourceAsStream(fileName);
		//BufferedReader br = new BufferedReader(new InputStreamReader(is));
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		Set<String> possiblePOStags = new HashSet<String>();
		for (String tag : "NN	NP	VB	CD	JJ	JJR	RB".split("\t")){
			possiblePOStags.add(tag);
		}
		String[] lineSplit = new String[4];
		Map<String,Integer> freqMap = new HashMap<String,Integer>();
		br.readLine(); 
		String line = null;
		String[] split = null;
		int i = 0;
		int linectr=0,linectr2 = 0;
		while ( (line = br.readLine()) != null){ 
			/* There are mistaken tags e.g. NN instead of JJ
			 * we keep only the most frequent tag for each word and discard others
			 */
			if ((split = line.split("\t"))!=null && split.length>1 && split[0].split("#").length>0){
				String word = split[0].split("#")[0];
				String currentTag = split[0].split("#")[split[0].split("#").length-1];
				int currentFreq = Integer.valueOf(split[1]);
				boolean toBeKept = true;
				for (String tag : possiblePOStags){
					if (freqMap.containsKey(word+"#"+tag)){
						if (freqMap.get(word+"#"+tag) > currentFreq){
							toBeKept = false;
						}
						else {
							freqMap.remove(word+"#"+tag);
						}
					}
				}
				if (toBeKept){
					freqMap.put(split[0], Integer.valueOf(split[1]));					
				}
			}
		}
		br.close();
		System.out.println("OK");
		return freqMap;
	}
	
	public static Map<String,Long> buildFreqMapLong(String fileName) throws IOException {


		System.out.print("Building frequency map...");
		//InputStream is = FrequentWords.class.getResourceAsStream(fileName);
		//BufferedReader br = new BufferedReader(new InputStreamReader(is));
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		Set<String> possiblePOStags = new HashSet<String>();
		for (String tag : "NN	NP	VB	CD	JJ	JJR	RB".split("\t")){
			possiblePOStags.add(tag);
		}
		String[] lineSplit = new String[4];
		Map<String,Long> freqMap = new HashMap<String,Long>();
		br.readLine(); 
		String line = null;
		String[] split = null;
		int i = 0;
		int linectr=0,linectr2 = 0;
		while ( (line = br.readLine()) != null){ 
			/* There are mistaken tags e.g. NN instead of JJ
			 * we keep only the most frequent tag for each word and discard others
			 */
			if ((split = line.split("\t"))!=null && split.length>1 && split[0].split("#").length>0){
				String word = split[0].split("#")[0];
				String currentTag = split[0].split("#")[split[0].split("#").length-1];
				Long currentFreq = Long.valueOf(split[1]);
				boolean toBeKept = true;
				for (String tag : possiblePOStags){
					if (freqMap.containsKey(word+"#"+tag)){
						if (freqMap.get(word+"#"+tag) > currentFreq){
							toBeKept = false;
						}
						else {
							freqMap.remove(word+"#"+tag);
						}
					}
				}
				if (toBeKept){
					freqMap.put(split[0], Long.valueOf(split[1]));					
				}
			}
		}
		br.close();
		System.out.println("OK");
		return freqMap;
	}
}
