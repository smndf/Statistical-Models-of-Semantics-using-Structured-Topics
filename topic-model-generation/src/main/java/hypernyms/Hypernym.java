package hypernyms;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import hypernyms.HypernymsSearch.Hypernym;

public class Hypernym {

	private String word;
	private int depth;

	public Hypernym(String word, int depth) {
		super();
		this.word = word;
		this.depth = depth;
	}

	public Hypernym(String word) {
		super();
		this.word = word;		}

	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public void addToMaps(Map<Hypernym, Integer> hMap,
			Map<Integer, Set<Hypernym>> hypRankMap, Integer weight) {

		int oldWeight = 0,newWeight = 0;
		if (hMap.containsKey(this)){
			oldWeight = hMap.get(this);
			newWeight = oldWeight + weight;
			//System.out.println("hyp("+hyp.getWord()+",d="+hyp.depth+")");
			//System.out.println("remove "+hyp.getWord()+" from hypRankMap "+hMap.get(hyp));
			hypRankMap.get(oldWeight).remove(this);
			if (!hypRankMap.containsKey(newWeight)){
				hypRankMap.put(newWeight,new HashSet<Hypernym>());
			}
			//System.out.println("add "+hyp.getWord()+" to hypRankMap "+hMap.get(hyp)+1);
			hypRankMap.get(newWeight).add(this);						
			//System.out.println("update hMap for "+hyp.getWord()+" : "+hMap.get(hyp)+" > "+hMap.get(hyp)+1);
			hMap.put(this,newWeight);
		} else {
			hMap.put(this,weight);
			//System.out.println("add "+hyp.getWord()+",1 to hMap ");
			if (!hypRankMap.containsKey(weight)){
				hypRankMap.put(weight,new HashSet<Hypernym>());
			}
			hypRankMap.get(weight).add(this);
			//System.out.println("add 1,"+hyp.getWord()+" to hypRankMap ");
		}


	}

	/*@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getOuterType().hashCode();
		result = prime * result + depth;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hypernym other = (Hypernym) obj;
		if (!getOuterType().equals(other.getOuterType()))
			return false;
		if (depth != other.depth)
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}*/

	/*private HypernymsSearch getOuterType() {
		return HypernymsSearch.this;
	}*/

}
