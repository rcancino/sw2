package com.luxsoft.siipap.pos.ui.forms.caja;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;

public class PagoFormModel extends DefaultFormModel{
	
	private Sucursal sucursal;
	private FormaDePago[] formasDePago;

	public PagoFormModel() {
		super(Bean.proxy(PagoModel.class));
		getPago().setSucursal(getSucursal());
	}
	
	private Handler handler;
	private PagoValidatorSupport validatorSupport;
	
	@Override
	protected void init() {		
		handler=new Handler();			
		addBeanPropertyChangeListener(handler);
		validatorSupport=new PagoValidatorSupport();
	}
	
	

	public void dispose(){
		removePropertyChangeListener(handler);
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		validatorSupport.setPago(getPago());
		validatorSupport.validar(getPago(), support);
	}

	

	public PagoModel getPago(){
		return (PagoModel)getBaseBean();
	}
	
	public FormaDePago[] getFormasDePago(){
		if(formasDePago==null)
			formasDePago=FormaDePago.values();
		return formasDePago;
	}
	public void setFormasDePago(FormaDePago...dePagos){
		this.formasDePago=dePagos;
	}
	
	/**
	 * Metodo detonado posterior al a seleccion de forma de pago
	 */
	protected void formaDePagoAsignada(){
	}
	
	protected void reset(){
		PagoModel pago=(PagoModel)Bean.proxy(PagoModel.class);
		pago.setSucursal(getSucursal());
		wrapper.setWrappedInstance(pago);
		pmodel.setBean(wrapper.getWrappedInstance());
		validate();
	}
	
	public void applicar(){
		switch (getPago().getFormaDePago()) {
		case TARJETA_CREDITO:
		case TARJETA_DEBITO:
			boolean res=AutorizacionTarjetaForm.autorizar(getPago());
			if(!res)
				return; //No se persiste
			break;
		default:
			break;
		}
		
	}
	
	public Pago persistir(){
		try {
			
			getPago().setFecha(Services.getInstance().obtenerFechaDelSistema());
			Pago target=getPago().toPago();
			beforePersist(target);
			Pago pago=(Pago)Services.getInstance().getUniversalDao().save(target);
			String msg=MessageFormat.format("Pago registrado:" +
					"\nCliente:\t{0}," +
					"\nFecha:\t{1,date,short} Folio: {2} " +
					"\nTotal:\t{3}  " +
					"\nTipo:\t{4}" +
					"\nID:\t{5}"
					, pago.getNombre()
					,pago.getFecha()
					,pago.getFolio()					
					,pago.getTotalCM()
					,pago.getInfo()
					,pago.getId()
					);
			MessageUtils.showMessage(msg, "Pago");	
			afterPersist();
			return pago;
			
		} catch (Exception e) {
			//MessageUtils.showError("Error al persistir el pago ",e);
			JOptionPane.showMessageDialog(null, ExceptionUtils.getRootCauseMessage(e));
			e.printStackTrace();
			return null;
		}
	}
	
	public void beforePersist(final Pago target){
		if(target instanceof PagoConDeposito){
			PagoConDeposito deposito=(PagoConDeposito)target;
			String hql="from PagoConDeposito p where " +
					"p.cliente.clave=? " +
					"and p.fechaDeposito=? " +
					"and p.banco=? " +
					"and p.cuenta.id=? " +
					"and p.total=?";
			Object[] params={target.getClave(),deposito.getFechaDeposito()
					,deposito.getBanco()
					,deposito.getCuenta().getId()
					,deposito.getTotal()
					};
			List<PagoConDeposito> data=Services.getInstance().getHibernateTemplate().find(hql, params);
			if(!data.isEmpty()){
				String depositoExistente="Deposito ya existente:\n id:"+data.get(0).getId();
				throw new RuntimeException(depositoExistente);
			}
				
		}
		
	}
	
	public void afterPersist(){
		reset();
	}
	
	public Sucursal getSucursal() {
		if(sucursal==null)
			sucursal=Services.getInstance().getConfiguracion().getSucursal();
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	private class Handler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			if("formaDePago".equals(evt.getPropertyName())){
				System.out.println("Forma de pago asignada: "+evt.getNewValue());
				formaDePagoAsignada();
			}
			
		}
		
	}

}
