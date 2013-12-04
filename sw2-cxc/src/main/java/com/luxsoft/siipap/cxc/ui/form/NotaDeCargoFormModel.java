package com.luxsoft.siipap.cxc.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.rules.RevisionDeCargosRules;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.MasterDetailFormModel;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

public class NotaDeCargoFormModel extends MasterDetailFormModel{
	
	private ValueModel importeHabilitado=new ValueHolder(Boolean.FALSE);
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	
	private boolean especial=false;
	
	

	public NotaDeCargoFormModel() {
		super(Bean.proxy(NotaDeCargo.class));
		
	}
	
	protected EventList createPartidasSource(){
		return source;
	}
	
	@Override
	protected void initEventHandling(){
		getModel("importe").addValueChangeListener(new ImporteHandler());
		getModel("impuesto").addValueChangeListener(new ImpuestoHandler());
		getModel("cargo").addValueChangeListener(new CargoHandler());
		getModel("especial").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getNewValue().equals(Boolean.TRUE)){
					getCargo().setComentario("CARGO ESPECIAL");
					getCargo().setCargo(1.0d);
				}else{
					getCargo().setComentario("");
				}
			}
		});
	}

	public void asignarFolio(){
		getCargo().setDocumento(new Long(ServiceLocator2.getCXCManager().buscarProximaNotaDeCargo()));
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		/*Long documento=(Long)getValue("documento");
		if((documento==null) || (documento<=0))
			support.addError("documento", "El folio es mandatorio");*/
		if(getCargo().getTotal()!=null && getCargo().getTotal().doubleValue()<=0)
			support.addError("total", "El Importe del cargo no es correcto");
		for(Object obj:source){
			NotaDeCargoDet det=(NotaDeCargoDet)obj;
			if(!det.validarCargo()){
				support.addError("", "El Importe del cargo no es correcto: Fac:"+det.getVenta().getDocumento());
			}
		}
	}

	protected void doListChange(){
		super.doListChange();
		importeHabilitado.setValue(source.isEmpty());
		BigDecimal imp=BigDecimal.ZERO;
		for(Object row:source){
			NotaDeCargoDet det=(NotaDeCargoDet)row;
			det.actualizarImporte();
			
			if(getCargo().isEspecial()){
				det.setSaldo(getSaldoEspecial(det.getVenta()));
				det.setImporte(det.getSaldo().multiply(BigDecimal.valueOf(.01)));
			}else{
				if(det.getVenta()!=null)
					det.setSaldo(det.getVenta().getSaldoCalculado());
			}
				
			imp=imp.add(det.getImporte());
		}
		setValue("importe", imp);
		if(getCargo().isEspecial()){
			BigDecimal val=getCargo().getImporte();
			getCargo().setImporte(BigDecimal.ZERO);
			getCargo().setImpuesto(val);
			getCargo().setTotal(val);			
		}
		
	}
	
	public void doListUpdated(ListEvent listChanges){
		doListChange();
	}
	
	public boolean deleteDetalle(final Object obj){
		getCargo().getConceptos().remove(obj);
		return source.remove(obj);
	}

	public ValueModel getImporteHabilitado() {
		return importeHabilitado;
	}
	
	public NotaDeCargo getCargo(){
		return (NotaDeCargo)getBaseBean();
	}
	
	private void actualizarTotal(){
		setValue("impuesto", MonedasUtils.calcularImpuesto(getCargo().getImporte()));
		setValue("total", MonedasUtils.calcularTotal(getCargo().getImporte()));		
	}
	
	
	public void agregarVenta(Cargo v) {
		NotaDeCargoDet det=new NotaDeCargoDet();
		det.setVenta(v);
		String pattern="Suc:{0} Docto:{1,number,#######} ({2,date,short})";
		det.setComentario(MessageFormat.format(pattern, v.getSucursal().getNombre(),v.getDocumento(),v.getFecha()));
		if(getCargo().getCargo()!=0){
			BigDecimal dob=BigDecimal.valueOf(getCargo().getCargo()/100);
			BigDecimal imp=v.getTotal().multiply(dob);
			det.setImporte(imp);
			//String pattern="{0} Documento:{1} Fecha:{2,date,short}";
			//det.setComentario(MessageFormat.format(pattern, arguments));
			//det.setComentario("Cargo relacionado con la factura:"+v.getDocumento());
		}		
		if(source.size()<5000){
			getCargo().agregarConcepto(det);
			source.add(det);
			actualizarImporteEnPartidas();
		}
		
			
	}
	
	private BigDecimal getSaldoEspecial(final Cargo cargo){
		BigDecimal total=cargo.getTotal();
		String hql="select sum(a.importe) from Aplicacion a where a.cargo.id=? and a.fecha<=?";
		List<BigDecimal> res=ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{cargo.getId(),DateUtil.toDate("31/12/2009")});
		if(res.get(0)==null)
			return cargo.getImporte();
		BigDecimal aplicaciones=res.get(0);
		BigDecimal saldo=total.subtract( aplicaciones);
				
		return BigDecimal.valueOf(saldo.doubleValue()/1.15);
	}
	
	public void actualizarImporteEnPartidas(){		
		for(int index=0;index<source.size();index++){
			NotaDeCargoDet det=(NotaDeCargoDet)source.get(index);
			det.setCargo(getCargo().getCargo());
			/*BigDecimal dob=BigDecimal.valueOf(getCargo().getCargo()/100);
			BigDecimal imp=det.getVenta().getTotal().multiply(dob);
			det.setImporte(imp);*/
			det.actualizarImporte();
			
			source.set(index, det);
		}
	}


	/**
	 * Termina de procesar el bean para su persitencia
	 * TODO Analizar si esta operacion se debe trasladar a una clase
	 * en la capa del modelo de dominio y persistencia
	 */
	public NotaDeCargo commit(){
		
		getCargo().setSaldo(getCargo().getTotal());
		//Fijar todas las propiedades requeridas
		NotaDeCargo target=new NotaDeCargo();
		Bean.normalizar(getCargo(), target, new String[]{"conceptos"});
		target.setOrigen(getOrigen());
		target.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
		target.setFechaRecepcionCXC(new Date());
		for(NotaDeCargoDet det:getCargo().getConceptos()){
			target.agregarConcepto(det);
		}
		//if(target.getDiaRevision()==0)
			//target.setDiaRevision(7);
		//Date vto=DateUtils.addDays(target.getFecha(), target.getCliente().getPlazo());
		//RevisionDeCargosRules.instance().actualizar(target, target.getFecha());
		target.setVencimiento(target.getFecha());
		return target;
	}
	
	
	
	public OrigenDeOperacion getOrigen() {
		return origen;
	}


	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
		if(getCargo()!=null)
			getCargo().setOrigen(origen);
	}


	

	public boolean isEspecial() {
		return especial;
	}

	public void setEspecial(boolean especial) {
		this.especial = especial;
	}




	private class ImporteHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(getCargo().isEspecial()){
				
			}else
				actualizarTotal();
		}		
	}
	
	private class ImpuestoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			//System.out.println("Venta especial asingando impuesto manual");
			if(getCargo().isEspecial()){
				//System.out.println("Venta especial asingando impuesto manual");
				getCargo().setImporte(BigDecimal.ZERO);
				setValue("total", getCargo().getImpuesto());
			}
					
		}		
	}
	
	private class CargoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			actualizarImporteEnPartidas();
		}		
	}

}
