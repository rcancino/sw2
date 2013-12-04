package com.luxsoft.sw3.cxp.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.BeanUtils;


import com.jgoodies.binding.value.ComponentValueModel;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
//import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.selectores.SelectorDeAnalisisPorRequisitar;
import com.luxsoft.sw3.cxp.selectores.SelectorDeFacturasDeCompras;

public class RequisicionDeComprasFormModel extends MasterDetailFormModel{
	
	
	public RequisicionDeComprasFormModel() {
		super(Requisicion.class);		
	}

	public RequisicionDeComprasFormModel(Object bean, boolean readOnly) {
		super(bean, readOnly);
	}

	public RequisicionDeComprasFormModel(Object bean) {
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
		final AsignadorDeTipoDeCambio astc=new AsignadorDeTipoDeCambio();
		getModel("fecha").addValueChangeListener(astc);
		
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
	
	public void edit(Object obj){
		RequisicionDe dd=(RequisicionDe)obj;
		int index=this.partidasSource.indexOf(source);
		System.out.println("Row: "+index+ "  Docto: "+dd.getDocumento());
		if(obj!=null && (index!=-1)){
			RequisicionDe source=(RequisicionDe)obj;
			RequisicionDe target=new RequisicionDe();
			BeanUtils.copyProperties(source, target,new String[]{"id","requisicion"});
			DefaultFormModel model=new DefaultFormModel(target,false);
			RequisicionDeComprasDetForm form=new RequisicionDeComprasDetForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				RequisicionDe res=(RequisicionDe)model.getBaseBean();				
				partidasSource.set(index, res);
				getMasterBean().actualizarTotal();
				totalModel.setValue(getMasterBean().getTotal());
			}
		}
	}
	

	@Override
	protected void doListChange(){	
		System.out.println("Partidas de requ modificadas: ");
		//getMasterBean().actualizarTotal();
	}
	
	
	public boolean deleteDetalle(final Object obj){
		RequisicionDe part=(RequisicionDe)obj;
		boolean res=getMasterBean().eleiminarPartida(part);
		if(res){
			getMasterBean().actualizarTotal();
			totalModel.setValue(getMasterBean().getTotal());
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
			}else if(getValue("moneda").equals(MonedasUtils.DOLARES)){
				asignarTipoDeCambio();
				getComponentModel("tipoDeCambio").setEnabled(true);
			}else{				
				setValue("tipoDeCambio", BigDecimal.ONE);
				getComponentModel("tipoDeCambio").setEnabled(true);
			}
		}
	}
	
	private void asignarTipoDeCambio(){
		double tc=ServiceLocator2.buscarTipoDeCambio(getMasterBean().getFecha());
		setValue("tipoDeCambio", BigDecimal.valueOf(tc));
	}
	
	public List<Concepto> getConceptosValidos(){
		final List<Concepto> data=ServiceLocator2.getUniversalDao().getAll(Concepto.class);
		CollectionUtils.filter(data, new Predicate(){
			public boolean evaluate(Object object) {
				Concepto cc=(Concepto)object;
				return  cc.getClave().equals("PAGO") || cc.getClave().equals("ANTICIPO");
				 
			}
		});
		return data;
	}
	
	public void insertar(){
		if(getMasterBean().getConcepto()!=null){
			if(getMasterBean().getConcepto().getClave().equals("PAGO")){
				insertarAnalisis();
			}else{
				insertarFactura();
			}
		}
	}
	
	public void insertarFactura(){
		final Proveedor p=getMasterBean().getProveedor();
		final Currency moneda=getMasterBean().getMoneda();
		if(p!=null){
			SelectorDeFacturasDeCompras selector = new SelectorDeFacturasDeCompras(){
				@Override
				protected List<CXPFactura> getData() {
					String hql="from CXPFactura f where f.proveedor.id=? and f.anticipof=true  and f.moneda=? and f.saldoReal>0";
					return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{p.getId(),moneda});
				}
			};
			selector.open();
			CXPFactura fac=selector.getSelected();
			if(fac!=null){
				if(getMasterBean().getMoneda().equals(fac.getMoneda())){
					RequisicionDe det=new RequisicionDe();
					det.setComentario("Pag Factura de compras");
					det.setFacturaDeCompras(fac);
					det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
					det.setTotal(new CantidadMonetaria(fac.getSaldoCalculado(),moneda));
					boolean res=getMasterBean().agregarPartida(det);
					if(res)
						source.add(det);
					actualizarPartidas();
				}	
			}
		}
	}
	
	private void insertarAnalisis(){
		List<AnalisisDeFactura> analisis=SelectorDeAnalisisPorRequisitar.buscarFacturas(
				getMasterBean().getProveedor(), 
				getMasterBean().getMoneda());
		if(!analisis.isEmpty()){
			procesarAnalisis(analisis);
		}
	}
	
	private void procesarAnalisis(final List<AnalisisDeFactura> analisis){
		for(AnalisisDeFactura a:analisis){
			if(a.getRequisicionDet()!=null){
				MessageUtils.showMessage("La factura "+a.getFactura().getDocumento()+" ya existe en la requisición: "+a.getRequisicionDet().getRequisicion().getId()
						, "Requisición de compras");
				continue;
			}
			CXPFactura fac=a.getFactura();
			// Garantizar misma moneda
			if(getMasterBean().getMoneda().equals(fac.getMoneda())){
				RequisicionDe det=new RequisicionDe();
				det.setComentario("Pag Factura de compras");
				det.setFacturaDeCompras(fac);
				det.setAnalisis(a);
				a.setRequisicionDet(det);
				det.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
				//det.setTotal(fac.getPorRequisitar(isDescuentoFinanciero()));
				boolean res=getMasterBean().agregarPartida(det);
				if(res)
					source.add(det);
			}			
		}
		actualizarPartidas();
	}
	
	public void procesarFacturas(final List<String> numeros){
		Proveedor p=getMasterBean().getProveedor();
		List<AnalisisDeFactura> analisis=ServiceLocator2.getAnalisisDeCompraManager().buscarAnalisisPorFactura(numeros, p);
		procesarAnalisis(analisis);
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
			if(getMasterBean().getId()!=null){
				for(RequisicionDe det:getMasterBean().getPartidas()){
					if(det.getFacturaDeCompras()!=null){
						if(det.getFacturaDeCompras().getDescuentoFinanciero()!=0){
							df=det.getFacturaDeCompras().getDescuentoFinanciero();
						}
					}
				}
			}
			descuentoFinanciero=new ComponentValueModel(new ValueHolder(new Double(df)));
			descuentoFinanciero.setEnabled(isDescuentoFinanciero());
			descuentoFinanciero.addValueChangeListener(new DescuentoFHandler());
			//conDescuentoModel.addValueChangeListener(new DescuentoHandler());
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

	private class AsignadorDeTipoDeCambio implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			asignarTipoDeCambio();
		}
	}
	
	

	private class DescuentoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			
			actualizarPartidas();
			getDFModel().setEnabled(isDescuentoFinanciero());
		}
	}
	
	public ValueModel totalModel=new ValueHolder(getMasterBean().getTotal());
	
	private class DescuentoFHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {			
			actualizarPartidas();
		}
	}
	
	
	
	private void actualizarPartidas(){
		
		CantidadMonetaria tot=new CantidadMonetaria(0d,getMasterBean().getMoneda());
		
		for(RequisicionDe det:getMasterBean().getPartidas()){
			
			AnalisisDeFactura analisis=det.getAnalisis();
			if(analisis!=null){
				
				CantidadMonetaria ta=new CantidadMonetaria(analisis.getImporte(),getMasterBean().getMoneda());
				ta=ta.add(MonedasUtils.calcularImpuesto(ta));
				if(analisis.isPrimerAnalisis()){					
					CXPFactura fac=analisis.getFactura();
					 ta=new CantidadMonetaria(fac.getTotalAnalizadoConFlete(),fac.getMoneda());
					/*ta=ta.add(fac.getFleteMN()
							.add(fac.getImpuestoFleteMN())
							.subtract(fac.getRetencionFleteMN())
							);*/
					if(isDescuentoFinanciero()){
						//ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, descuentoFinanciero.doubleValue());
						ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, descuentoFinanciero.doubleValue());
						det.getFacturaDeCompras().setDescuentoFinanciero(descuentoFinanciero.doubleValue());
					}
					ta=ta.subtract(analisis.getFactura().getBonificadoCM());
				}else{
					if(isDescuentoFinanciero()){
						ta=MonedasUtils.aplicarDescuentosEnCascadaBase100(ta, descuentoFinanciero.doubleValue());
						det.getFacturaDeCompras().setDescuentoFinanciero(descuentoFinanciero.doubleValue());
					}
				}
				det.setTotal(ta);
				//tot=tot.add(det.getTotal());
				int index = source.indexOf(det);
				if(index>=0)
					source.set(index,det);
			}				
			det.setImporte(MonedasUtils.calcularImporteDelTotal(det.getTotal()));
			det.setImpuesto(MonedasUtils.calcularImpuesto(det.getImporte()));
			tot=tot.add(det.getTotal());
		}
		validate();
		//getMasterBean().setTotal(tot);
		
		getMasterBean().actualizarTotal();
		totalModel.setValue(getMasterBean().getTotal());
		
	}

}
