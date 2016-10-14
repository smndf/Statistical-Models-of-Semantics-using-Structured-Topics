package hypernyms;

public class IsA {

	private String hypernym;
	private Integer weight;
	private Integer frequency;
	
	public IsA(String hypernym, Integer weight) {
		super();
		this.hypernym = hypernym;
		this.weight = weight;
	}
	
	public IsA(String hypernym, Integer weight, Integer frequency) {
		super();
		this.hypernym = hypernym;
		this.weight = weight;
		this.frequency = frequency;
	}

	public String getHypernym() {
		return hypernym;
	}
	public void setHypernym(String hypernym) {
		this.hypernym = hypernym;
	}
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	public Integer getFrequency() {
		return frequency;
	}
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		return hypernym+":"+frequency;
	}
	
	
}
