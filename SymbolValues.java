
public class SymbolValues {

	private String type;
	private String kind;
	private int index=0;
	
	
	public SymbolValues(String type, String kind, int index) {
		
		this.type = type;
		this.kind = kind;
		this.index = index;

		
	}


	protected String getType() {
		return type;
	}


	protected String getKind() {
		return kind;
	}


	protected int getIndex() {
		return index;
	}
	
	protected void setIndex(int index) {
		
		this.index = index;
	}
	
	
	
}
