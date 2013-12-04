package com.luxsoft.sw3.contabilidad.ui.form;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Tipo;

public class CuentaContableFormModel extends DefaultFormModel{
	
	private EventList<String> subTipos;
	private EventList<Tipo> tiposList;

	public CuentaContableFormModel(final CuentaContable bean) {
		super(bean);
	}
	
	public CuentaContable getCuenta(){
		return (CuentaContable)getBaseBean();
	}
	
	
	@Override
	protected void init() {
		super.init();
		tiposList=GlazedLists.eventListOf(Tipo.values());
		getModel("tipo").addValueChangeListener(new TipoHandler());
	}
	
	public EventList<Tipo> getTiposList(){
		return tiposList;
	}

	public EventList<String> getSubTiposList(){
		if(subTipos==null){
			subTipos=new BasicEventList<String>(0);
		}
		return subTipos;
	}
	
	private class TipoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(getCuenta().getTipo()==null){
				subTipos.clear();
				getCuenta().setSubTipo(null);
			}else{
				subTipos.clear();
				subTipos.addAll(getCuenta().getTipo().getSubTiposList());
				getCuenta().setSubTipo(subTipos.get(0));
			}
		}
		
	}

	
}
