package louvainmethod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ToTwoColumnFormat {

	public static void main(String[] args) {

		if (args.length!=1) {
			System.err.println("One argument needed: the path to the file");
			System.exit(1);
		}
		String fileName = args[0];
		try {
			String outputFile = fileName.substring(0,fileName.length()-4)+"OK.csv";
			FileWriter fw = new FileWriter(outputFile);
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = br.readLine())!= null){
				//System.out.println(line.split("\t").length);
				if (line.split("\t").length > 5){
					fw.write(line.split("\t")[0]+"\t"+line.split("\t")[5]+"\n");
				}
			}
			br.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		
	}

}
