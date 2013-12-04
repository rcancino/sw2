package com.luxsoft.sw3.ui.selectores;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Example.PropertySelector;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;


/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeCargos extends AbstractSelector<Cargo>{
	
	
	public SelectorDeCargos() {
		super(Cargo.class, "Cargos");
		
	}
	
	@Override
	protected TableFormat<Cargo> getTableFormat() {
		String[] props=new String[]{
				"sucursal.nombre"
				,"origen"
				,"documento"
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"saldoCalculado"
				};
		String[] names=new String[]{
				"Sucursal"
				,"Origen"
				,"Folio"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Saldo"};
		return GlazedLists.tableFormat(Cargo.class,props,names);
	}
	
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(850,500));
	}
	
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "buscar");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}	
	
	
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	public Long folio=new Long(0);
	public Long sucursalId=0L;

	@Override
	protected List<Cargo> getData() {		
		String hql="from Venta v where v.documento=? and v.sucursal.id=?";
		List data= Services.getInstance()
		.getHibernateTemplate()
		.find(hql, new Object[]{
				folio
				,sucursalId}
		);
		System.out.println("Registros: "+data.size());
		return data;
	}
	
	public void clean(){
		source.clear();
	}
	
		
	
	public static Venta buscar(final Long numero,Long sucursalId){
		SelectorDeCargos selector=new SelectorDeCargos();
		selector.folio=numero;
		selector.sucursalId=sucursalId;
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			return (Venta)selector.getSelected();
		}		
		return null;
	}	

}
