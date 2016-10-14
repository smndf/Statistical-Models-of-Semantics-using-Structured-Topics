package louvainmethod;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


public class LMGraphConversion {

	public void convertToLM(String inputFile) throws IOException {


		//FrequentWords fw = new FrequentWords();
		//Map<String,Integer> freqMap = fw.buildFreqMap("/news100M_stanford_cc_word_count");
		//System.out.println("map :");
		//System.out.println(mapToString(freqMap,10));
		//System.out.println("...");



		String outputFile = inputFile.split("/")[inputFile.split("/").length-1].substring(0, inputFile.length()-4) + "LM.txt";
		//InputStream is = getClass().getResourceAsStream(inputFile);
		//BufferedReader br = new BufferedReader(new InputStreamReader(is));
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		File fo = new File(outputFile);
		try{


			FileWriter fw1 = new FileWriter (fo);

			StringWriter writer = new StringWriter();
			String[] lineSplit = new String[4];
			br.readLine(); // word	cid	cluster	isas
			String line = null;
			//String[] split = null;
			int i = 0;

			Map<Integer,String> mapItoS = new HashMap<Integer,String>();
			Map<String,Integer> mapStoI = new HashMap<String,Integer>();

			String root = null;
			int linectr=0,linectr2 = 0;
			int ctr = 0;
			while ( (line = br.readLine()) != null){ 

				lineSplit = line.split("\t");

				linectr++;
				if (linectr==10000){
					linectr2 +=linectr;
					System.out.println("line " + linectr2);
					linectr=0;
				}
				if (lineSplit.length>0){	
					root = lineSplit[0];
					int rootNumber = 0;

					// node root already seen ?
					if (mapStoI.containsKey(root)){
						rootNumber = mapStoI.get(root);
					} else {
						rootNumber = i;
						mapStoI.put(root, rootNumber);
						mapItoS.put(rootNumber,root);
						i++;
					}

					if (lineSplit.length>1){

						ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[1].split(",")));
						for (String neighbourSp : neighboursSplit){
							if (neighbourSp.split(":").length>1){							
								//if (freqMap.containsKey(neighbourSp.split(":")[0].split("#")[0] + "#" + neighbourSp.split(":")[0].split("#")[1]) 
								//	&& freqMap.get(neighbourSp.split(":")[0].split("#")[0] + "#" + neighbourSp.split(":")[0].split("#")[1]) > 10){
								ctr++;
								// node neighbourSp.split(":")[0] already seen ?
								int destNumber = 0;
								if (mapStoI.containsKey(neighbourSp.split(":")[0])){
									destNumber = mapStoI.get(neighbourSp.split(":")[0]);
								} else {
									destNumber = i;
									mapStoI.put(neighbourSp.split(":")[0], destNumber);
									mapItoS.put(destNumber,neighbourSp.split(":")[0]);
									i++;
								}


								writer.write(mapStoI.get(root) + " " + mapStoI.get(neighbourSp.split(":")[0]) + " " + neighbourSp.split(":")[1]+"\n");
								//}
							}
						}
					}
				}
				if (linectr == 0){
					fw1.write (writer.toString());
					writer.getBuffer().setLength(0);
				}

			}


			// write map into file			
			String mapFile = "mapItoS" + inputFile.split("/")[inputFile.split("/").length-1];
			writeNodesNames(mapItoS,mapFile);





			fw1.write (writer.toString());

			fw1.close();
			System.out.println("fini avec " + ctr + " mots");
		}
		catch (IOException exception)
		{
			System.err.println ("Erreur lors de la lecture : " + exception.getMessage());
		}
		br.close();
	}

	private String mapToString(Map<String, Integer> freqMap, int i) {
		int it = 0;
		StringWriter writer = new StringWriter();
		for (Entry<String,Integer> entry : freqMap.entrySet()) {
			it++;
			writer.write(entry.getKey().toString());
			writer.write(" ");
			writer.write(entry.getValue().toString());
			writer.write("\n");
			if (it>i) break;
		}		
		return writer.toString();
	}

	private void writeNodesNames(Map<Integer, String> mapItoS, String mapFile) {
		File mapOut = new File(mapFile);
		try{
			FileWriter fw = new FileWriter (mapOut);
			StringWriter writer = new StringWriter();
			for (Entry<Integer,String> entry : mapItoS.entrySet()) {
				writer.write(entry.getKey().toString());
				writer.write(" ");
				writer.write(entry.getValue().toString());
				writer.write("\n");
			}

			writer.write("");
			fw.write (writer.toString());
			fw.close();
		}
		catch (IOException exception)
		{
			System.err.println ("Erreur lors de la lecture : " + exception.getMessage());
		}

	}

}
