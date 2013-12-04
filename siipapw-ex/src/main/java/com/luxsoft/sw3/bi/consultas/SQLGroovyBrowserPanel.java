package com.luxsoft.sw3.bi.consultas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.util.ClassUtils;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.util.SQLUtils;

public class SQLGroovyBrowserPanel extends FilteredBrowserPanel<Map>{

	public SQLGroovyBrowserPanel() {
		super(Map.class);
	}
	
	public void init(){
		doGroovyInit();
	}
	
	protected TableFormat buildTableFormat(){
		return new MapTableForma();
	}
	
	/**
	 * Inicializacion en Groovy pasar a Interface
	 * 
	 */
	public void doGroovyInit(){
		manejarPeriodo();
	}

	@Override
	protected void beforeLoad() {
		super.beforeLoad();
	}
	
	/**
	 *  Generacion del SQL pasar a Groovy o externalizar
	 *  
	 *  TODO Externalizar posiblemente con finData
	 *  
	 */
	protected List doGroovyFindData(){
		String path=ClassUtils.addResourcePathToPackagePath(getClass(), "test.sql");
		String sql=SQLUtils.loadSQLQueryFromResource(path);
		System.out.println("Procesando: "+sql);
		//Object[] args={};
		List res= ServiceLocator2.getJdbcTemplate().queryForList(sql);
		return res;
		//ClassUtils.
	}
	
	private void afterGroovyFindData(final List data){
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
	
	protected List findData(){
		return doGroovyFindData();
	}	
	
	@Override
	protected void dataLoaded(final List data){		
		source.clear();
		source.addAll(data);
		afterGroovyFindData(data);
		redoTableModel();
		if(grid!=null)
			grid.packAll();
		afterLoad();
	}
	
	private void redoTableModel(){
		System.out.println("Reconfigurando el table model y table format...");
		EventTableModel model=(EventTableModel)grid.getModel();
		model.setTableFormat(new MapTableForma());
		//grid.setModel(newModel);
	}

	
	protected void afterLoad() {
		super.afterLoad();
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
