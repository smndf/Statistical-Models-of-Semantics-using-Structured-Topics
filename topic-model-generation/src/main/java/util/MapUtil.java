package util;

import graph.ClusterMetrics.TrianglesAndTriplets;
import graph.ResultatBuildGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MapUtil {
	
	static Logger log = LogManager.getLogger("de.tudarmstadt.lt.util");
	
	public static Map<String, String> readMapFromReader(BufferedReader reader, String delimiter) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split(delimiter);
			if (parts.length == 2) {
				map.put(parts[0], parts[1]);
			} else {
				log.warn("MapUtil.readMapFromFile: column count != 2: " + line);
			}
		}
		
		reader.close();
		return map;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ){
	    List<Map.Entry<K, V>> list = new LinkedList<Entry<K, V>>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>(){
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ) {
	            return -( o1.getValue() ).compareTo( o2.getValue() );
	        }
	    } );
	    Map<K, V> result = new LinkedHashMap<K, V>();
	    for (Map.Entry<K, V> entry : list) {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    return result;
	}
	
	public static Map<String, String> readMapFromFile(String fileName, String delimiter) throws IOException {
		BufferedReader reader = new BufferedReader(new MonitoredFileReader(fileName));
		
		return readMapFromReader(reader, delimiter);
	}

	public static Set<String> readSetFromReader(BufferedReader reader) throws IOException {
		Set<String> set = new HashSet<String>();
		
		String line;
		while ((line = reader.readLine()) != null) {
			set.add(line);
		}
		
		reader.close();
		return set;
	}

	public static Set<String> readSetFromFile(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new MonitoredFileReader(fileName));
		
		return MapUtil.readSetFromReader(reader);
	}
		
	
	public static <K, V extends Comparable<V>> Map<K, V> sortMapByValue(Map<K, V> map) {
		ValueComparatorNoEquals<K, V> vc = new ValueComparatorNoEquals<K, V>(map);
		Map<K, V> sortedMap = new TreeMap<K, V>(vc);
		sortedMap.putAll(map);
		return sortedMap;
	}
	
	public static <K, V extends Comparable<V>> List<K> sortMapKeysByValue(Map<K, V> map) {
		ValueComparator<K, V> vc = new ValueComparator<K, V>(map);
		List<K> sortedKeys = new LinkedList<K>(map.keySet());
		Collections.sort(sortedKeys, vc);
		return sortedKeys;
	}
	
	static class ValueComparatorNoEquals<K, V extends Comparable<V>> implements Comparator<K> {
	    Map<K, V> base;
	    public ValueComparatorNoEquals(Map<K, V> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(K a, K b) {
	        if (base.get(a).compareTo(base.get(b)) > 0) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
	static class ValueComparator<K, V extends Comparable<V>> implements Comparator<K> {
	    Map<K, V> base;
	    public ValueComparator(Map<K, V> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(K a, K b) {
	        return -base.get(a).compareTo(base.get(b));
	    }
	}
	
	/**
	 * Writes out a map to a UTF-8 file in tsv (tab-separated value) format.
	 * @param map Map to write out
	 * @param nbTriangles 
	 * @param out File to write map out to
	 * @throws IOException 
	 */
	public static void writeMap(Map<?,?> map, Writer writer, String keyValSep, String entrySep) throws IOException
	{
		for (Entry<?, ?> entry : map.entrySet()) {
			writer.write(entry.getKey().toString());
			writer.write(keyValSep);
			writer.write(entry.getValue().toString());
			writer.write(entrySep);
		}
	}
	
	
	public static void writeMap(Map<?,?> map, Map<Integer, String> nodesMapItoS, Writer writer, String keyValSep, String entrySep) throws IOException
	{
		for (Entry<?, ?> entry : map.entrySet()) {
			writer.write(entry.getKey().toString());
			writer.write(keyValSep);
			for (Integer node : (Set<Integer>)entry.getValue()){
				writer.write(nodesMapItoS.get(node).toString() + ",");
			}
			writer.write(entrySep);
		}
	}
	
	public static void writeMap2(Map<Integer, Set<Integer>> map, TrianglesAndTriplets trianglesTriplets, Map<Integer,String> nodesMapItoS , Map<Integer, Integer> eigenvectorCentralityScores, Writer writer, String keyValSep, String entrySep) throws IOException
	{
		for (Entry<Integer, Set<Integer>> entry : map.entrySet()) {
			writer.write(nodesMapItoS.get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(trianglesTriplets.getTriangles().get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(trianglesTriplets.getTriplets().get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(trianglesTriplets.clusteringCoefficient(entry.getKey()).toString());
			writer.write(keyValSep);
			//System.out.println(entry.getValue());
			//System.out.println(nodesMapItoS.get(entry.getValue()));
			for (Integer node : entry.getValue()){
				writer.write(nodesMapItoS.get(node).toString()+ ":" + eigenvectorCentralityScores.get(node) + ",");
			}
			writer.write(entrySep);
		}
	}
	
	public static void writeMap(Map<?,?> map, String out) throws IOException
	{
		Writer writer = FileUtil.createWriter(out);
		writeMap(map, writer, "\t", "\n");
		writer.close();
	}
	
	public static String toString(Map<Integer, Set<Integer>> map, TrianglesAndTriplets nbTriangles, Map<Integer, String> nodesMapItoS, Map<Integer, Integer> eigenvectorCentralityScores, String keyValSep, String entrySep) {
		StringWriter writer = new StringWriter();
		try {
			writeMap2(map, nbTriangles, nodesMapItoS, eigenvectorCentralityScores, writer, keyValSep, entrySep);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
	
	public static String toString(Map<Integer, List<Integer>> centralityScoresSorted, TrianglesAndTriplets trianglesTriplets, Map<Integer,String> nodesMapItoS, String keyValSep, String entrySep) {
		StringWriter writer = new StringWriter();
		try {
			writeMap3(centralityScoresSorted, trianglesTriplets, nodesMapItoS, writer, keyValSep, entrySep);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}
	

	public static String toString(ResultatBuildGraph graph,
			Map<Integer, Set<Integer>> clusters, String keyValSep, String entrySep) {
		StringWriter writer = new StringWriter();
		Integer noCluster = 0;
		for (Entry<Integer, Set<Integer>> entry : clusters.entrySet()) {
			writer.write(noCluster.toString());
			writer.write(keyValSep);
			writer.write(String.valueOf(entry.getValue().size()));
			writer.write(keyValSep);
			for (Integer node : entry.getValue()){
				String word = graph.getNodesMapItoS().get(node).toString();
				if (word.split("#").length==3) {
					word = word.split("#")[0]+"#"+word.split("#")[2];
					writer.write(word+ ", ");
				}
			}
			writer.write(entrySep);
			noCluster++;
		}
		writer.getBuffer().setLength(writer.getBuffer().length()-entrySep.length()-1);
		return writer.toString();
	}
	
	private static void writeMap3(Map<Integer, List<Integer>> centralityScoresSorted,
			TrianglesAndTriplets trianglesTriplets,
			Map<Integer, String> nodesMapItoS, StringWriter writer,
			String keyValSep, String entrySep) throws IOException{
		for (Entry<Integer, List<Integer>> entry : centralityScoresSorted.entrySet()) {
			writer.write(nodesMapItoS.get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(String.valueOf(entry.getValue().size()));
			writer.write(keyValSep);
			writer.write(trianglesTriplets.getTriangles().get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(trianglesTriplets.getTriplets().get(entry.getKey()).toString());
			writer.write(keyValSep);
			writer.write(trianglesTriplets.clusteringCoefficient(entry.getKey()).toString());
			writer.write(keyValSep);
			//System.out.println(entry.getValue());
			//System.out.println(nodesMapItoS.get(entry.getValue()));
			int size = 0;
			for (Integer node : entry.getValue()){
				System.out.print(node+" ");
				writer.write(nodesMapItoS.get(node).toString()+ ",");
				size++;
			}
			writer.getBuffer().setLength(writer.getBuffer().length()-2);
			writer.write(entrySep);
		}
	}

	/**
	 * Gets the value of <code>key</code> in <code>map</code>, creating
	 * a new instance of <code>valueClass</code> as value of <code>key</code> if
	 * it does not exist yet.
	 * 
	 * @return The value of <code>key</code>
	 */
	@SuppressWarnings("unchecked")
	public static <A, B> B getOrCreate(Map<A, B> map, A key, Class<?> valueClass) {
		B value = map.get(key);
		if (value == null) {
			try {
				value = (B)valueClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			map.put(key, value);
		}
		return value;
	}
	
	/**
	 * Adds <code>element</code> to the value-collection of <code>key</code> in <code>map</code>.
	 * If <code>key</code> does not exist in <code>map</code> yet, a new collection of type
	 * <code>collectionClass</code> will be instantiated, inserted in <code>map</code> with
	 * the specified <code>key</code>, and <code>element</code> will be added to it.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <N, E, C extends Collection> void addTo(Map<N, C> map, N key, E element, Class<? extends Collection> collectionClass) {
		C collection = map.get(key);
		if (collection == null) {
			try {
				collection = (C)collectionClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			map.put(key, collection);
		}
		collection.add(element);
	}
	
	/**
	 * Adds <code>num</code> to the value belonging to <code>key</code> in
	 * <code>map</code>, instantiating the value with 0 if it <code>map</code>
	 * does not contain <code>key</code> yet.
	 */
	public static <T> void addIntTo(Map<T, Integer> map, T key, int num) {
		Integer val = map.get(key);
		if (val == null) {
			val = 0;
		}
		val += num;
		map.put(key, val);
	}
	
	/**
	 * Adds <code>num</code> to the value belonging to <code>key</code> in
	 * <code>map</code>, instantiating the value with 0 if it <code>map</code>
	 * does not contain <code>key</code> yet.
	 */
	public static <T> void addFloatTo(Map<T, Float> map, T key, float num) {
		Float val = map.get(key);
		if (val == null) {
			val = 0.0f;
		}
		val += num;
		map.put(key, val);
	}
	
	public static <T> void incrementMapForKey(Map<T, Integer> map,
			T key) {
		if (!map.containsKey(key)){
			map.put(key, 1);
		} else {
			map.put(key, map.get(key)+1);
		}
	}

}