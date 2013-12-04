package com.luxsoft.siipap.cxp.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPCargo;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * FormModel para el mantenimiento de Notas de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnticipoFormModel extends MasterDetailFormModel{

	public AnticipoFormModel() {
		super(CXPAnticipo.class);
	}
	
	public AnticipoFormModel(final CXPAnticipo nota){
		super(nota);
	}
	
	public CXPAnticipo getAnticipo(){
		return (CXPAnticipo)getBaseBean();
	}
	
	@Override
	protected void initEventHandling() {
		getModel("total").addValueChangeListener(new TotalHandler());
		getModel("proveedor").addValueChangeListener(new ProveedorHandler());
		disponibleModel=new ValueHolder(getAnticipo().getDisponible());
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
		if(getAnticipo().getId()!=null){
			res.addAll(getAnticipo().getAplicaciones());
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
	public CXPAnticipo commit(){
		return getAnticipo();
	}
	
	
	public boolean deleteDetalle(final Object obj){
		
		CXPAplicacion aplicacion=(CXPAplicacion)obj;
		boolean ok=getAnticipo().eliminarAplicacion(aplicacion);		
		Assert.isTrue(ok,"La aplicacion no se encuentra en el pago posible problema con equals o hashcode");
		boolean mok=source.remove(aplicacion);
		Assert.isTrue(ok,"La aplicacion no se encuentra en el el EventList de las partidas");
		return mok;
		
	}
	
	public void procesarAplicaciones(final List<CXPCargo> cargos){
		for(CXPCargo cargo:cargos){
			//Garantizar misma moneda
			if(getAnticipo().getMoneda().equals(cargo.getMoneda())){
				CXPAplicacion aplicacion=new CXPAplicacion();
				aplicacion.setCargo(cargo);
				aplicacion.setComentario("Aplicación automatica de pago");
				boolean res=getAnticipo().agregarAplicacion(aplicacion);
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
		BigDecimal disponible=getAnticipo().getTotal();
		BigDecimal aplicado=BigDecimal.ZERO;
		for(CXPAplicacion a:getAnticipo().getAplicaciones()){
			aplicado=aplicado.add(a.getImporte());
		}
		BigDecimal res=disponible.subtract(aplicado);		
		return res;
	}
	
	private void actualizarDisponible(){
		disponibleModel.setValue(getDisponible());
	}	

	@Override
	protected void doListChange() {
		actualizarDisponible();		
		super.doListChange();
	}
	
	public boolean manejaTotalesEstandares(){
		return false;
	}
	
	public void doListUpdated(ListEvent listChanges){
		actualizarDisponible();
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
		for(CXPAplicacion aplicacion:getAnticipo().getAplicaciones()){
			if(aplicacion.getImporte().doubleValue()<=0){
				String pattern="El importe por aplicar a la factura {0} es incorrecto";
				support.addError("", MessageFormat.format(pattern, aplicacion.getCargo().getDocumento()));
			}
		}
	}

	private class TotalHandler implements PropertyChangeListener{
		
		public void propertyChange(PropertyChangeEvent evt) {
			BigDecimal valor=(BigDecimal)evt.getNewValue();
			if(valor==null)
				valor=BigDecimal.ZERO;
			actualizarImportes(valor.doubleValue());
			actualizarDisponible();
			
		}
	}
	
	private class ProveedorHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			if(getAnticipo().getId()==null){
				getAnticipo().getAplicaciones().clear();
				source.clear();
			}
			
			
		}
		
	}
}
