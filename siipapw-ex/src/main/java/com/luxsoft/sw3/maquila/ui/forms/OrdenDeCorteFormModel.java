package com.luxsoft.sw3.maquila.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;

import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.OrdenDeCorte;
import com.luxsoft.sw3.maquila.model.OrdenDeCorteDet;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeBobinasDisponibles;


/**
 * Controlador y PresentationModel para la fomra y mantenimiento de Entradas de material
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class OrdenDeCorteFormModel extends DefaultFormModel {
	
	private EventList<OrdenDeCorteDet> partidasSource;
	private EventList<Producto> productos;
	
	protected Logger logger=Logger.getLogger(getClass());

	public OrdenDeCorteFormModel() {
		super(new OrdenDeCorte());
	}
	
	public OrdenDeCorteFormModel(OrdenDeCorte orden) {
		super(orden);
	}
	
	protected void init(){
		getModel("almacen").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				logger.info("Detectando almacen seleccionado: "+getOrden().getAlmacen());
				if(getOrden().getId()==null){
					getPartidasSource().clear();
					getOrden().getCortes().clear();
					validate();
				}else{
					throw new IllegalArgumentException("El almacen no es modificable");
				}
			}
		});
		if(getOrden().getId()==null){
			partidasSource=GlazedLists.eventList(new ArrayList<OrdenDeCorteDet>());
		}
		else
			partidasSource=GlazedLists.eventList(getOrden().getCortes());
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getOrden().getCortes().isEmpty()){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		super.addValidation(support);
	}
	
	@SuppressWarnings("unchecked")
	private void actualizarProductos(){
		if(getOrden()!=null){
			if(getOrden().getAlmacen().getMaquilador()!=null){
				String hql="select pp.producto from Proveedor p left join p.productos pp where p.id=?";
				getProductos().clear();
				getProductos().addAll(ServiceLocator2.getHibernateTemplate()
						.find(hql,getOrden().getAlmacen().getMaquilador().getId()));			
			}
		}
	}
	
	public EventList<OrdenDeCorteDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public OrdenDeCorte getOrden(){
		return (OrdenDeCorte)getBaseBean();
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
		}
		return productos;
	}
	
	public void insertar(){
		if(getOrden().getAlmacen()==null){
			return;
		}
		EntradaDeMaterialDet origen=SelectorDeBobinasDisponibles.seleccionar(getOrden().getAlmacen());
		if(origen==null)
			return;
		OrdenDeCorteDet target=new OrdenDeCorteDet();
		target.setOrigen(origen);
		target.setOrden(getOrden());
		target.setAlmacen(getOrden().getAlmacen());
		DefaultFormModel model=new DefaultFormModel(target){
			protected void addValidation(PropertyValidationSupport support) {
				OrdenDeCorteDet det=(OrdenDeCorteDet)getBaseBean();
				if(det!=null){
					if(!det.validarDisponibleKilos())
						support.getResult().addError("Kilos > a disponible en la entrada");
				}
			}
			
		};
		OrdenDeCorteDetForm form=new OrdenDeCorteDetForm(model);
		if(getProductos().isEmpty()){
			actualizarProductos();
		}
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			OrdenDeCorteDet det=(OrdenDeCorteDet)model.getBaseBean();
			boolean ok=getOrden().addCorte(det);
			if(ok){
				afterInserPartida(det);
				partidasSource.add(det);
			}else{
				MessageUtils.showMessage("La entrada ya esta registrada: "+det.getDestino(), "Orden de corte");
			}
		}
		form.setProductos(null);
		
	}
	
	public void afterInserPartida(OrdenDeCorteDet det){
		validate();
	}
	
	public void elminarPartida(int index){
		OrdenDeCorteDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getOrden().getCortes().remove(det);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void editar(int index){
		/*EntradaDeMaterialDet source=partidasSource.get(index);
		if(source!=null){
			EntradaDeMaterialDet target=beforeUpdate(source);
			DefaultFormModel model=new DefaultFormModel(target);
			RecepcionDeMaterialDetForm form=new RecepcionDeMaterialDetForm(model);
			if(getProductos().isEmpty()){
				actualizarProductos();
			}
			form.setProductos(getProductos());
			form.open();
			if(!form.hasBeenCanceled()){
				afterUpdate(source, target);
				partidasSource.set(index, source);
				validate();
			}
		}*/
	}
	
	protected OrdenDeCorteDet beforeUpdate(final OrdenDeCorteDet source){
		OrdenDeCorteDet target=new OrdenDeCorteDet();
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log"});
		return target;
	}
	
	protected void afterUpdate(OrdenDeCorteDet source,OrdenDeCorteDet target){
		//TODO Presentar la forma para ajustar descuentos ??
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		validate();
	}
	
	
}
