import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SymbolTable {

	
	private Hashtable<String, SymbolValues> classSymbol;
	private Hashtable<String, SymbolValues> subroutineSymbol;
	
	
	public SymbolTable() {
		
		//classSymbol resets when compiling new class
		classSymbol = new Hashtable<String, SymbolValues>();
		subroutineSymbol = new Hashtable<String, SymbolValues>();
	
		
	}
	
	protected void startSubroutine() {
		
		//subroutineSymbol resets when compiling new subroutine
		//clear table
		subroutineSymbol.clear();
		//first entry of subSymbol is always this, classType, argument set at index 0
		
	
		
	}
	
	//defines new identifier of the given name type and kind and assigns
	//running index to it in the table
	//two scropes:class & subroutine - kinds
	protected void define(String name, String type, String kind, int index) {
		
		SymbolValues symValue = new SymbolValues(type, kind, index);
		
		if (kind.equals("static")||kind.equals("field")) {
			
			classSymbol.put(name, symValue);
			
		} else {
			
			subroutineSymbol.put(name, symValue);
		}
		
	}
	
	
	//returns number of variables of the given kind already defined in the current scope
	protected int varCount(String kind) {

		int count=0;

		if (kind.equals("static")) {
			
			for (Map.Entry<String, SymbolValues> entry : classSymbol.entrySet()) {
	
				
				
				SymbolValues value = entry.getValue();
				String kin = value.getKind();
		
				
				if (kin.equals("static")) {
			
					
					count++;
					
				}
			}
	
		} else if (kind.equals("field")) {
			
		
			
			for (Map.Entry<String, SymbolValues> entry : classSymbol.entrySet()) {
	
				
				
				SymbolValues value = entry.getValue();
				String kin = value.getKind();
		
				
				if (kin.equals("field")) {
			
					
					count++;
					
				}
			}
			
		} else if (kind.equals("var")) {
			
			for (Map.Entry<String, SymbolValues> entry : subroutineSymbol.entrySet()) {
	
				
				
				SymbolValues value = entry.getValue();
				String kin = value.getKind();
		
				
				if (kin.equals("var")) {
			
					
					count++;
					
				}
			}
			
		} else if (kind.equals("argument")){
			
			
			
			for (Map.Entry<String, SymbolValues> entry : subroutineSymbol.entrySet()) {

				
				
				SymbolValues value = entry.getValue();
				String kin = value.getKind();
		
				
				if (kin.equals("argument")) {
			
					
					
					count++;
					System.out.println(count);
					
					
				}
			}
			
			
		}

		return count;
	}
	
	
	//returns the kind of the named identifier in the current scope
	//how to know the currentScope?
	protected String kindOf(String name) {
		
		
		if (subroutineSymbol.containsKey(name)) {
			
			
			return subroutineSymbol.get(name).getKind();
			
		
		} else {
			
			return classSymbol.get(name).getKind();
			
		}
		
	}
	
	
	//returns the type of the named identifier
	protected String typeOf(String name) {
		
		if (subroutineSymbol.containsKey(name)) {
			
			
			return subroutineSymbol.get(name).getType();
			
		
		} else {
			
			return classSymbol.get(name).getType();
			
		}
	}
	
	//return the index assigned to the identifier
	protected int indexOf(String name) {
		
		if (subroutineSymbol.containsKey(name)) {
			
			
			return subroutineSymbol.get(name).getIndex();
			
		
		} else {
			
			return classSymbol.get(name).getIndex();
			
		}
	}
	
	public Hashtable<String, SymbolValues> getClassSymbol() {
		return classSymbol;
	}
	
	public Hashtable<String, SymbolValues> getSubSymbol() {
		return subroutineSymbol;
	}
	
	public void printSubSymbol(){
		
		System.out.println("subSymbol Size: " + subroutineSymbol.size());
		
		for (Map.Entry<String, SymbolValues> entry : subroutineSymbol.entrySet()) {
			
			String key = entry.getKey();
			SymbolValues value = entry.getValue();
			
			System.out.println(key +","+value.getType() +","+value.getKind()+","+value.getIndex());
			
		}
		
	}
	
	public void printClassSymbol() {
		
		System.out.println("classSymbol Size: " + subroutineSymbol.size());
		
		for (Map.Entry<String, SymbolValues> entry : classSymbol.entrySet()) {
			
			String key = entry.getKey();
			SymbolValues value = entry.getValue();
			
			System.out.println(key +","+value.getType() +","+value.getKind()+","+value.getIndex());
			
		}
		
	}
}
