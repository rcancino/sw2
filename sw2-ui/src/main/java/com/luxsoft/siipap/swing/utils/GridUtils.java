package com.luxsoft.siipap.swing.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;

import com.luxsoft.siipap.swing.Application;

public class GridUtils {
	
	public static void saveColumnsVisibility(JXTable grid,String key){
		//System.out.println("Persistiendo columnas para: "+grid.hashCode());
		if(Application.isLoaded()){
			StringBuffer buf=new StringBuffer();
			TableColumnModelExt tcm=(TableColumnModelExt)grid.getColumnModel();
			List<TableColumn> cols=tcm.getColumns(true);
			for(TableColumn tc:cols){
				TableColumnExt tx=(TableColumnExt)tc;
				boolean vis=tx.isVisible();
				buf.append(tx.getModelIndex()+"="+vis);
				buf.append(",");
			}
			System.out.println("Coding: "+key);
			System.out.println("Configuracion: "+buf.toString());
			Application.instance().getUserPreferences().put(key, buf.toString());
		}
	}
	
	public static void restorColumnsVisibility(JXTable grid,String key){
		if(Application.isLoaded()){
			System.out.println("Decoding: "+key);
			String value=Application.instance().getUserPreferences().get(key, "");
			Set<Integer> hiden=new HashSet<Integer>();
			if(!StringUtils.isBlank(value)){
				TableColumnModelExt tcm=(TableColumnModelExt)grid.getColumnModel();
				String[] cols=StringUtils.split(value, ',');
				for(String c:cols){
					String[] data1=StringUtils.split(c, '=');
					int column=Integer.valueOf(data1[0]);
					boolean visible=Boolean.valueOf(data1[1]);
					if(!visible)
						hiden.add(column);
				}
				List<TableColumn> columns=tcm.getColumns(true);
				for(TableColumn tc:columns){
					TableColumnExt tx=(TableColumnExt)tc;
					if(hiden.contains(tx.getModelIndex()))
							tx.setVisible(false);
				}
			}
		}
	}

}
