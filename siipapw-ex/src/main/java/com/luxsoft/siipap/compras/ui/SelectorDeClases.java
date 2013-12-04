package com.luxsoft.siipap.compras.ui;

import java.util.List;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;

public class SelectorDeClases extends AbstractSelector<Clase>{
	
	public SelectorDeClases() {
		super(Clase.class, "Selector de Clases");
		
	}

	@Override
	protected List<Clase> getData() {
		return ServiceLocator2.getUniversalDao().getAll(Clase.class);
	}

	@Override
	protected TableFormat<Clase> getTableFormat() {
		return GlazedLists.tableFormat(Clase.class, new String[]{"id","nombre"},new String[]{"id","Clase"});
	}
	
	public static Clase seleccionar(){
		SelectorDeClases s=new SelectorDeClases();
		s.open();
		if(!s.hasBeenCanceled()){
			return s.getSelected();
		}
		return null;
	}

}
