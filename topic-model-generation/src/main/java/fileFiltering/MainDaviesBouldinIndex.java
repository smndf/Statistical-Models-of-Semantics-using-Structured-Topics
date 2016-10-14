package fileFiltering;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import graph.BuildGraph;
import graph.ResultatBuildGraph;

public class MainDaviesBouldinIndex {

	public static void main(String[] args) {
		
		CommandLineParser clParser = new BasicParser();
		Options options = new Options();
		Option clustersFileOption = Option.builder()
				.longOpt("clusters")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to clustersFile")
				.build();
		Option ddtFileOption = Option.builder()
				.longOpt("ddt")
				.numberOfArgs(1)
				.required(true)
				.type(String.class)
				.desc("path to ddt file")
				.build();

		options.addOption(ddtFileOption);
		options.addOption(clustersFileOption);
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
		String clustersFileName = cl.getOptionValue("clusters");
		String ddtFileName = cl.getOptionValue("ddt");
		ResultatBuildGraph res = null;
		try {
			res = BuildGraph.buildGraph(ddtFileName);
			FilterAfterAlgo2.filter(clustersFileName, res);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
