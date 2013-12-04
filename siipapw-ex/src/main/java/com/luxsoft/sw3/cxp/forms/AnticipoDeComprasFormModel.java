package com.luxsoft.sw3.cxp.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxp.model.AnticipoDeCompra;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;

public class AnticipoDeComprasFormModel extends DefaultFormModel implements PropertyChangeListener{

	public AnticipoDeComprasFormModel(AnticipoDeCompra a){
		super(a);
	}
	
	
	public AnticipoDeComprasFormModel() {
		super(AnticipoDeCompra.class);
		
	}
	public AnticipoDeCompra getAnticipo(){
		return (AnticipoDeCompra)getBaseBean();
	}

	public AnticipoDeCompra commit() {
		removeBeanPropertyChangeListener(this);
		return getAnticipo();
	}
	
	protected void init(){
		addBeanPropertyChangeListener(this);
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getAnticipo().getDescuentoFinanciero()>0){
			if(StringUtils.isBlank(getAnticipo().getDocumentoNota())){
				support.addError("descuentoFinanciero", "Se requiere el numero de la nota de credito para el D.F.");
			}
		}
		if(getAnticipo().getDescuento()>0){
			if(StringUtils.isBlank(getAnticipo().getDocumentoDescuentoComercial())){
				support.addError("", "Documento  para el descuento comercial?");
			}
		}
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName=evt.getPropertyName();
		
		if("documento".equals(propertyName)){
			resolverFactura();
		}else if("proveedor".equals(propertyName)){
			resolverFactura();
		}else if("importe".equals(propertyName)){
			actualizarImportes();
		}else if("descuento".equals(propertyName)){
			actualizarImportes();
		}else if("descuentoFinanciero".equals(propertyName)){
			actualizarImportes();
		}
		validate();
	}
	
	private void resolverFactura(){
		String fact=(String)getValue("documento");		
		if(!StringUtils.isEmpty(fact) && getAnticipo().getProveedor()!=null){
			//System.out.println("Validando factura: "+fact+ "Para prov: "+getAnticipo().getProveedor());
			CXPFactura existente=ServiceLocator2.getCXPFacturaManager()
					.buscarFactura(fact, getAnticipo().getProveedor());
			if(existente!=null){
				String pattern="Factura ya registrada id:{0} fecha={1,date,short}";
				MessageUtils.showMessage(MessageFormat.format(pattern, existente.getId(),existente.getFecha()), "Anticipos");
				getAnticipo().setDocumento(null);
			}
		}
	}
	private void actualizarImportes(){
		
		BigDecimal importe=getAnticipo().getImporte();
		double desc=getAnticipo().getDescuentoFinanciero();
		if(getAnticipo().getNota()!=null)
			if(getAnticipo().getNota().getId()!=null)
				return;
		
		getAnticipo().firePropertyChange("disponible", BigDecimal.ZERO, getAnticipo().getDisponible());
	}
	
	

}
