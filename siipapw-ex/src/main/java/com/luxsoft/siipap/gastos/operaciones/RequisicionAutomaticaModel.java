package com.luxsoft.siipap.gastos.operaciones;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class RequisicionAutomaticaModel extends MasterDetailFormModel{
	
	

	public RequisicionAutomaticaModel() {
		super(Requisicion.class);		
	}

	public RequisicionAutomaticaModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public RequisicionAutomaticaModel(Object bean) {
		super(bean);		
	}
	
	public Requisicion getMasterBean(){
		return (Requisicion)getBaseBean();
	}
	
	@SuppressWarnings("unchecked")
	protected void init(){
		//super.init();
		
		source=createPartidasList();
		source.addListEventListener(new PartidasHandler());
		partidasSource=source;
		if(!getMasterBean().getPartidas().isEmpty()){
			//inicializarPartidas();
			for(Object  det:getMasterBean().getPartidas()){
				source.add(det);
			}
		}
		
		//Handlers		
		final MonedaHandler monedaHandler=new MonedaHandler();		
		addBeanPropertyChangeListener(monedaHandler);
		//getModel("tipoDeCambio").addValueChangeListener(monedaHandler);
		actualizarRequisicion();
		validate();
	}
	
	
	
	protected void addValidation(PropertyValidationSupport support){
		for(RequisicionDe det:getMasterBean().getPartidas()){
			if(StringUtils.isBlank(det.getDocumento())){
				support.addError("CXPFactura", "El número de facturo/documento es obligatorio para todas las partidas");
			}
		}
	}
	
	
	
	protected EventList createPartidasList(){
		final Connector connector=GlazedLists.beanConnector(RequisicionDe.class);
		final ObservableElementList list=new ObservableElementList(super.createPartidasList(),connector);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public Object insertDetalle(final Object obj){
		if(obj!=null){
			RequisicionDe det=(RequisicionDe)obj;
			boolean resModel=getMasterBean().agregarPartida(det);
			if(resModel){
				det.actualizarImportesDeGastosProrrateado();
				source.add(det);
				return det;
			}
			return null;			
		}
		return null;
		
	}
	
	@Override
	protected void afeterPartidaInserted(Object partida) {
		getMasterBean().actualizarTotal();
	}

	@Override
	protected void doListChange(){
		debugImportes(" Antes de doListChange");
		getMasterBean().actualizarTotal();
		debugImportes(" Despues de doListChange");
		super.doListChange();
	}
	
	
	public boolean deleteDetalle(final Object obj){
		RequisicionDe part=(RequisicionDe)obj;
		boolean res=getMasterBean().eleiminarPartida(part);
		if(res){
			return source.remove(part);
		}
		return false;
	}
	
	protected void actualizarRequisicion(){
		/*
		if(getValue("moneda").equals(MonedasUtils.PESOS)){
			setValue("tipoDeCambio", BigDecimal.ONE);
			getComponentModel("tipoDeCambio").setEnabled(false);
		}else{
			setValue("tipoDeCambio", BigDecimal.ONE);
			getComponentModel("tipoDeCambio").setEnabled(true);
		}
		*/
		debugImportes("Antes de actualizarRequisicion");
		getMasterBean().actualizarTotal();
		debugImportes("Descpues de actualizarRequisicion");
	}
	
	
	/**
	 * Listener que detecta cambios en la moneda  para aplicar las reglas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class MonedaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("moneda".equals(evt.getPropertyName())){
				if(logger.isDebugEnabled()){
					logger.debug("Cambio de moneda detectado");
				}
				//definirTipoDeCambio();
				//actualizarRequisicion();
			}if("tipoDeCambio".equals(evt.getPropertyName())){
				actualizarRequisicion();
			}
		}
	}

	
	protected class PartidasHandler implements ListEventListener{
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				switch (listChanges.getType()) {
				case ListEvent.INSERT:
				case ListEvent.DELETE:
				case ListEvent.UPDATE:
					doListChange();
					break;
				default:
					break;
				}				
			}
		}		
	}
	
	private void debugImportes(String where){
		
		System.out.println("DEBUG EN: "+where);
		System.out.println("\tImp: "+getMasterBean().getImporte());
		System.out.println("\tTax: "+getMasterBean().getImpuesto());
		System.out.println("\tTot: "+getMasterBean().getTotal());
		
	}
	

}
