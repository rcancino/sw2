package com.luxsoft.siipap.compras.ui;

import java.util.List;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;

public class SelectorDeLineas extends AbstractSelector<Linea>{
	
	public SelectorDeLineas() {
		super(Linea.class, "Selector de líneas");
		
	}

	@Override
	protected List<Linea> getData() {
		return ServiceLocator2.getUniversalDao().getAll(Linea.class);
	}

	@Override
	protected TableFormat<Linea> getTableFormat() {
		return GlazedLists.tableFormat(Linea.class, new String[]{"nombre","descripcion"},new String[]{"Linea","Descripción"});
	}
	
	public static Linea seleccionar(){
		SelectorDeLineas s=new SelectorDeLineas();
		s.open();
		if(!s.hasBeenCanceled()){
			return s.getSelected();
		}
		return null;
	}

}
