package project1;

public class TargetType {
	
	private String type;
	
	private int ref = 0;;
	
	private int dec = 0;

	TargetType(String type, int ref, int dec){
		
		this.type = type;
		this.ref = ref;
		this.dec = dec;
	}
	
	
	public String getType() {
		return type;
	}
	
	public int getRef() {
		return ref;
	}

	public int getDec() {
		return dec;
	}
	
	public void addRef() {
		this.ref ++;
	}
	
	public void addDec() {
		this.dec ++;
	}
}

