package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class ImagesToJson {

	public static void main(String[] args) {

		if (args.length==1){
			System.out.println(imagesFileToJson(args[0]));
		}
	}


	public static String imagesFileToJson(String imagesFileUrl){
		Map<String,String> map = new HashMap<String,String>();
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(imagesFileUrl));
			//InputStream is = getServletContext().getResourceAsStream(imagesFileUrl);
			//BufferedReader input = new BufferedReader(new InputStreamReader(is));
			String[] lineSplit;
			String line = null;
			try {
				while ( (line = input.readLine()) != null){ 
					lineSplit = line.split("\t");
					if (lineSplit.length == 2){
						map.put(lineSplit[0], lineSplit[1]);
					}
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		json.put("images", map );
		return json.toString();
	}
}
