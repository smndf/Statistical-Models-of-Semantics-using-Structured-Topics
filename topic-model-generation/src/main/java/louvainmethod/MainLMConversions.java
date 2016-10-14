package louvainmethod;


import java.io.IOException;



public class MainLMConversions {

	public static void main(String[] args) {

		if (args.length<2){
			System.out.println("Not enough arguments : \nbef InputFileName\nor\naft InputFileName MapIntegerToString OutputFile");
		} else {
			switch (args[0]) {
			case "bef":  
				LMGraphConversion lmgc = new LMGraphConversion();
				try {
					lmgc.convertToLM(args[1]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				break; 
			case "aft":  
				if (args.length<4){
					System.out.println("Not enough arguments : aft InputFile MapIntegerToString OutputFile");
				} else {
					Clusters c = new Clusters();
					String outputFile = args[3];
					try {
						c.convertFromLM(args[1], args[2],outputFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			default : System.out.println("No correct argument (bef or aft)");
			}
		}	 
	}
}
