package com.luxsoft.siipap.cxp.ui.selectores;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


/**
 * Selector de facturas para un proveedor, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeProveedores extends AbstractSelector<Proveedor>{
	
	
	public SelectorDeProveedores() {
		super(Proveedor.class, "Catálogo de proveedores");
		
	}
	
	@Override
	protected TextFilterator<Proveedor> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"nombreRazon"});
	}

	@Override
	protected TableFormat<Proveedor> getTableFormat() {
		String props[]={"clave","nombreRazon"};
		String labels[]={"Clave","Nombre"};
		return GlazedLists.tableFormat(Proveedor.class,props,labels);
	}
	
	 
	
	protected JComponent buildHeader(){
		HeaderPanel header=new HeaderPanel("Proveedores registrados","Seleccione un proveedor");
		return header;
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(810,500));
	}
	

	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createLoadAction(this, "load");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	

	@Override
	protected List<Proveedor> getData() {
		return ServiceLocator2.getProveedorManager().getAll();
	}		

	
	
	public static Proveedor seleccionarProveedor(){
		SelectorDeProveedores selector=new SelectorDeProveedores();//getInstance();
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			Proveedor selected=selector.getSelected();
			return selected;
		}		
		return null;
		
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				Proveedor p=seleccionarProveedor();
				System.out.println(p);
				System.exit(0);
			}
			
		});
		
	}

}
