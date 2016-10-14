package classification;

public class NamedEntity {

	private String text;
	private String tag;
	
	public NamedEntity(String text, String tag) {
		super();
		this.text = text;
		this.tag = tag;
	}

	public NamedEntity(String text) {
		super();
		this.text = text;
		this.tag = "";
	}

	public NamedEntity() {
		super();
		this.text = "";
		this.tag = "";
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public String toString() {
		return "(" + text + ", " + tag + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		NamedEntity other = (NamedEntity) obj;
		if (this.getTag().equals(other.getTag()) && this.getText().equals(other.getText())) return true;
		return false;
	}
	
	
	
	
}
