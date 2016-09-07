package classes;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopicGraph {

	private Map<Integer,String> mapItoS;
	private String mapItoSJson;
	private Map<Integer,Set<Integer>> connections;
	private String connectionsJson;
	private List<String> list;
	private String listJson;
	private String imagesJson;
	
	public Map<Integer, String> getMapItoS() {
		return mapItoS;
	}
	public void setMapItoS(Map<Integer, String> mapItoS) {
		this.mapItoS = mapItoS;
	}
	public Map<Integer, Set<Integer>> getConnections() {
		return connections;
	}
	public void setConnections(Map<Integer, Set<Integer>> connections) {
		this.connections = connections;
	}
	public String getMapItoSJson() {
		return mapItoSJson;
	}
	public void setMapItoSJson(String mapItoSJSON) {
		this.mapItoSJson = mapItoSJSON;
	}
	public String getConnectionsJson() {
		return connectionsJson;
	}
	public void setConnectionsJson(String connectionsJSON) {
		this.connectionsJson = connectionsJSON;
	}
	
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
	public String getListJson() {
		return listJson;
	}
	public void setListJson(String listJson) {
		this.listJson = listJson;
	}
	public void updateListJson(){
		StringWriter sw = new StringWriter();
		sw.append("'");
		for (String s : list){
			sw.append(s+",");
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-1);
		sw.append("'");
		
		//this.listJson = "'"+new Gson().toJson(this.list)+"'";
		this.listJson = sw.toString();
	}
	public static String toJavascriptArray(String[] arr){
	    StringBuffer sb = new StringBuffer();
	    sb.append("[");
	    for(int i=0; i<arr.length; i++){
	        sb.append("\"").append(arr[i]).append("\"");
	        if(i+1 < arr.length){
	            sb.append(",");
	        }
	    }
	    sb.append("]");
	    return sb.toString();
	}
	public String getImagesJson() {
		return imagesJson;
	}
	public void setImagesJson(String imagesJson) {
		this.imagesJson = imagesJson;
	}
	
	
	
}
