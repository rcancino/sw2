package com.luxsoft.sw3.maquila.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;


/**
 * Controlador y PresentationModel para la fomra y mantenimiento de Entradas de material
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RecepcionDeMaterialFormModel extends DefaultFormModel {
	
	private EventList<EntradaDeMaterialDet> partidasSource;
	private EventList<Proveedor> proveedores;
	private EventList<Producto> productos;
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public RecepcionDeMaterialFormModel() {
		super(new EntradaDeMaterial());
	}
	
	public RecepcionDeMaterialFormModel(EntradaDeMaterial recepcion) {
		super(recepcion);
	}
	
	protected void init(){
		getModel("almacen").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				logger.info("Detectando almacen: "+getRecepcion().getAlmacen());
				if(getRecepcion().getId()==null){
					getPartidasSource().clear();
					getProductos().clear();
					getRecepcion().getPartidas().clear();
					validate();
				}else{
					throw new IllegalArgumentException("El almacen no es modificable");
				}
			}
		});
		if(getRecepcion().getId()==null){
			partidasSource=GlazedLists.eventList(new ArrayList<EntradaDeMaterialDet>());
		}
		else
			partidasSource=GlazedLists.eventList(getRecepcion().getPartidas());
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getRecepcion().getPartidas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		if(StringUtils.isBlank(getRecepcion().getEntradaDeMaquilador())){
			support.getResult().addError("La referencia/entrada del maquilador es mandatoria");
		}
		super.addValidation(support);
	}
	
	@SuppressWarnings("unchecked")
	private void actualizarProductos(){
		if(getRecepcion().getAlmacen().getMaquilador()!=null){
			String hql="select pp.producto from Proveedor p left join p.productos pp where p.id=?";
			getProductos().clear();
			getProductos().addAll(ServiceLocator2.getHibernateTemplate()
					.find(hql,getRecepcion().getAlmacen().getMaquilador().getId()));			
		}
	}
	
	public EventList<EntradaDeMaterialDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public EntradaDeMaterial getRecepcion(){
		return (EntradaDeMaterial)getBaseBean();
	}
	
	public EventList<Proveedor> getProveedores(){
		if(proveedores==null){
			proveedores=new BasicEventList<Proveedor>();
			proveedores.addAll(ServiceLocator2.getProveedorManager().buscarActivos());
		}
		return proveedores;
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
		}
		return productos;
	}
	
	public void insertar(){
		EntradaDeMaterialDet target=new EntradaDeMaterialDet();
		target.setRecepcion(getRecepcion());
		target.setAlmacen(getRecepcion().getAlmacen());
		DefaultFormModel model=new DefaultFormModel(target);
		RecepcionDeMaterialDetForm form=new RecepcionDeMaterialDetForm(model);
		if(getProductos().isEmpty()){
			actualizarProductos();
		}
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			EntradaDeMaterialDet det=(EntradaDeMaterialDet)model.getBaseBean();
			boolean ok=getRecepcion().agregarEntrada(det);
			if(ok){
				afterInserPartida(det);
				partidasSource.add(det);
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Recepción de bobinas");
			}
		}
		form.setProductos(null);
	}
	
	public void afterInserPartida(EntradaDeMaterialDet det){
		
		validate();
	}
	
	public void elminarPartida(int index){
		EntradaDeMaterialDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getRecepcion().eliminarEntrada(det);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void editar(int index){
		EntradaDeMaterialDet source=partidasSource.get(index);
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
		}
	}
	
	protected EntradaDeMaterialDet beforeUpdate(final EntradaDeMaterialDet source){
		EntradaDeMaterialDet target=new EntradaDeMaterialDet();
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log"});
		return target;
	}
	
	protected void afterUpdate(EntradaDeMaterialDet source,EntradaDeMaterialDet target){
		//TODO Presentar la forma para ajustar descuentos ??
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		validate();
	}
	
	
}
