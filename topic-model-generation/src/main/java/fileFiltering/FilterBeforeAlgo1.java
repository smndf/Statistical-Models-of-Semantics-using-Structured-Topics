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

/* transform inputFile for each line :
 * word	cid	cluster => word#cid	cluster
 */


public class FilterBeforeAlgo1 {

	public void filter(String inputFile) throws FileNotFoundException {

		String outputFile = inputFile.substring(0, inputFile.length() - 4) + "FiltBef1.txt";
		File fo = new File(outputFile);

		//InputStream is = getClass().getResourceAsStream(inputFile);
		BufferedReader input = new BufferedReader(new FileReader(inputFile));

		String[] lineSplit = new String[3];
		String line = null;

		try {

			FileWriter fw = new FileWriter (fo);
			StringWriter sw = new StringWriter();

			//String pos = "";
			while ( (line = input.readLine()) != null){ 
				lineSplit = line.split("\t");
				if (lineSplit.length>2){
					if (lineSplit[0].split(",").length==1 && lineSplit[1].split(":").length==1){
						sw.write(lineSplit[0] + "#" + lineSplit[1] + "\t"); 
						for (String s : lineSplit[2].split(",")){
							if (s.split(":").length==2 && isFloat(s.split(":")[1])){
								sw.write(s + ","); 
							}
						}
						sw.getBuffer().setLength(sw.getBuffer().length()-1);
						sw.write("\n");
						fw.write (sw.toString());
						sw.getBuffer().setLength(0);
					}
				}
			}
			fw.close();
			sw.close();
			System.out.println("file modified, new file : " + outputFile);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private boolean isFloat(String string) {
		try {
			Float.parseFloat(string);
			return true;
		}
		catch (NumberFormatException ex) {
			return false;
		}
	}
	
}

