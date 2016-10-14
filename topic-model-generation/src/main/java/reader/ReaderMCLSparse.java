package reader;



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

import mcl.MarkovClustering;
import mcl.MarkovClusteringSparse;
import mcl.Matrix;
import mcl.SparseMatrix;
import mcl.SparseVector;

public class ReaderMCLSparse {

	public static void main(String[] args) throws IOException {			
		String fileName;
		if (args.length!=0){
			fileName = args[0] ;
			System.out.println("fileName = " + fileName);
			runMCL(fileName);
		}else{
			System.out.println("Need the file name as argument.");
		}
	}

	public static void runMCL(String fileName) throws IOException {

		System.out.print("Reading input file and building matrix...");	
		//FileReader fr = new FileReader("testReader.txt");
		//InputStream is = getClass().getResourceAsStream(fileName);
		//BufferedReader br = new BufferedReader(new InputStreamReader(is));
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		int nbNodes = getNumberNodes(fileName);
		System.out.println(nbNodes + " nodes");

		String line = null;

		String[] lineSplit = new String[4];
		Map<Integer,String> nodesMapItoS = new HashMap<Integer,String>();
		Map<String,Integer> nodesMapStoI = new HashMap<String,Integer>();
		//Graph<Integer, Float> g = new ArrayBackedGraph<Float>(N, 6);
		SparseMatrix matrix = new SparseMatrix(nbNodes,nbNodes);
		br.readLine();
		int i = 0;
		int root = 0; //root word of each cluster,line
		int linectr=0,linectr2 = 0;
		while ( (line = br.readLine()) != null){ 

			root = i;

			linectr++;
			if (linectr==10000){
				linectr2 +=linectr;
				System.out.println("line " + linectr2);
				linectr=0;
			}

			lineSplit = line.split("\t");
			//ArrayList neighbours = new ArrayList<String>();
			//System.out.println(lineSplit[0]);
			String word = lineSplit[0];

			if (nodesMapStoI.containsKey(word)){
				root = nodesMapStoI.get(word);
			}else{
				nodesMapItoS.put(i, word);
				nodesMapStoI.put(word, i);
				//matrix = Vectors.increaseSize(matrix, 1, 1);
				i++;
			}
			if (lineSplit.length>1){
				//System.out.println(lineSplit[2]);
				ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[1].split(",")));
				for (String neighbourSp : neighboursSplit){
					//neighbours.add(neighbourSp.split(":")[0]);
					// if node already added
					if (neighbourSp.split(":").length>1){							
						if (nodesMapStoI.containsKey(neighbourSp.split(":")[0])){
							int node = nodesMapStoI.get(neighbourSp.split(":")[0]);
							matrix.set(node, root, matrix.get(node, root) + Double.valueOf(neighbourSp.split(":")[1]));
							matrix.set(root, node, matrix.get(root, node) + Double.valueOf(neighbourSp.split(":")[1]));

						}else{
							nodesMapItoS.put(i, neighbourSp.split(":")[0]);
							nodesMapStoI.put(neighbourSp.split(":")[0] , i);
							//matrix = Vectors.increaseSize(matrix, 1, 1);
							//g.addEdgeUndirected(root, i, Float.valueOf(neighbourSp.split(":")[1]));
							matrix.add(i, root, Double.valueOf(neighbourSp.split(":")[1]));
							matrix.add(root, i, Double.valueOf(neighbourSp.split(":")[1]));
							i++;
						}
					}
				}
			}
		}
		System.out.println("OK");	


		//System.out.println(nodesMapItoS.toString());


		System.out.print("Running Markov Clustering algo...");
		MarkovClusteringSparse mc = new MarkovClusteringSparse();
		SparseMatrix m2 = mc.run(matrix, 0, 2.0, 0.5, 0.00001);
		System.out.println("OK");	

		//System.out.println("\nmap words :");
		//System.out.println(nodesMapStoI.toString());

		String outputFile = "MCL"+fileName;
		writeResultsMCL( m2, nodesMapItoS, outputFile);
		System.out.println("Results written in file "+outputFile);

	}

	public static void writeResultsMCL(SparseMatrix m, Map<Integer,String> nodesMap, String outputFile){
		File fo = new File(outputFile);

		try
		{
			FileWriter fw = new FileWriter (fo);

			StringWriter writer = new StringWriter();
			StringWriter cluster = new StringWriter();

			int noTopic = 0;
			Boolean emptyColumn = true;
			for (int i = 0; i<m.size();i++){
				SparseVector c;
				if ((c = m.getColum(i)).max()>0){
					//System.out.println("column " + i);
					if (nodesMap.get(i) == null){
						System.out.println(i + " ");
						System.out.println(c.toStringDense());
					}
					cluster.write(nodesMap.get(i).toString() + "\t");
					for (Map.Entry<Integer, Double> entry : c.entrySet()){
						if (entry.getValue()!=0){
							cluster.write(nodesMap.get(entry.getKey()) + ",");
						}
					}
					cluster.getBuffer().setLength(cluster.toString().length()-1);
					cluster.write("\n");
				}
			}

			fw.write (cluster.toString());

			fw.close();
		}
		catch (IOException exception)
		{
			System.err.println ("Error while reading : " + exception.getMessage());
		}
	}

	public static int getNumberNodes(String fileName) throws IOException {
		System.out.print("Reading input file to know number of nodes...");	
		//FileReader fr = new FileReader("testReader.txt");
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		String line = null;
		String[] lineSplit = new String[4];
		Set<String> nodes = new HashSet<String>();
		br.readLine();
		int linectr=0,linectr2 = 0;
		while ( (line = br.readLine()) != null){ 

			linectr++;
			if (linectr==10000){
				linectr2 +=linectr;
				System.out.println("line " + linectr2);
				linectr=0;
			}

			lineSplit = line.split("\t");
			String word = lineSplit[0];

			nodes.add(word);
			if (lineSplit.length>1){
				ArrayList<String> neighboursSplit = new ArrayList<String>(Arrays.asList(lineSplit[1].split(",")));
				for (String neighbourSp : neighboursSplit){
					if (neighbourSp.split(":").length>1){
						nodes.add(neighbourSp.split(":")[0]);
					}
				}
			}
		}
		System.out.println("OK");	
		return nodes.size();
	}
}	
