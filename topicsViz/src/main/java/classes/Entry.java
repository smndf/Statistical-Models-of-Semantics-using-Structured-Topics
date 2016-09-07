package classes;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

public class Entry {

	private Topic topic;
	private String highlightTopic;
	private Float scoreTopic;
	
	public Entry() {
		super();
	}

	public Entry(Topic topic, String highlightTopic, Float scoreTopic) {
		super();
		this.topic = topic;
		this.highlightTopic = highlightTopic;
		this.scoreTopic = scoreTopic;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public String getHighlightTopic() {
		return highlightTopic;
	}

	public void setHighlightTopic(String highlightTopic) {
		this.highlightTopic = highlightTopic;
	}

	public Float getScoreTopic() {
		return scoreTopic;
	}

	public void setScoreTopic(Float scoreTopic) {
		this.scoreTopic = scoreTopic;
	}

}
