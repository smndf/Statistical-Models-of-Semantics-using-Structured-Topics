package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class CleanTopicWords {

	/* This class can be used to remove POStags from words 
	 * (this step is normally done while searching for hypernyms)
	 */

	public static void main(String[] args) {
		String fileToClean = "/CWddt-wiki-mwe-posFiltBef2FiltAft.txt";
		String outputFile = fileToClean.substring(1,fileToClean.length()-4)+"Clean.txt";
		InputStream is = CleanTopicWords.class.getResourceAsStream(fileToClean);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		File fo = new File(outputFile);
		try {
			FileWriter fw = new FileWriter(fo);
			while((line = br.readLine()) != null){
				fw.write(cleanLine(line)+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String cleanLine(String line) {
		StringWriter sw = new StringWriter();
		String[] lineSplit = line.split("\t");
		if (lineSplit.length==6){
			if (lineSplit[5].split(",").length>0){
				for (String mwe : lineSplit[5].split(",")){
					if (mwe.split(" ").length>0){
						for (String word : mwe.split(" ")){
							sw.append(word.split("#")[0]+" ");	
						}
						sw.getBuffer().setLength(sw.getBuffer().length()-1); // remove last " "
					}
					sw.append(",");
				}
				sw.getBuffer().setLength(sw.getBuffer().length()-1); // remove last ","
			}

		}
		return lineSplit[0]+"\t"+lineSplit[1]+"\t"+lineSplit[2]+"\t"+lineSplit[3]+"\t"+lineSplit[4]+"\t"+sw.toString();
	}
	
	public static String cleanExpression(String exp){
		StringWriter sw = new StringWriter();
		for (String word : exp.split(" ")){
			sw.append(word.split("#")[0]+" ");	
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-1); // remove last " "
		return sw.toString();
	}

}
