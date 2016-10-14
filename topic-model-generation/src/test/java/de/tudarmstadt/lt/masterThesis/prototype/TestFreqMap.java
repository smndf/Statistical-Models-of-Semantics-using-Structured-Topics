package de.tudarmstadt.lt.masterThesis.prototype;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class TestFreqMap {

	public static void main(String[] args) {

		String freqFile = "/Users/simondif/Downloads/news100M_stanford_cc_word_count";
		try {
			Map<String,Long> freqMap = frequency.FrequentWords.buildFreqMapLong(freqFile);
			Long sumFreq = Long.valueOf("0");
			for (Entry<String, Long> entry : freqMap.entrySet()){
				sumFreq += entry.getValue();
			}
			Double avFreq = sumFreq.doubleValue()/Integer.valueOf(freqMap.size()).doubleValue();
			System.out.println("sumFreq\t"+sumFreq);
			System.out.println("freqMap.size()\t"+freqMap.size());
			System.out.println("avFreq\t"+avFreq);
			long sumSquareDiff = 0;
			Long maxValue = Long.MIN_VALUE;
			String wordWithMaxFreq = "";
			for (Entry<String, Long> entry : freqMap.entrySet()){
				sumSquareDiff += (entry.getValue()-242)*(entry.getValue()-242);
				if (entry.getValue()>maxValue) {
					maxValue = entry.getValue();
					wordWithMaxFreq = entry.getKey();
				}
			}
			System.out.println("maxValue = "+maxValue+" for word "+wordWithMaxFreq);
			double avSquareDiff = Long.valueOf(sumSquareDiff).doubleValue()/Integer.valueOf(freqMap.size()).doubleValue();
			avSquareDiff = Math.sqrt(avSquareDiff);
			System.out.println("avSquareDiff\t"+avSquareDiff);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
