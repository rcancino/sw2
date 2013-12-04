package com.luxsoft.siipap.replica.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DBFUtils {
	
	public static List<Map<String, Object>> readFile(final InputStream io) throws Exception {
		try {

			List<Map<String, Object>> rows=new ArrayList<Map<String,Object>>();
			DBFMapConverter reader;
			reader = new DBFMapConverter(io);
			Map<String, Object> rowObjects;

			while ((rowObjects = reader.nextRecord()) != null) {
				rows.add(rowObjects);
			}
			return rows;
		} finally {
			io.close();
		}

	}

}
