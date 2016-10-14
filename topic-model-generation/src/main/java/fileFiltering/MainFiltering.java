package fileFiltering;

import frequency.FrequentWords;
import graph.BuildGraph;
import graph.ResultatBuildGraph;

import java.io.IOException;
import java.util.Map;

import org.openide.util.io.NbMarshalledObject;

import reader.ReaderCW;


public class MainFiltering 
{
	public static void main( String[] args ) throws IOException
	{
		if (args.length<4 || args.length>5){
			System.out.println("Use:\n - 1st argument (mandatory) = Type of filter\n - 2nd argument (mandatory) = mwe/swe?\n - 3rd argument (mandatory) = InputFileName\n - 4th argument (mandatory) = BaseFileName\n - 5th argument (facultative) = max degree of nodes (default infinite)");
		} else {
			Map<String,Integer> freqMap ;
			String fileName = args[2];
			Boolean mwes = false;
			if (args[1].equals("mwe")) mwes = true;
			switch (args[0]) {
			case "fb1":  
				FilterBeforeAlgo1 f1 = new FilterBeforeAlgo1();
				f1.filter(fileName);
				break; 
			case "fb2":  
				freqMap = FrequentWords.buildFreqMap("/news100M_stanford_cc_word_count");
				FilterBeforeAlgo2 f2 = new FilterBeforeAlgo2();
				f2.filter(fileName,null,50,mwes);
				break;
			case "fa1":  
				if (args.length<3){
					System.out.println("Not enough arguments : TypeOfFilter mwe/swe InputFileName BaseFileName");
				} else {
					freqMap = FrequentWords.buildFreqMap("/news100M_stanford_cc_word_count");
					ResultatBuildGraph res;
					if (args.length == 5){
						boolean exception = false;
						int nbMaxEdgesPerNode = 0;
						try {
							nbMaxEdgesPerNode = Integer.valueOf(args[4]);
						} catch (Exception e){
							exception = true;
							System.out.println("Problem with arg 5 max degree of nodes");
						} finally {
							if (exception) res = BuildGraph.buildGraph(args[3]);
							else res = BuildGraph.buildGraph(args[3],nbMaxEdgesPerNode);
						}
					} else res = BuildGraph.buildGraph(args[3]);
					FilterAfterAlgo1 f3 = new FilterAfterAlgo1();
					f3.filter(fileName, res, freqMap, 50);
				}
				break;
			case "fa2":  
				if (args.length<3){
					System.out.println("Not enough arguments : TypeOfFilter InputFileName BaseFileName");
				} else {
					ResultatBuildGraph res;
					if (args.length == 5){
						boolean exception = false;
						int nbMaxEdgesPerNode = 0;
						try {
							nbMaxEdgesPerNode = Integer.valueOf(args[4]);
						} catch (Exception e){
							exception = true;
							System.out.println("Problem with arg 5 max degree of nodes");
						} finally {
							if (exception) res = BuildGraph.buildGraph(args[3]);
							else res = BuildGraph.buildGraph(args[3],nbMaxEdgesPerNode);
						}
					} else res = BuildGraph.buildGraph(args[3]);
					FilterAfterAlgo2 f4 = new FilterAfterAlgo2();
					f4.filter(fileName, res);
				}
				break;
			default : System.out.println("No correct argument (fb1,fb2,fa1 or fa2)");
			}
		}

	}
}
