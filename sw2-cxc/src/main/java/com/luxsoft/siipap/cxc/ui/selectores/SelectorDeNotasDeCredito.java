package com.luxsoft.siipap.cxc.ui.selectores;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;



/**
 * 
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeNotasDeCredito extends AbstractSelector<NotaDeCredito>{
	
	
	public SelectorDeNotasDeCredito() {
		super(NotaDeCredito.class, "Notas de credito");
		
	}
	
	@Override
	protected TableFormat<NotaDeCredito> getTableFormat() {
		String[] props=new String[]{
				"clave"
				,"nombre"
				,"tipo"
				,"folio"
				,"fecha"
				,"sucursal.nombre"
				,"total"
				,"aplicado"
				,"disponible"
				,"aplicable"
				,"impreso"
				};
		String[] names=new String[]{
				"clave"
				,"nombre"
				,"tipo"
				,"folio"
				,"fecha"
				,"sucursal.nombre"
				,"total"
				,"aplicado"
				,"disponible"
				,"aplicable"
				,"impreso"
				};
		return GlazedLists.tableFormat(NotaDeCredito.class,props,names);
	}
	
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(850,500));
	}
	
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "buscar");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar notas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}	
	
	
	
	@Override
	protected void onWindowOpened() {
		load();
	}

	@Override
	protected List<NotaDeCredito> getData() {
		return ListUtils.EMPTY_LIST;
		
	}
	
	public void clean(){
		source.clear();
	}
	
}
