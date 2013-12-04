package com.luxsoft.siipap.cxc.ui.analisis;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;

public class AnalisisDeCobranzaPanel extends FilteredBrowserPanel<AnalisisDeCobranzaSemanal>{

	public AnalisisDeCobranzaPanel() {
		super(AnalisisDeCobranzaSemanal.class);
		
	}
	
	protected void init(){
		addProperty("year"
				,"mes"
				,"ventas1"
				,"ventas2"
				,"ventas3"
				,"ventas4"
				,"ventas5"
				);
		addLabels("Año"
				,"Mes"
				,"VS1"
				,"VS2"
				,"VS3"
				,"VS4"
				,"VS5"
				);
	}

	@Override
	protected List<AnalisisDeCobranzaSemanal> findData() {
		return new ArrayList<AnalisisDeCobranzaSemanal>();
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				};
		return actions;
	}
	
	
	

}
