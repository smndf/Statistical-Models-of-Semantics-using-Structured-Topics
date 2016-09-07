package classes;

import java.util.ArrayList;
import java.util.List;

public class Results {

	private String text;
	private List<Entry> entries;
	
	public Results() {
		super();
		this.entries = new ArrayList<Entry>();
	}
	public Results(String text, List<Entry> entries) {
		super();
		this.text = text;
		this.entries = entries;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<Entry> getEntries() {
		return entries;
	}
	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}
	
}
