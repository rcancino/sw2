package com.luxsoft.siipap.gastos.consultas;

import java.util.Comparator;
import java.util.Map;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;

public class MapTableModel implements AdvancedTableFormat<Map<String, Object>>{
	
	private Map<String, Object> model;

	public Class getColumnClass(int column) {
		return null;
	}

	public Comparator getColumnComparator(int column) {
		return GlazedLists.comparableComparator();
	}

	public int getColumnCount() {
		return model.size();
	}

	public String getColumnName(int column) {
		return null;
	}

	public Object getColumnValue(Map<String, Object> baseObject, int column) {
		return baseObject.get(getColumnName(column));
	}

}
