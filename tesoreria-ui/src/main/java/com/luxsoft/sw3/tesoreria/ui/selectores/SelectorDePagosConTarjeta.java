package com.luxsoft.sw3.tesoreria.ui.selectores;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.util.DBUtils;

public class SelectorDePagosConTarjeta extends AbstractSelector<PagoConTarjeta>{

	public Long sucursalId;
	public Periodo periodo;
	
	
	public SelectorDePagosConTarjeta() {
		super(PagoConTarjeta.class, "Selector de pagos con tarjeta");
		periodo=Periodo.getPeriodo(-5);
	}

	@Override
	protected TableFormat<PagoConTarjeta> getTableFormat() {
		String[] props={"sucursal.nombre","clave","nombre","fecha","primeraAplicacion"
				,"tarjeta.nombre","total"};
		String[] names={"Sucursal","Cliente","Nombre","Fecha","Cobranza"
				,"Tarjeta","Total"};
		return GlazedLists.tableFormat(PagoConTarjeta.class,props,names);
	}

	@Override
	protected List<PagoConTarjeta> getData() {
		if(getSucursalId()==null){
			String hql="from PagoConTarjeta p where date(p.fecha) between ?  and ?"
			//+" and p.id not in(select s.pago.id from CorteDeTarjetaDet s)"
				;
			Object[] params={					
					getPeriodo().getFechaInicial()
					,getPeriodo().getFechaFinal()
					};
			List<PagoConTarjeta> res=ServiceLocator2.getHibernateTemplate().find(hql,params);
			return res;
			
		}else{
			String hql="from PagoConTarjeta p where date(p.fecha) between ?  and ?" +
			" and p.sucursal.id=? " 
			//+" and p.id not in(select s.pago.id from CorteDeTarjetaDet s)"
			;
			Object[] params={					
					getPeriodo().getFechaInicial()
					,getPeriodo().getFechaFinal()
					,getSucursalId()
					};
			List<PagoConTarjeta> res=ServiceLocator2.getHibernateTemplate().find(hql,params);
			return res;
		}
	}
	
	protected void addButton(ToolBarBuilder builder){
		DispatchingAction a=new DispatchingAction(this,"cambiarTarjeta");
		a.putValue(Action.NAME, "Cambiar Tarjeta");
		a.putValue(Action.SHORT_DESCRIPTION, "Cambio de tarjeta");
		a.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/creditcards.png"));
		builder.add(a);
	}

	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(700,400));
	}
	
	
	
	@Override
	protected TextFilterator<PagoConTarjeta> getBasicTextFilter() {
		TextFilterator<PagoConTarjeta> filterator=GlazedLists.textFilterator("total","sucursal.nombre","tarjeta.nombre");
		return filterator;
	}

	public void cambiarTarjeta(){
		PagoConTarjeta selected=getSelected();
		int index=source.indexOf(selected);
		
		if(selected!=null && (index!=-1)){
			SelectorDeTarjeta sel=new SelectorDeTarjeta();
			sel.open();
			if(!sel.hasBeenCanceled()){
				Tarjeta tar=sel.getTarjeta();
				selected.setTarjeta(tar);
				selected=(PagoConTarjeta)ServiceLocator2.getUniversalDao().save(selected);
				source.set(index, selected);
			}
		}
	}
	
	public void clear(){
		this.source.clear();
	}

	public Long getSucursalId() {
		return sucursalId;
	}

	public void setSucursalId(Long sucursalId) {
		this.sucursalId = sucursalId;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}
	
	public static List<PagoConTarjeta> seleccionar(Long sucursalId){
		SelectorDePagosConTarjeta selector=new SelectorDePagosConTarjeta();
		selector.setSucursalId(sucursalId);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<PagoConTarjeta> res=new ArrayList<PagoConTarjeta>(selector.getSelectedList());
			selector.clear();
			return res;
		}
		return ListUtils.EMPTY_LIST;
	}
	

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DBUtils.whereWeAre();
				//seleccionar(3L);
				SelectorDePagosConTarjeta selector=new SelectorDePagosConTarjeta();
				selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
				selector.setPeriodo(new Periodo("01/11/2010","30/11/2010"));
				selector.open();
				System.exit(0);
			}

		});
	}

}
