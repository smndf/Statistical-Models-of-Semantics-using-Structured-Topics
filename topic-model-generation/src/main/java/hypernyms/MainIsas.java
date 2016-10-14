package hypernyms;

import java.io.IOException;

public class MainIsas {

	public static void main( String[] args){

		IsasSearch isas = new IsasSearch();
		if (args.length == 2){
			String input = args[0];
			System.out.println("input = "+input);
			String isasFileStem = args[1];
			try {
				isas.addISAS(input, isasFileStem);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("need input file as argument 1 and isas file name stem as argument 2");
		}

	}
}
