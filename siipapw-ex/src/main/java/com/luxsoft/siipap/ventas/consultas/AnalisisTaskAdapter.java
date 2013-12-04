package com.luxsoft.siipap.ventas.consultas;

import javax.swing.Action;
import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPane;

import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;

public class AnalisisTaskAdapter extends AbstractInternalTaskView{
	
	protected final AbstractAnalisisPanel panel;
	
	public AnalisisTaskAdapter(final AbstractAnalisisPanel panel){
		this.panel=panel;
	}

	public JComponent getControl() {
		return panel.getControl();
	}
	
	@Override
	public void instalOperacionesAction(JXTaskPane operaciones) {
		for(Action a:panel.getActions()){
			operaciones.add(a);
		}
	}
	
	@Override
	public void instalProcesosActions(JXTaskPane procesos) {
		
	}
	
	@Override
	public void installFiltrosPanel(JXTaskPane filtros) {
		//filtros.add(panel.getFilterPanel());
	}		
	

}
