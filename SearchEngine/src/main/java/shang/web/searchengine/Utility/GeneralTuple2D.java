package shang.web.searchengine.Utility;

public class GeneralTuple2D<T1, T2 extends Comparable<T2>> implements Comparable<GeneralTuple2D<T1, T2>> {
	
	private T1 key;
	private T2 value;
	
	public GeneralTuple2D(){
	}
	public GeneralTuple2D(T1 key, T2 value){
		this.key = key;
		this.value = value;
	}

	public T1 getItem() {
		return key;
	}

	public T2 getValue() {
		return value;
	}

	@Override
	public String toString() {
    	StringBuilder builder = new StringBuilder();
    	
    	builder.append("key: ");
    	builder.append(this.key);
    	builder.append("    ");
    	builder.append("value: ");
    	builder.append(this.value);
    	return builder.toString();
	}
	

	public void setValue(T2 value) {
		this.value = (T2) value;
	}
	

	@Override
	public int compareTo(GeneralTuple2D<T1, T2> old) {//better
		T2 lhs = this.value;
		T2 rhs = old.getValue();
		return lhs.compareTo(rhs);
	}
	
    @SuppressWarnings("unchecked")//checked
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        GeneralTuple2D<T1, T2> target;
        if (o == null || getClass() != o.getClass()) 
        	return false;
        else 
        	target = (GeneralTuple2D<T1, T2>)o;
        
        if(key.equals(target.getItem())) return true;
        else return false;
    }

}
