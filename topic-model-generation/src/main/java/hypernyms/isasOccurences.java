package hypernyms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import util.MapUtil;

public class isasOccurences {

	public static void main(String[] args) {

		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option isasOption = Option.builder()
				.longOpt("isasFile")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("isasFile")
				.build();
		options.addOption(isasOption);
		CommandLine cl = null;
		boolean success = false;
		try {
			cl = clParser.parse(options, args);
			success = true;
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		if (!success) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar ***.jar", options, true);
			System.exit(1);
		}		
		String isasFile = cl.getOptionValue("isasFile");
		Map<String,Integer> isas = new HashMap<String, Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(isasFile));
			String line = null;
			while ((line=br.readLine()) != null){
				String[] split = line.split("\t");
				if (split.length==3){
					String isa = split[1];
					if (!isas.containsKey(isa)) isas.put(isa, 1);
					else isas.put(isa, isas.get(isa)+1);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		isas = MapUtil.sortMapByValue(isas);
		try {
			FileWriter fw = new FileWriter(isasFile.substring(0,isasFile.length()-4)+"Dist.csv");
			for (Map.Entry<String, Integer> entry : isas.entrySet()){
				fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
