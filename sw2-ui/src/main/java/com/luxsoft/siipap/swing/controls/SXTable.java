package com.luxsoft.siipap.swing.controls;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.utils.Renderers;

public class SXTable  extends JXTable{

	public SXTable() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SXTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		// TODO Auto-generated constructor stub
	}

	public SXTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		// TODO Auto-generated constructor stub
	}

	public SXTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		// TODO Auto-generated constructor stub
	}

	public SXTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		// TODO Auto-generated constructor stub
	}

	public SXTable(TableModel dm) {
		super(dm);
		// TODO Auto-generated constructor stub
	}

	public SXTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
	}

	@Override
	protected void createDefaultRenderers() {		
		super.createDefaultRenderers();
		setDefaultRenderer(Date.class, new DefaultTableRenderer(new ToDate()));
		setDefaultRenderer(CantidadMonetaria.class, Renderers.getCantidadMonetariaTableCellRenderer());
	}
	
	@Override
	public void doLayout() {		
		super.doLayout();
	}





	private class ToDate implements StringValue{
		
		final DateFormat df=new SimpleDateFormat("dd/MM/yyyy");

		public String getString(Object value) {
			try {
				return df.format(value);
			} catch (Exception e) {				
				if(value!=null)
					return value.toString();
				else
					return "";
			}
			
		}
		
	}
	
	
	

}
