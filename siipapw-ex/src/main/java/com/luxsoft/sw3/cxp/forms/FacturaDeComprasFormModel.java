package com.luxsoft.sw3.cxp.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPUtils;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.ui.selectores.SelectorDeCOMS;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * FormModel para el mantenimiento de analisis y revision de facturas de compras
 * 
 * @author Ruben Cancino
 *
 */
public class FacturaDeComprasFormModel extends DefaultFormModel implements PropertyChangeListener,ListEventListener<AnalisisDeFacturaDet>{
	
	private ValueModel editarFactura=new ValueHolder(true);
	
	private EventList<AnalisisDeFacturaDet> analisisDeEntradas;
	private AnalisisDeFactura analisis;

	public FacturaDeComprasFormModel() {
		super(CXPFactura.class);
		analisis=new AnalisisDeFactura();
		analisis.setFactura(getFactura());
		getFactura().agregarAnalisis(analisis);
		initListeners();
	}
	
	public FacturaDeComprasFormModel(AnalisisDeFactura analisis){
		super(analisis.getFactura());
		this.analisis=analisis;
		getFactura().setComentarioAnalisis(analisis.getComentario());
		initListeners();
	}
		
	public CXPFactura getFactura(){
		return (CXPFactura)getBaseBean();
	}
	
	public AnalisisDeFactura getAnalisis(){
		return analisis;
	}
	
	public ValueModel getEditarFactura() {
		return editarFactura;
	}
	
	protected void init(){
	}
	
	private void initListeners(){
		BigDecimal importeAnalizado=getAnalisis().getImporte();
		getFactura().setImporteAnalizado(importeAnalizado);
		getFactura().setImpuestoAnalizado(MonedasUtils.calcularImpuesto(importeAnalizado));
		getFactura().setTotalAnalizado(MonedasUtils.calcularTotal(importeAnalizado));
		
		addBeanPropertyChangeListener(this);
		final ImporteHandler handler=new ImporteHandler();
		getModel("importe").addValueChangeListener(handler);
		getModel("flete").addValueChangeListener(handler);
		getModel("cargos").addValueChangeListener(handler);
		analisisDeEntradas=GlazedLists.eventList(getAnalisis().getPartidas());
		analisisDeEntradas.addListEventListener(this);
	}
	
	
	
	public void commit(){
		analisisDeEntradas.removeListEventListener(this);
		AnalisisDeFactura a=getAnalisis();
		for(AnalisisDeFacturaDet det:a.getPartidas()){
			System.out.println("Det: "+det);
		}
		getAnalisis().setComentario(getFactura().getComentarioAnalisis());
	}
	
	public EventList<AnalisisDeFacturaDet> getAnalisisDeEntradas() {
		return analisisDeEntradas;
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {		
		if(getAnalisis().getFactura()!=null){
			if(getAnalisis().getFactura().getDocumento()!=null){ //Para evitar la validacion desde el bean
				if(StringUtils.isBlank(getAnalisis().getFactura().getDocumento()))
					support.addError("documento", "El folio de la factura no puede ser nulo");
			}
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName=evt.getPropertyName();
		//System.out.println("Actualizando; "+propertyName);
		if("documento".equals(propertyName)){
			resolverFactura();
		}else if("proveedor".equals(propertyName)){
			actualizarVencimiento();
		}else if("moneda".equals(propertyName)){
			for(int index=0;index<analisisDeEntradas.size();index++){
				AnalisisDeFacturaDet det=analisisDeEntradas.get(index);
				calcularPrecios(det);
				analisisDeEntradas.set(index, det);
			}
			//getAnalisis().actualizarTotalesAnalizado()
		}
		validate();
	}
	
	private void resolverFactura(){
		String fact=(String)getValue("documento");
		System.out.println("Buscando factura: "+fact);
		if(!StringUtils.isEmpty(fact) && getFactura().getProveedor()!=null){
			CXPFactura existente=ServiceLocator2.getCXPFacturaManager()
					.buscarFactura(fact, getFactura().getProveedor());
			if(existente!=null){
				if(MessageUtils.showConfirmationMessage(
						"Factura ya registrada con Id: "+existente.getId()+"\n Desea agregar un nuevo análisis ?"
						, "Registro de facturas CXP")){
					wrapper.setWrappedInstance(existente);
					pmodel.setBean(existente);
					getMainModel().setBean(existente);
					getFactura().setDocumento(fact);
					
					//analisisDeEntradas.removeListEventListener(this);
					analisis=new AnalisisDeFactura();
					analisis.setPrimerAnalisis(false);
					getFactura().agregarAnalisis(analisis);
					analisisDeEntradas.clear();
					analisisDeEntradas.addAll(getAnalisis().getPartidas());
					//analisisDeEntradas.addListEventListener(this);
					actualizarAcumulado();
					validate();
					this.editarFactura.setValue(Boolean.FALSE);
					//this.editarFactura.setValue(Boolean.FALSE);
				}else{
					getFactura().setDocumento(null);
					analisis.setPrimerAnalisis(true);
					validate();
					this.editarFactura.setValue(Boolean.TRUE);
				}
			}
		}
	}
	
	private void actualizarVencimiento(){
		final CXPFactura factura=getFactura();
		final Proveedor p=factura.getProveedor();		
		if(p!=null){
			Date fecha=factura.getFecha();
			Date revision=fecha;
			if(factura.getRecibo()!=null)
				revision=factura.getRecibo().getRecibo().getFecha();
			Date vto   = CXPUtils.calcularVencimiento(revision, fecha, p);
			Date vtoDf = CXPUtils.calcularVencimientoDescuentoF(revision, fecha,p);
			//factura.setFecha(fecha);
			factura.setRevision(revision);
			factura.setVencimiento(vto);
			factura.setVencimientoDF(vtoDf);
		}
	}
	
	public void insertarEntradas(){
		Proveedor prov=getFactura().getProveedor();
		if(prov!=null){
			List<EntradaPorCompra> entradas=SelectorDeCOMS.buscarEntradas(prov);
			CollectionUtils.forAllDo(entradas, new Closure(){
				public void execute(Object input) {
					EntradaPorCompra e=(EntradaPorCompra)input;
					Assert.isTrue(e.getProveedor().equals(getAnalisis().getFactura().getProveedor()));
					AnalisisDeFacturaDet det=new AnalisisDeFacturaDet();
					det.setEntrada(e);
					det.setCantidad(e.getPendienteDeAnalisis());
					if(getAnalisis().agregarPartida(det)){
						calcularPrecios(det);
						analisisDeEntradas.add(det);
						getAnalisis().actualizarTotalesAnalizado();
					}
				}
			});
		}	
	}
	
	private void calcularPrecios(final AnalisisDeFacturaDet det){
		Proveedor prov=getFactura().getProveedor();
		if(prov!=null){
			Producto prod=det.getEntrada().getProducto();
			Currency moneda=getAnalisis().getFactura().getMoneda();
			Date fecha=det.getEntrada().getFechaCompra();
			ListaDePreciosDet precio=null;
			if(fecha!=null)			
				precio=CXPServiceLocator.getInstance().getListaDePreciosDao().buscarPrecioVigente(prod, moneda,prov,fecha);
			if(precio==null) return;
			Assert.isTrue(moneda.equals(precio.getPrecio().getCurrency()),"El precio no es de la misma moneda");
			if(logger.isDebugEnabled()){
				logger.debug(MessageFormat
						.format("Aplicando precio {0} para producto:{1}\n Lista utilizada: {2}", precio.getPrecio(),det.getEntrada().getProducto(),precio.getLista().getId()));
			}
			det.setPrecio(precio.getPrecio().amount());
			det.setDesc1(precio.getDescuento1());
			det.setDesc2(precio.getDescuento2());
			det.setDesc3(precio.getDescuento3());
			det.setDesc4(precio.getDescuento4());
			det.calcularImporte();
		}
	}
	
	
	
	public void eliminarAnalisis(final int index){
		AnalisisDeFacturaDet det=this.analisisDeEntradas.get(index);
		if(det!=null){
			boolean eliminado= getAnalisis().eliminarPartida(det);
			if(eliminado){
				this.analisisDeEntradas.remove(det);
				getAnalisis().actualizarTotalesAnalizado();
			}
		}
	}
	
	public AnalisisDeFacturaDet editar(AnalisisDeFacturaDet source){
		//final AnalisisDeFacturaDet source=this.analisisDeEntradas.get(index);
		final AnalisisDeFacturaDet target=new AnalisisDeFacturaDet();
		target.setEntrada(source.getEntrada());
		BeanUtils.copyProperties(source, target,new String[]{"id","analisis"});
		final AnalisisDetForm form=new AnalisisDetForm(target);
		form.open();
		if(!form.hasBeenCanceled()){
			//System.out.println("Props: "+ToStringBuilder.reflectionToString(target,ToStringStyle.MULTI_LINE_STYLE));
			BeanUtils.copyProperties(target, source,new String[]{"id","analisis"});
			source.setPrecio(target.getPrecio());
			//this.analisisDeEntradas.set(index, source);
			return source;
		}	
		return null;	
	}


	/*
	 * Detecta cambios en las partidas analizadas
	 * 
	 */
	public void listChanged(ListEvent<AnalisisDeFacturaDet> listChanges) {
		while(listChanges.hasNext()){
			listChanges.next();
		}
		System.out.println("Acualizando partidas analizadas..");
		BigDecimal importeAnalizado=BigDecimal.ZERO;
		for(AnalisisDeFacturaDet det:this.analisisDeEntradas){
			importeAnalizado=importeAnalizado.add(det.getImporte());
		}
		getAnalisis().actualizarTotalesAnalizado();
		//importeAnalizado=getAnalisis().getImporte();
		getFactura().setImporteAnalizado(importeAnalizado);
		getFactura().setImpuestoAnalizado(MonedasUtils.calcularImpuesto(importeAnalizado));
		getFactura().setTotalAnalizado(MonedasUtils.calcularTotal(importeAnalizado));
		System.out.println("Importe del analisis: "+getFactura().getImporteAnalizado());
		actualizarAcumulado();
		validate();
	}
	
	private void actualizarAcumulado(){
		if(getFactura().getId()==null){
			BigDecimal analizadoAcu=getFactura().getAnalizadoTotal();
			analizadoAcu=analizadoAcu.add(getFactura().getTotalAnalizado());
			getFactura().setAnalizadoAcumulado(analizadoAcu);
			
			getFactura().setPendienteDeAnalizar(getFactura().getTotal().subtract(analizadoAcu));
		}
		
	}
	
	
	private class ImporteHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			CXPFactura fac=getFactura();
			CantidadMonetaria importe=CantidadMonetaria.pesos(fac.getImporte().doubleValue());
			CantidadMonetaria cargos=CantidadMonetaria.pesos(fac.getCargos().doubleValue());
			importe=importe.add(cargos);
			CantidadMonetaria impuesto=MonedasUtils.calcularImpuesto(importe);
			
			CantidadMonetaria flete=CantidadMonetaria.pesos(fac.getFlete().doubleValue());
			CantidadMonetaria impuestoFlete=CantidadMonetaria.pesos(fac.getImpuestoflete().doubleValue());
			CantidadMonetaria retFlete=CantidadMonetaria.pesos(fac.getRetencionflete().doubleValue());
			CantidadMonetaria total=importe.add(impuesto).add(flete).add(impuestoFlete).subtract(retFlete);
			fac.setImpuesto(impuesto.amount());
			fac.setTotal(total.amount());
			validate();
		}		
	}

}
