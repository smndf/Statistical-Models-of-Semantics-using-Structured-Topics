package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HandleHighlightsElasticSearch {

	public static void main(String[] args) {

		String highlightEx = "content [content], fragments[[, <em>fish</em>, , <em>salmon</em>, , <em>whitefish</em>, -<em>2</em>, -<em>fish</em>,  <em>fish</em>, , <em>Salmon</em>, <em>whitefish</em>,, <em>Char</em>,  <em>fish</em>, , <em>Salmo</em>]]";
		List<String> fragmentsList = new ArrayList<String>();
		Map<String,Integer> map = new HashMap<String,Integer>();
		if (highlightEx.contains("<em>")){
			for (String s : highlightEx.split("<em>")){
				if (s.contains("</em>")){
					s = s.split("</em>")[0];
					fragmentsList.add(s);
				}
			}
		}
		for (String fragment : fragmentsList){
			if (map.containsKey(fragment)){
				map.put(fragment, map.get(fragment)+1);
			} else {
				map.put(fragment, 1);
			}
		}
		map = MapUtil.sortMapByValue(map);
		for (Entry<String, Integer> entry : map.entrySet()){
			System.out.println(entry.getKey()+" : "+entry.getValue());
		}
	}

}
