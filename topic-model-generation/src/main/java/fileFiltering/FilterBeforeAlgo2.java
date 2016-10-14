package fileFiltering;

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
import java.util.Arrays;
import java.util.Map;

/* transform inputFile for each line :
 * word	cid	cluster => word#cid	cluster
 * filter words with frequency dictionary + keeps NN NP JJ RB only
 */

public class FilterBeforeAlgo2 {

	public void filter(String inputFile, Map<String, Integer> freqMap, int freqThreshold, boolean mwes) throws FileNotFoundException {

		String outputFile = inputFile.substring(0, inputFile.length() - 4) + "FiltBef2.txt";
		File fo = new File(outputFile);

		BufferedReader input = new BufferedReader(new FileReader(inputFile));

		String[] lineSplit = new String[3];
		String line = null;
		try {

			FileWriter fw = new FileWriter (fo);
			StringWriter sw = new StringWriter();
			line = input.readLine();
			sw.write(line.split("\t")[0] + "\t" + line.split("\t")[2] + "\n");
			if (!mwes){	// DO WE DEAL WITH A FILE INVOLVING NO MULTI WORD EXPRESSIONS ?
				String pos = "";
				int nbWords = 0;
				while ( (line = input.readLine()) != null){ 
					lineSplit = line.split("\t");
					if (lineSplit[0].split("#").length>1){
						pos = lineSplit[0].split("#")[1]; 
						if (pos.equals("NN") || pos.equals("NP") /* || pos.equals("JJ") || pos.equals("RB")*/){

							String word;
							if (lineSplit.length>2){
								if (lineSplit[0].split(",").length==1 && lineSplit[1].split(":").length==1){
									word = lineSplit[0] + "#" + lineSplit[1];
									sw.write( word + "\t");

									nbWords = 0;
									ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[2].split(",")));
									for (String neighbour : neighboursSplit){
										if (neighbour.split("#").length>1){
											pos = neighbour.split("#")[1];		
											if (pos.equals("NN") || pos.equals("NP")/* || pos.equals("JJ") || pos.equals("RB")*/){	
												String term = neighbour.split("#")[0] + "#" + neighbour.split("#")[1];
												if (freqMap.containsKey(term) && freqMap.get(term) > freqThreshold){
													sw.write(neighbour + ",");
													nbWords++;
												}
											}
										}
									}
									if (nbWords>0){
										//System.out.println(nbWords);
										sw.getBuffer().setLength(sw.toString().length() - 1);
										sw.write("\n");
										fw.write (sw.toString());
									}
									sw.getBuffer().setLength(0);								
								}
							}
						}
					}
				}

			} else {
				String pos = "";
				int nbWords = 0;
				while ( (line = input.readLine()) != null){ 
					lineSplit = line.split("\t");
					//if (lineSplit[0].split("#").length>1){
					if (containsNounTag(lineSplit[0])){
						//pos = lineSplit[0].split("#")[1]; 
						//if (pos.equals("NN") || pos.equals("NP") /* || pos.equals("JJ") || pos.equals("RB")*/){

						String mwe;
						if (lineSplit.length>2){
							if (lineSplit[0].split(",").length==1 && lineSplit[1].split(":").length==1){
								mwe = lineSplit[0] + "#" + lineSplit[1];
								sw.write( mwe + "\t");

								nbWords = 0;
								ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[2].split(",")));
								for (String neighbour : neighboursSplit){
									if (neighbour.split("#").length>1){
										pos = neighbour.split("#")[1];		
										if (containsNounTag(neighbour)){	
											/*String term = neighbour.split("#")[0] + "#" + neighbour.split("#")[1];
											if (freqMap.containsKey(term) && freqMap.get(term) > freqThreshold){
											}*/
											sw.write(neighbour + ",");
											nbWords++;
										}
									}
								}
								if (nbWords>0){
									//System.out.println(nbWords);
									sw.getBuffer().setLength(sw.toString().length() - 1);
									sw.write("\n");
									fw.write (sw.toString());
								}
								sw.getBuffer().setLength(0);								
							}
						}
					}
					//}
				}
			}
			fw.close();
			sw.close();
			System.out.println("file filtered, new file : " + outputFile);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private boolean containsNounTag(String mwe) {
		Boolean result = false;
		for (String part : mwe.split(" ")){
			if (part.split("#").length>1 && part.split("#")[1].equals("NOUN")){
				result = true;
			}
		}
		return result;
	}
}
