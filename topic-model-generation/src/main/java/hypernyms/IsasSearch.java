package hypernyms;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IsasSearch {

	public void addISAS(String inputFile, String isasFileStem) throws IOException{
		String outputFile = inputFile.substring(0, inputFile.length()-4) + "IsasCommoncrawl.csv";
		outputFile = outputFile.split("/")[outputFile.split("/").length-1];
		System.out.println("output = "+outputFile);
		BufferedReader clusters = getBufferedReaderForFile(inputFile,true);
		Set<String> cluster = new HashSet<String>();
		int clusterNo = 0;
		int i = 0;
		File fo = new File(outputFile);
		FileWriter fw = new FileWriter (fo);
		String line;
		while ( (line = clusters.readLine()) != null){
			System.out.println("new line " + i++);
			cluster.clear();
			clusterNo = Integer.valueOf(line.split("\t")[0]);
			for (String word : line.split("\t")[line.split("\t").length-1].split(",")){
				cluster.add(word);//clean(word));
			}
			TwoLists<String> twoLists = searchISAS(cluster, isasFileStem);
			fw.write(clusterNo + "\t" + toString(twoLists.getL1()) + "\t" + toString(twoLists.getL2()) + "\t" + line.split("\t")[line.split("\t").length-1] + "\n");
			fw.flush();
		}
		fw.close();
	}

	private String clean(String word) {
		if (word.split("#").length >0){
			word = word.split("#")[0];
		}
		return word;
	}

	private String toString(List<String> hypernyms) {
		StringWriter sw = new StringWriter();
		for (String s : hypernyms){
			sw.append(s+", ");
		}
		return sw.toString().substring(0, sw.getBuffer().length()-2);
	}


	public TwoLists searchISAS(Set<String> cluster, String isasFileStem){

		Map<String,Integer> hMap1 = new HashMap<String,Integer>();
		TreeMap<Integer,Set<String>> hypRankMap1 = new TreeMap<Integer,Set<String>>();
		Map<String,Integer> hMap2 = new HashMap<String,Integer>();
		TreeMap<Integer,Set<String>> hypRankMap2 = new TreeMap<Integer,Set<String>>();

		String line = null;
		List<String> list1 = null;
		List<String> list2 = null;
		try {

			Set<String> isas = new HashSet<String>();
			String isasFile;
			for (String word : cluster){
				if (word.length()>0){
					isasFile = isasFileStem + "-" + word.toLowerCase().charAt(0) + ".csv";
					try{
						BufferedReader isasText = getBufferedReaderForFile(isasFile,true);
						//word = word.split("#")[0].toLowerCase();
						//System.out.println("\n"+word.toString());

						String letter = word.substring(0, 1);
						while ( (line = isasText.readLine()) != null){ 
							//System.out.println(line.split("\t")[1].toString());
							if (line.split("\t")[0].toLowerCase().equals(word)){
								String hyp = line.split("\t")[1];
								isas.add(hyp);
								addToMaps(hyp, hMap1, hypRankMap1, 1);
								addToMaps(hyp, hMap2, hypRankMap2, Integer.valueOf(line.split("\t")[2]));
								//System.out.println(line.split("\t")[1].toString());
							}
						}
					}catch (FileNotFoundException e) {
						System.out.println("no file "+isasFile);
					}
				}
			}
			list1 = findBestHypernyms(hypRankMap1,isas.size());
			list2 = findBestHypernyms(hypRankMap2,isas.size());			
			//System.out.println(isas.toString());
			if (list1.isEmpty()){
				list1.add("NO HYPERNYMS");
			}
			if (list2.isEmpty()){
				list2.add("NO HYPERNYMS");
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		return new TwoLists<String>(list1,list2) ;
	}
	
	public static List<IsA> searchIsA(String isasFileStem,String word) throws NumberFormatException, IOException{
		if (word.length()<2) return new ArrayList<IsA>();
		String isasFile = isasFileStem + "-" + word.substring(0,2) + ".csv";
		List<IsA> isas = new ArrayList<IsA>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(isasFile));
			//word = word.split("#")[0].toLowerCase();
			//System.out.println("\n"+word.toString());
			String line;
			while ( (line = br.readLine()) != null){ 
				//System.out.println(line.split("\t")[1].toString());
				if (line.split("\t")[0].toLowerCase().equals(word)){
					String hyp = line.split("\t")[1];
					Integer weight = Integer.valueOf(line.split("\t")[2]);
					isas.add(new IsA(hyp,weight));
					//System.out.println(line.split("\t")[1].toString());
				}
			}
			br.close();
		}catch (FileNotFoundException e) {
			System.out.println("no file "+isasFile);
		}
		return isas;
	}

	public void addToMaps(String hyp, Map<String, Integer> hMap,
			Map<Integer, Set<String>> hypRankMap, Integer weight) {

		int oldWeight = 0,newWeight = 0;
		if (hMap.containsKey(hyp)){
			oldWeight = hMap.get(hyp);
			newWeight = oldWeight + weight;
			//System.out.println("hyp("+hyp.getWord()+",d="+hyp.depth+")");
			//System.out.println("remove "+hyp.getWord()+" from hypRankMap "+hMap.get(hyp));
			hypRankMap.get(oldWeight).remove(hyp);
			if (!hypRankMap.containsKey(newWeight)){
				hypRankMap.put(newWeight,new HashSet<String>());
			}
			//System.out.println("add "+hyp.getWord()+" to hypRankMap "+hMap.get(hyp)+1);
			hypRankMap.get(newWeight).add(hyp);						
			//System.out.println("update hMap for "+hyp.getWord()+" : "+hMap.get(hyp)+" > "+hMap.get(hyp)+1);
			hMap.put(hyp,newWeight);
		} else {
			hMap.put(hyp,weight);
			//System.out.println("add "+hyp.getWord()+",1 to hMap ");
			if (!hypRankMap.containsKey(weight)){
				hypRankMap.put(weight,new HashSet<String>());
			}
			hypRankMap.get(weight).add(hyp);
			//System.out.println("add 1,"+hyp.getWord()+" to hypRankMap ");
		}


	}
	
	private List<String> findBestHypernyms(TreeMap<Integer,Set<String>> hypRankMap, int clusterSize) {

		final int SIZE = 10;
		Map<String,Integer> map = new HashMap<String,Integer>();
		List<String> listIsas = new ArrayList<String>();
		while (map.size()<=SIZE && !hypRankMap.isEmpty()){
			for (String s : hypRankMap.get(hypRankMap.lastKey())){
				if (map.size()<=SIZE){
					map.put(s,hypRankMap.lastKey());
					listIsas.add(s+"#"+hypRankMap.lastKey());
				}
			}
			hypRankMap.remove(hypRankMap.lastKey());
		}
		return listIsas;
	}

	private BufferedReader getBufferedReaderForFile(String file, Boolean externFiles) throws FileNotFoundException {

		BufferedReader br;
		if (externFiles){
			br= new BufferedReader(new FileReader(file));			
		} else {			
			InputStream is2 = getClass().getResourceAsStream("/"+file);
			br = new BufferedReader(new InputStreamReader(is2));
		}
		return br;
	}

	public class TwoMaps<T>{
		private Map<T,Integer> map1;
		private Map<T,Integer> map2;
		public TwoMaps(Map<T, Integer> map1, Map<T, Integer> map2) {
			super();
			this.map1 = map1;
			this.map2 = map2;
		}
		public Map<T, Integer> getMap1() {
			return map1;
		}
		public void setMap1(Map<T, Integer> map1) {
			this.map1 = map1;
		}
		public Map<T, Integer> getMap2() {
			return map2;
		}
		public void setMap2(Map<T, Integer> map2) {
			this.map2 = map2;
		}
	}
	
	public class TwoLists<T>{
		private List<T> l1;
		private List<T> l2;
		public TwoLists(List<T> l1, List<T> l2) {
			super();
			this.l1 = l1;
			this.l2 = l2;
		}
		public List<T> getL1() {
			return l1;
		}
		public void setL1(List<T> l1) {
			this.l1 = l1;
		}
		public List<T> getL2() {
			return l2;
		}
		public void setL2(List<T> l2) {
			this.l2 = l2;
		}
	}

}
