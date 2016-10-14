package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortTextFile {

	public static void main(String[] args) throws Exception {

		if (args.length==1){
			String inputFile = args[0];
			String outputFile = inputFile.substring(0, inputFile.length()-4) + "Sorted.csv";
			FileReader fileReader = new FileReader(inputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String inputLine;
			List<String> lineList = new ArrayList<String>();
			while ((inputLine = bufferedReader.readLine()) != null) {
				lineList.add(inputLine);
			}
			fileReader.close();

			Collections.sort(lineList);

			FileWriter fileWriter = new FileWriter(outputFile);
			PrintWriter out = new PrintWriter(fileWriter);
			for (String outputLine : lineList) {
				out.println(outputLine);
			}
			out.flush();
			out.close();
			fileWriter.close();
		} else {
			System.out.println("need input file as argument");
		}

	}
}

