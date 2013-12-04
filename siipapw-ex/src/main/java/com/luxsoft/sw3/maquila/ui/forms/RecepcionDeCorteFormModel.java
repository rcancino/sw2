package com.luxsoft.sw3.maquila.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.model.OrdenDeCorteDet;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorte;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeCortesPendientes;



/**
 * Controlador y PresentationModel para la fomra y mantenimiento de Entradas de material
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RecepcionDeCorteFormModel extends DefaultFormModel {
	
	private EventList<RecepcionDeCorteDet> partidasSource;
	
	protected Logger logger=Logger.getLogger(getClass());

	public RecepcionDeCorteFormModel() {
		super(new RecepcionDeCorte());
	}
	
	public RecepcionDeCorteFormModel(RecepcionDeCorte orden) {
		super(orden);
	}
	
	protected void init(){
		getModel("almacen").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				logger.info("Detectando almacen seleccionado: "+getRecepcion().getAlmacen());
				if(getRecepcion().getId()==null){
					getPartidasSource().clear();
					getRecepcion().getPartidas().clear();
					validate();
				}else{
					throw new IllegalArgumentException("El almacen no es modificable");
				}
			}
		});
		partidasSource=GlazedLists.eventList(getRecepcion().getPartidas());
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getRecepcion().getPartidas().isEmpty()){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		for(RecepcionDeCorteDet det:getRecepcion().getPartidas()){
			if(det.getEntrada()<=0)
				support.getResult().addError("El recepción del corte: "+det.getId()+ "  "+det.getProducto().getClave()+ " debe ser >0");
		}
		super.addValidation(support);
	}
	
	public EventList<RecepcionDeCorteDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public RecepcionDeCorte getRecepcion(){
		return (RecepcionDeCorte)getBaseBean();
	}
	
	public void insertar(){
		if(getRecepcion().getAlmacen()==null){
			return;
		}
		List<OrdenDeCorteDet> cortes= SelectorDeCortesPendientes.seleccionar(getRecepcion().getAlmacen());
		for(OrdenDeCorteDet corte:cortes){
			if(!partidasSource.contains(corte)){
				RecepcionDeCorteDet det=new RecepcionDeCorteDet(corte);
				DefaultFormModel model=new DefaultFormModel(det);
				RecepcionDeCorteDetForm form=new RecepcionDeCorteDetForm(model);
				form.open();
				if(!form.hasBeenCanceled()){
					det=(RecepcionDeCorteDet)model.getBaseBean();
					boolean ok=getRecepcion().agregarRecepcion(det);
					if(ok){
						afterInserPartida(det);
						partidasSource.add(det);
					}else{
						MessageUtils.showMessage("La orden unitaria ya esta registrada: "
								+det.getCorte().getId(), "Recepción de corte");
					}
				}
			}
		}
	}
	
	public void afterInserPartida(RecepcionDeCorteDet det){
		validate();
	}
	
	public void elminarPartida(int index){
		RecepcionDeCorteDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getRecepcion().getPartidas().remove(det);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void editar(int index){
		RecepcionDeCorteDet source=partidasSource.get(index);
		if(source!=null){
			RecepcionDeCorteDet target=beforeUpdate(source);
			DefaultFormModel model=new DefaultFormModel(target);
			RecepcionDeCorteDetForm form=new RecepcionDeCorteDetForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				afterUpdate(source, target);
				partidasSource.set(index, source);
				validate();
			}
		}
	}
	
	protected RecepcionDeCorteDet beforeUpdate(final RecepcionDeCorteDet source){
		RecepcionDeCorteDet target=new RecepcionDeCorteDet();
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log"});
		return target;
	}
	
	protected void afterUpdate(RecepcionDeCorteDet source,RecepcionDeCorteDet target){
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		validate();
	}
	
}
