package com.luxsoft.siipap.cxp.ui.form;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.CXPNota.Concepto;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * FormModel para el mantenimiento de Notas de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class NotaDeCreditoFormModel extends MasterDetailFormModel{

	public NotaDeCreditoFormModel() {
		super(CXPNota.class);
	}
	
	public NotaDeCreditoFormModel(final CXPNota nota){
		super(nota);
	}
	
	public CXPNota getNota(){
		return (CXPNota)getBaseBean();
	}
	
	@Override
	protected void initEventHandling() {
		getModel("total").addValueChangeListener(new TotalHandler());
		getModel("proveedor").addValueChangeListener(new ProveedorHandler());
		disponibleModel=new ValueHolder(getNota().getDisponible());
	}

	private void actualizarImportes(final double tot){
		CantidadMonetaria total=CantidadMonetaria.pesos(tot);
		CantidadMonetaria importe=MonedasUtils.calcularImporteDelTotal(total);
		CantidadMonetaria iva=MonedasUtils.calcularImpuesto(importe);
		setValue("importe", importe.amount());
		setValue("impuesto",iva.amount());		
	}
	
	protected EventList createPartidasSource(){
		return source;
	}
	
	@Override
	protected EventList createPartidasList(){
		EventList res=super.createPartidasList();
		if(getNota().getId()!=null){
			res.addAll(getNota().getAplicaciones());
		}
		return res;
	}
	
	@Override
	public Object insertDetalle(Object obj) {		
		return super.insertDetalle(obj);
	}

	/**
	 * 
	 * @return
	 */
	public CXPNota commit(){
		return getNota();
	}
	
	public boolean deleteDetalle(final Object obj){
		CXPAplicacion aplicacion=(CXPAplicacion)obj;
		boolean ok=getNota().eliminarAplicacion(aplicacion);		
		Assert.isTrue(ok,"La aplicacion no se encuentra en la nota posible problema con equals o hashcode");
		boolean mok=source.remove(aplicacion);
		Assert.isTrue(ok,"La aplicacion no se encuentra en el el EventList de las partidas");
		return mok;
		
	}
	
	public void procesarAplicaciones(final List<CXPCargo> cargos){
		Concepto c=getNota().getConcepto();
		switch (c) {
		case DESCUENTO:
			estimarImportesPorDescuento(cargos);
			break;
		case DESCUENTO_FINANCIERO:
			estimarImportesPorDescuentoFinanciero(cargos);
			break;
		case DEVLUCION:
		case BONIFICACION:
			procesarAplicacionesEstandar(cargos);
			break;
		default:
			break;
		}		
	}
	
	private void estimarImportesPorDescuento(final List<CXPCargo> cargos){
		for(CXPCargo cargo:cargos){
			//Garantizar misma moneda
			if(getNota().getMoneda().equals(cargo.getMoneda())){
				CXPAplicacion aplicacion=new CXPAplicacion();
				aplicacion.setCargo(cargo);
				aplicacion.setComentario("Aplicación automatica de nota");
				boolean res=getNota().agregarAplicacion(aplicacion);
				if(res){
					CXPFactura fac=(CXPFactura)cargo;
					CantidadMonetaria totalFactura=fac.getTotalCM();
					CantidadMonetaria totalAnalisis=new CantidadMonetaria(fac.getAnalizadoTotalCosto(),fac.getMoneda());
					CantidadMonetaria flete=fac.getTotalFlete();
					CantidadMonetaria otrosCargos=new CantidadMonetaria(fac.getCargos(),fac.getMoneda());
					CantidadMonetaria aplicable=totalFactura.subtract(totalAnalisis).subtract(flete).subtract(otrosCargos);
					if(aplicable.amount().doubleValue()>0)
						aplicacion.setImporte(aplicable.amount());
					else
						aplicacion.setImporte(BigDecimal.ZERO);
					source.add(aplicacion);					
				}
			}
		}
		setValue("total", getNota().getAplicadoCalculado());
	}
	
	private void estimarImportesPorDescuentoFinanciero(final List<CXPCargo> cargos){
		for(CXPCargo cargo:cargos){
			//Garantizar misma moneda
			if(getNota().getMoneda().equals(cargo.getMoneda())){
				CXPAplicacion aplicacion=new CXPAplicacion();
				aplicacion.setCargo(cargo);
				aplicacion.setComentario("Aplicación automatica de nota");
				boolean res=getNota().agregarAplicacion(aplicacion);
				if(res){
					CXPFactura fac=(CXPFactura)cargo;
					CantidadMonetaria tot=fac.getImporteDescuentoFinanciero2();
					aplicacion.setImporte(tot.amount());
					source.add(aplicacion);					
				}
			}
		}
		setValue("total", getNota().getAplicadoCalculado());
	}
	
	private void procesarAplicacionesEstandar(final List<CXPCargo> cargos){
		for(CXPCargo cargo:cargos){
			//Garantizar misma moneda
			if(getNota().getMoneda().equals(cargo.getMoneda())){
				CXPAplicacion aplicacion=new CXPAplicacion();
				aplicacion.setCargo(cargo);
				aplicacion.setComentario("Aplicación automatica de nota");
				boolean res=getNota().agregarAplicacion(aplicacion);
				if(res){
					aplicacion.setImporte(estimparImporteParaAplicar(cargo));
					source.add(aplicacion);					
				}
			}
		}
	}
	
	
	private BigDecimal estimparImporteParaAplicar(CXPCargo cargo){
		double saldo=cargo.getSaldo().doubleValue();
		double disponible=getDisponible().doubleValue();
		if(disponible<=0)
			return BigDecimal.ZERO;
		else if(saldo<=disponible)
			return BigDecimal.valueOf(saldo);
		else
			return BigDecimal.valueOf(disponible);
	}
	
	public BigDecimal getDisponible(){
		BigDecimal disponible=getNota().getTotal();
		BigDecimal aplicado=BigDecimal.ZERO;
		if(!permiteDisponible())
			return aplicado;
		for(CXPAplicacion a:getNota().getAplicaciones()){
			aplicado=aplicado.add(a.getImporte());
		}
		BigDecimal res=disponible.subtract(aplicado);		
		return res;
	}
	
	private void actualizarDisponible(){
		disponibleModel.setValue(getDisponible());
		setValue("total", getNota().getAplicadoCalculado());
	}	

	@Override
	protected void doListChange() {
		actualizarDisponible();		
		super.doListChange();
	}
	
	public void doListUpdated(ListEvent listChanges){
		actualizarDisponible();
		
	}
	
	public boolean isTotalMutable(){
		return (getNota().getConcepto().equals(Concepto.BONIFICACION)
				||
				getNota().getConcepto().equals(Concepto.DEVLUCION)
				);
	}

	public boolean permiteDisponible(){
		return (getNota().getConcepto().equals(Concepto.BONIFICACION)
				||
				getNota().getConcepto().equals(Concepto.DEVLUCION)
				);
	}
	
	private ValueModel disponibleModel;
	
	public ValueModel getDisponibleModel(){
		return disponibleModel;
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getDisponible().doubleValue()<0){
			support.addError("disponible", "El importe de las aplicaciones es incorrecto");
		}
	}

	private class TotalHandler implements PropertyChangeListener{
		
		public void propertyChange(PropertyChangeEvent evt) {
			BigDecimal valor=(BigDecimal)evt.getNewValue();
			if(valor==null)
				valor=BigDecimal.ZERO;
			actualizarImportes(valor.doubleValue());
			//actualizarDisponible();
			
		}
	}
	
	private class ProveedorHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			if(getNota().getId()==null){
				getNota().getAplicaciones().clear();
				source.clear();
			}
		}
		
	}
	
}
