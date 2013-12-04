package com.luxsoft.sw2.replica.remote;

import java.util.List;
import java.util.Map;

public interface RequestService {
	
	public List<Map<String, Object>> requestSqlData(String sql,Object...params);

}
