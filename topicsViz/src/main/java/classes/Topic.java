package classes;

import java.io.StringWriter;

public class Topic {

	private int id;
	private int size;
	private String content;
	private String fiftyFirstWords;
	private String wordNetHypernyms;
	private String isaRelations;
	
	public Topic(){
		super();
	}
	
	public Topic(String content){
		super();
		this.content = content;
	}
	
	public Topic(int id, String content, String wordNetHypernyms, String isaRelations) {
		super();
		this.id = id;
		this.content = content;
		this.wordNetHypernyms = wordNetHypernyms;
		this.isaRelations = isaRelations;
		this.size = content.split(",").length;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getFiftyFirstWords() {
		return fiftyFirstWords;
	}

	public void setFiftyFirstWords(String fiftyFirstWords) {
		this.fiftyFirstWords = fiftyFirstWords;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getWordNetHypernyms() {
		return wordNetHypernyms;
	}
	public void setWordNetHypernyms(String wordNetHypernyms) {
		this.wordNetHypernyms = wordNetHypernyms;
	}
	public String getIsaRelations() {
		return isaRelations;
	}
	public void setIsaRelations(String isaRelations) {
		this.isaRelations = isaRelations;
	}

	public static Topic getTopicFromLine(String line) {
		if (line.split("\t").length<4) return null;
		Topic topic = new Topic();
		int id = Integer.valueOf(line.split("\t")[0]);
		topic.setId(id);
		String content = line.split("\t")[1];
		topic.setContent(content);
		int size = content.split(",").length;
		topic.setSize(size);
		String fiftyFirstWords;
		if (size>50){
			fiftyFirstWords = getNFirstWords(content,50);
		} else {
			fiftyFirstWords = content;
		}
		topic.setFiftyFirstWords(fiftyFirstWords);
		String hypernyms = line.split("\t")[2];
		StringWriter sw = new StringWriter();
		for (String hyp : hypernyms.split(", ")){
			sw.append(hyp.split(",")[0]+", ");
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-2);
		hypernyms = sw.toString();
		topic.setWordNetHypernyms(hypernyms);
		String isas = line.split("\t")[3];
		topic.setIsaRelations(isas);
		return topic;
	}

	private static String getNFirstWords(String content, int N) {
		
		StringWriter sw = new StringWriter();
		int nb = 0;
		for (String word : content.split(",")){
			sw.append(word+",");
			nb++;
			if (nb==N) break;
		}
		sw.getBuffer().setLength(sw.getBuffer().length()-1);
		return sw.toString();
	}
	
}
