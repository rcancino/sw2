package com.luxsoft.sw2.replica;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface BatchConverter<T> {
	
	 
	public Map<Serializable, String> format(Collection<T> bean);
	
	public Collection<T> parse(Map<Serializable,String> batchMap);

}
