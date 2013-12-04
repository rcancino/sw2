package com.luxsoft.siipap.cxp.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.value.ComponentValueModel;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

public class RequisicionFormModel extends MasterDetailFormModel{
	
	
	public RequisicionFormModel() {
		super(Requisicion.class);		
	}

	public RequisicionFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public RequisicionFormModel(Object bean) {
		super(bean);		
	}
	
	public Requisicion getMasterBean(){
		return (Requisicion)getBaseBean();
	}
	
	
	protected void initEventHandling(){
		if(getMasterBean().getId()!=null ){
			for(Object  det:getMasterBean().getPartidas()){
				source.add(det);
			}
		}
		//Handlers		
		final MonedaHandler monedaHandler=new MonedaHandler();		
		getModel("moneda").addValueChangeListener(monedaHandler);
		definirTipoDeCambio();
		this.conDescuentoModel=new ValueHolder(Boolean.TRUE);
		this.conDescuentoModel.addValueChangeListener(new DescuentoHandler());
		
	}
	
	@SuppressWarnings("unchecked")
	public Object insertDetalle(final Object obj){
		if(obj!=null){
			RequisicionDe det=(RequisicionDe)obj;
			boolean resModel=getMasterBean().agregarPartida(det);
			if(resModel){
				source.add(det);
				return det;
			}
			return null;			
		}
		return null;
		
	}
	
	public void afterEdit(Object partida){
		int index=source.indexOf(partida);
		if(index!=-1){
			final Object element=source.get(index);
			// Actualizamos el modelo
			Object found=CollectionUtils.find(getMasterBean().getPartidas(), new Predicate(){
				public boolean evaluate(Object object) {
					return object.equals(element);
				}
			});
			if(found!=null){
				source.set(index, partida);
				getMasterBean().actualizarTotal();
				System.out.println("Debio mandar nuevo total: "+getMasterBean().getTotal());
			}else{
				logger.info("El bean en edicion no existe en el modelo no estan sicronizadas la colleccion del modelo con el GlazedList de partidas");
			}
		}else
			logger.info("El bean en edición no existe en el EventList de las partidas, probablemente existe un error en el equals del bean");
		
		
	}
	
	@Override
	protected void afeterPartidaInserted(Object partida) {
		
	}

	@Override
	protected void doListChange(){		
		getMasterBean().actualizarTotal();
	}
	
	
	public boolean deleteDetalle(final Object obj){
		RequisicionDe part=(RequisicionDe)obj;
		boolean res=getMasterBean().eleiminarPartida(part);
		if(res){
			return source.remove(part);
		}
		return false;
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getMasterBean().getTotal().amount().doubleValue()<=0)
			support.addError("Total", "Se requiere el importe de la requisición");
		for(RequisicionDe det:getMasterBean().getPartidas()){
			if(det.getTotal().amount().doubleValue()<=0){
				support.addError("partidas"
						,MessageFormat.format("La factura {0} tiene un importe incorrecto de: {1} ",det.getDocumento(),det.getTotal()));
			}
		}
	}

	protected void definirTipoDeCambio(){
		if(getValue("id")==null){
			if(getValue("moneda").equals(MonedasUtils.PESOS)){
				setValue("tipoDeCambio", BigDecimal.ONE);
				getComponentModel("tipoDeCambio").setEnabled(false);
			}else{				
				setValue("tipoDeCambio", BigDecimal.ONE);
				getComponentModel("tipoDeCambio").setEnabled(true);
			}
		}
	}
	
	public List<Concepto> getConceptosValidos(){
		final List<Concepto> data=ServiceLocator2.getUniversalDao().getAll(Concepto.class);
		CollectionUtils.filter(data, new Predicate(){
			public boolean evaluate(Object object) {
				Concepto cc=(Concepto)object;
				return  cc.getClave().equals("PAGO") ;
				/*
				if(getValue("id")==null)
					return cc.getClave().startsWith("ANTICIPO") || cc.getClave().startsWith("PAGO") ;
					
				else
					return  cc.getClave().startsWith("PAGO") ;*/ 
			}
		});
		return data;
	}
	
	public void procesarFacturas(final List<CXPFactura> facturas){
		for(CXPFactura fac:facturas){
			// Garantizar misma moneda
			if(getMasterBean().getMoneda().equals(fac.getMoneda())){
				RequisicionDe det=new RequisicionDe();
				det.setComentario("Pag Factura de compras");
				det.setFacturaDeCompras(fac);
				det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
				det.setTotal(fac.getPorRequisitar(isDescuentoFinanciero()));
				boolean res=getMasterBean().agregarPartida(det);
				if(res)
					source.add(det);
			}			
		}
	}
	
	public boolean isDescuentoFinanciero(){
		return (Boolean)conDescuentoModel.getValue();
	}
	
	private ValueHolder conDescuentoModel;
	private ComponentValueModel descuentoFinanciero;
	
	public ValueModel getConDescuentoModel(){
		return this.conDescuentoModel;
	}
	
	public ComponentValueModel getDFModel(){
		if(descuentoFinanciero==null){
			double df=getMasterBean().getProveedor()!=null?getMasterBean().getProveedor().getDescuentoFinanciero():0d;
			descuentoFinanciero=new ComponentValueModel(new ValueHolder(new Double(df)));
			descuentoFinanciero.setEnabled(isDescuentoFinanciero());
			descuentoFinanciero.addValueChangeListener(new DescuentoFHandler());
		}
		return descuentoFinanciero;
	}
	
	/**
	 * Listener que detecta cambios en la moneda  para aplicar las reglas
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class MonedaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(getValue("id")==null){
				getMasterBean().eliminarPartidas();
				source.clear();
				definirTipoDeCambio();
			}
			
		}
	}


	private class DescuentoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			for(RequisicionDe det:getMasterBean().getPartidas()){
				det.setTotal(det.getFacturaDeCompras().getPorRequisitar(isDescuentoFinanciero()));
			}
			getDFModel().setEnabled(isDescuentoFinanciero());
		}
	}
	
	public ValueModel totalModel=new ValueHolder(null);
	
	private class DescuentoFHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			double df=(Double)evt.getNewValue();
			
			CantidadMonetaria tot=new CantidadMonetaria(0d,getMasterBean().getMoneda());
			
			for(RequisicionDe det:getMasterBean().getPartidas()){
				det.getFacturaDeCompras().setDescuentoFinanciero(df);
				CantidadMonetaria porReq=det.getFacturaDeCompras().getPorRequisitarSimple(isDescuentoFinanciero());
				det.setTotal(porReq);
				tot=tot.add(porReq);
				int index = source.indexOf(det);
				if(index>=0)
					source.set(index,det);
				
			}
			//getMasterBean().actualizarTotal();
			validate();
			setValue("total", tot);
			totalModel.setValue(tot);
			System.out.println("Total...:   "+getValue("total"));
		}
	}

}
