package com.luxsoft.siipap.swing.browser;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;

import com.luxsoft.siipap.model.Periodo;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

public class SQLBrowserPanel extends FilteredBrowserPanel<Map>{
	
	private String sql;

	public SQLBrowserPanel() {
		super(Map.class);
	}
	
	public void init(){
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodoDelMesActual();
	}
	
	protected TableFormat buildTableFormat(){
		return new MapTableForma();
	}
	
	protected List findData(){
		return ListUtils.EMPTY_LIST;
	}	
	
	@Override
	protected void dataLoaded(final List data){		
		source.clear();
		source.addAll(data);
		afterFindData(data);
		redoTableModel();
		if(grid!=null)
			grid.packAll();
		afterLoad();
	}
	
	private void afterFindData(final List data){
		System.out.println("Procesando regsitros: "+data.size());
		if(data!=null && !data.isEmpty()){
			System.out.println("Regenerando el TableModel");
			Map<String,Object> row=(Map<String,Object>)data.get(0);
			setProperties(row.keySet().toArray(new String[0]));
			setLabels(properties);
		}else{
			setProperties(null);
			setLabels(null);
		}
	}
	
	private void redoTableModel(){
		System.out.println("Reconfigurando el table model y table format...");
		EventTableModel model=(EventTableModel)grid.getModel();
		model.setTableFormat(new MapTableForma());
		
	}

	

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}



	public  class MapTableForma implements WritableTableFormat<Map>
	//,AdvancedTableFormat<Map>
	{
		public int getColumnCount() {
			if(source.isEmpty())
				return 0;
			Map row=(Map)source.get(0);
			return row.keySet().size();
			//return 0;
		}

		public String getColumnName(int column) {
			if(ArrayUtils.isEmpty(getLabels()))
				return null;
			return getLabels()[column];
		}

		public Object getColumnValue(Map baseObject, int column) {
			
			if(baseObject == null || ArrayUtils.isEmpty(getProperties())) return null;
			String key=getProperties()[column];
			return baseObject.get(key);
		}

		public boolean isEditable(Map baseObject, int column) {			
			return false;
		}

		public Map setColumnValue(Map baseObject, Object editedValue, int column) {			
			return null;
		}
		
	}

}
