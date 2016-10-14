package hypernyms;

import java.io.FileNotFoundException;

public class MainHypernyms 
{
	public static void main( String[] args )
	{
		
		
		if (args.length>0){

			String inputFile = args[0] ;
			HypernymsSearch hs = new HypernymsSearch();
			try {
				hs.wordNetSearch(inputFile,true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No file name.");
			/*
			String inputFile = "CWddt-wiki-n30-1400k-v3-closureFiltBef1FiltAft.txt" ;
			
			HypernymsSearch hs = new HypernymsSearch();
			try {
				hs.wordNetSearch(inputFile,false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			*/
		}
		//IsasSearch is = new IsasSearch();
		//is.isasSearch();
	}
}
