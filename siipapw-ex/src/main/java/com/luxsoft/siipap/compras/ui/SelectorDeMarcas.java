package com.luxsoft.siipap.compras.ui;

import java.util.List;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;


import com.luxsoft.siipap.model.core.Marca;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;

public class SelectorDeMarcas extends AbstractSelector<Marca>{
	
	public SelectorDeMarcas() {
		super(Marca.class, "Selector de Marcas");
		
	}

	@Override
	protected List<Marca> getData() {
		return ServiceLocator2.getUniversalDao().getAll(Marca.class);
	}

	@Override
	protected TableFormat<Marca> getTableFormat() {
		return GlazedLists.tableFormat(Marca.class, new String[]{"id","nombre"},new String[]{"Id","Nombre"});
	}
	
	public static Marca seleccionar(){
		SelectorDeMarcas s=new SelectorDeMarcas();
		s.open();
		if(!s.hasBeenCanceled()){
			return s.getSelected();
		}
		return null;
	}

}
