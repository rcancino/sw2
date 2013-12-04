package com.luxsoft.sw3.contabilidad.ui.form;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.PolizasManager;

/**
 * Controlador y PresentationModel para la fomra de mantenimiento de Polizas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaFormModel extends DefaultFormModel {
	
	private EventList<PolizaDet> partidasSource;
	private EventList<CuentaContable> cuentas;
	
	
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public PolizaFormModel() {
		super(new Poliza());
	}
	
	public PolizaFormModel(Poliza poliza) {
		super(poliza);
		
	}
	
	protected void init(){
		partidasSource=GlazedLists.eventList(getPoliza().getPartidas());
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getPoliza().getPartidas().isEmpty()){
			support.getResult().addError("Se requieren partidas en la póliza");
			
		}else if(getPoliza().getCuadre().abs().doubleValue()>0){
			support.getResult().addError("Descuadre: "+getPoliza().getCuadre());
		}
		//getPoliza().firePropertyChange("cuadre", null, getPoliza().getCuadre());
	}
	
	
	
	public EventList<PolizaDet> getPartidasSource() {
		return partidasSource;
	}	
	public Poliza getPoliza(){
		return (Poliza)getBaseBean();
	}
	
	public EventList<CuentaContable> getPartidas(){
		if(cuentas==null){
			cuentas=new BasicEventList<CuentaContable>();
			Comparator c=GlazedLists.beanPropertyComparator(CuentaContable.class, "id");
			cuentas=new UniqueList<CuentaContable>(cuentas,c);
			cuentas.addAll(ServiceLocator2.getHibernateTemplate().find("from CuentaContable c left join fetch c.conceptos"));
		}
		return cuentas;
	}
	
	public void insertar(){		
		PolizaDet target=new PolizaDet();
		final DefaultFormModel model=new DefaultFormModel(target);
		final PolizaDetForm form=new PolizaDetForm(model);
		form.setCuentas(getPartidas());
		form.open();
		if(!form.hasBeenCanceled()){
			getPoliza().getPartidas().add(target);
			target.setPoliza(getPoliza());
			target.setRenglon(getPoliza().ultimoRenglon++);
			System.out.println(" Partida agregada: "+target+ " Renglon: "+target.getRenglon());
			partidasSource.add(target);
			getPoliza().actualizar();
			validate();
		}
		
	}
	
	
	
	
	
	
	
	
	public void elminarPartida(PolizaDet det){
		int index=partidasSource.indexOf(det);
		System.out.println("Eliminando: "+det+  "    Index: "+index+ " Renglon: "+det.getRenglon());
		
		if(index!=-1){
			boolean ok=getPoliza().getPartidas().remove(det);
			if(ok){				
				System.out.println("Partidas: "+getPoliza().getPartidas().size());
				partidasSource.remove(index);
				getPoliza().actualizar();
				validate();
				return;
			}
		}
		validate();
	}
	
	public void editar(int index){
		//Assert.isNull(getPoliza().getCierre(),"Poliza cerrada ilegal modificarla");
		PolizaDet source=partidasSource.get(index);
		PolizaDet proxy=beforeUpdate(source);
		final DefaultFormModel model=new DefaultFormModel(proxy);		
		final PolizaDetForm form=new PolizaDetForm(model);
		form.setCuentas(ServiceLocator2.getUniversalDao().getAll(CuentaContable.class));
		form.open();
		if(!form.hasBeenCanceled()){
			afterUpdate(source, proxy);
			partidasSource.set(index, source);
			getPoliza().actualizar();
			validate();
		}
	}
	
	public void view(int index){
		PolizaDet source=partidasSource.get(index);
		PolizaDet proxy=beforeUpdate(source);
		final DefaultFormModel model=new DefaultFormModel(proxy,true);
		final PolizaDetForm form=new PolizaDetForm(model);
		form.open();
	}
	
	protected PolizaDet beforeUpdate(final PolizaDet source){
		PolizaDet target=(PolizaDet)Bean.proxy(PolizaDet.class);
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log"});
		return target;
	}
	
	protected void afterUpdate(PolizaDet source,PolizaDet target){
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		
	}
	
	
	
	protected PolizasManager getManager(){
		return ServiceLocator2.getPolizasManager();
	}
	
}
