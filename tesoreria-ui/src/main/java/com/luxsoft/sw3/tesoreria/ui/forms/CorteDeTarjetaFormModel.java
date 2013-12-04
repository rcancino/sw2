package com.luxsoft.sw3.tesoreria.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjetaDet;
import com.luxsoft.sw3.tesoreria.ui.selectores.SelectorDePagosConTarjeta;




/**
 * Controlador y PresentationModel para la forma y mantenimiento de corte de tarjetas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CorteDeTarjetaFormModel extends DefaultFormModel {
	
	private EventList<CorteDeTarjetaDet> partidasSource;
	
	protected Logger logger=Logger.getLogger(getClass());

	public CorteDeTarjetaFormModel() {
		super(new CorteDeTarjeta());
	}
	
	public CorteDeTarjetaFormModel(CorteDeTarjeta corte) {
		super(corte);
	}
	
	protected void init(){
		
		PropertyChangeListener handler1=new PropertyChangeListener() {			
			public void propertyChange(PropertyChangeEvent evt) {
				logger.info("Detectando sucursal seleccionado: "+getCorte().getSucursal());
				if(getCorte().getId()==null){
					clear();
				}else{
					throw new IllegalArgumentException("La sucursal no es modificable");
				}
			}
		};
		
		getModel("sucursal").addValueChangeListener(handler1);
		getModel("tipoDeTarjeta").addValueChangeListener(handler1);
		
		partidasSource=GlazedLists.eventList(getCorte().getPartidas());		
	}
	
	private void clear(){
		if(getCorte().getId()==null){
			getPartidasSource().clear();
			getCorte().getPartidas().clear();
			validate();
		}
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getCorte().getPartidas().isEmpty()){
			//support.addError("", "Debe registrar por lo menos un pago");
		}		
		super.addValidation(support);
	}
	
	public EventList<CorteDeTarjetaDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public CorteDeTarjeta getCorte(){
		return (CorteDeTarjeta)getBaseBean();
	}
	
	public void insertar(){
		if(getCorte().getSucursal()==null){
			return;
		}		
		SelectorDePagosConTarjeta selector=new SelectorDePagosConTarjeta(){

			@Override
			protected List<PagoConTarjeta> getData() {
				String hql="from PagoConTarjeta p where p.fecha=? " +
						" and p.sucursal.id=? " +
						" and p.id not in(select s.pago.id from CorteDeTarjetaDet s)";
				String tipoTarjeta=getCorte().getTipoDeTarjeta();
				if(tipoTarjeta.equals(CorteDeTarjeta.TIPOS_DE_TARJETAS[1])){
					hql+=" and p.tarjeta.nombre like \'%AMERICAN%\'";
				}else{
					hql+=" and p.tarjeta.nombre not like \'%AMERICAN%\'";
				}
				Object[] params={getCorte().getCorte(),getCorte().getSucursal().getId()};
				List<PagoConTarjeta> res=ServiceLocator2.getHibernateTemplate().find(hql,params);
				return res;
			}
			
		};
		selector.setTitle("Pagos con tarjeta pendientes de corte");
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<PagoConTarjeta> pagos=new ArrayList<PagoConTarjeta>(selector.getSelectedList());
			for(PagoConTarjeta pago:pagos){
				CorteDeTarjetaDet det=new CorteDeTarjetaDet();
				det.setPago(pago);
				det.setCorte(getCorte());
				boolean ok=getCorte().agregarPartida(det);
				if(ok){
					afterInserPartida(det);
					partidasSource.add(det);
				}
			}
		}
		
	}
	
	public void afterInserPartida(CorteDeTarjetaDet pago){
		getCorte().actualizarTotal();
		validate();
	}
	
	public void elminarPartida(int index){
		CorteDeTarjetaDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getCorte().eliminarPartida(det);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		getCorte().actualizarTotal();
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void editar(int index){
		
	}
	
	protected PagoConTarjeta beforeUpdate(final PagoConTarjeta source){
		return source;
	}

	public CorteDeTarjeta commit() {
		CorteDeTarjeta corte=getCorte();
		corte.actualizarTotal();
		return corte;
	}
	
	
	
}
