package com.luxsoft.siipap.swing.views2;

import javax.swing.Icon;

import org.jdesktop.swingx.JXTaskPane;

import com.luxsoft.siipap.swing.controls.ViewControl;

public interface InternalTaskView extends ViewControl{
	
	public static final String SUPPORT_KEY="internalTaskView";
	
	public String getTitle();
	
	public Icon getIcon();
	
	public void instalOperacionesAction(JXTaskPane operaciones);
	
	public void instalProcesosActions(JXTaskPane procesos);
	
	public void installFiltrosPanel(JXTaskPane filtros);
	
	public void installDetallesPanel(JXTaskPane detalle);
	
	public void open();
	
	public void close();

}
