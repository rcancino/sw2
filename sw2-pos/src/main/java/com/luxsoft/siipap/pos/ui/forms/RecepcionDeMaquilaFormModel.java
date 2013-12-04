package com.luxsoft.siipap.pos.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;

import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;


/**
 * Controlador y PresentationModel para la fomra y mantenimiento de Recepciones de maquila
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RecepcionDeMaquilaFormModel extends DefaultFormModel {
	
	private EventList<EntradaDeMaquila> partidasSource;
	private EventList<Proveedor> proveedores;
	private EventList<Producto> productos;
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public RecepcionDeMaquilaFormModel() {
		super(new RecepcionDeMaquila());
	}
	
	public RecepcionDeMaquilaFormModel(RecepcionDeMaquila recepcion) {
		super(recepcion);
	}
	
	protected void init(){
		if(getRecepcion().getSucursal()==null)
			getRecepcion().setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				logger.info("Detectando cambio de maquilador: "+getRecepcion().getProveedor().getNombreRazon());
				if(getRecepcion().getId()==null){
					getPartidasSource().clear();
					getProductos().clear();
					getRecepcion().getPartidas().clear();
					validate();
				}else{
					throw new IllegalArgumentException("El maquilador no es modificable");
				}
			}
		});
		if(getRecepcion().getId()==null){
			partidasSource=GlazedLists.eventList(new ArrayList<EntradaDeMaquila>());
		}
		else
			partidasSource=GlazedLists.eventList(getRecepcion().getPartidas());
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getRecepcion().getProveedor()==null){
			support.addError("", "El maquilador es mandatorio");
		}
		if(getRecepcion().getPartidas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		if(StringUtils.isBlank(getRecepcion().getRemision())){
			support.getResult().addError("La referencia/entrada del maquilador es mandatoria");
		}
		super.addValidation(support);
	}
	
	@SuppressWarnings("unchecked")
	private void actualizarProductos(){
		if(getRecepcion().getProveedor()!=null){
			String hql="select pp.producto from Proveedor p left join p.productos pp where p.id=?";
			getProductos().clear();
			getProductos().addAll(getHibernateTemplate()
					.find(hql,getRecepcion().getProveedor().getId()));			
		}
	}
	
	public EventList<EntradaDeMaquila> getPartidasSource() {
		return partidasSource;
	}	
	
	public RecepcionDeMaquila getRecepcion(){
		return (RecepcionDeMaquila)getBaseBean();
	}
	
	public EventList<Proveedor> getProveedores(){
		if(proveedores==null){
			proveedores=new BasicEventList<Proveedor>();
			proveedores.addAll(getHibernateTemplate().find("from Proveedor p where p.maquilador=true"));
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
		if(getRecepcion().getProveedor()==null)
			return;
		EntradaDeMaquila target=new EntradaDeMaquila();
		target.setRecepcion(getRecepcion());
		target.setSucursal(getRecepcion().getSucursal());
		DefaultFormModel model=new DefaultFormModel(target){			
			protected void addValidation(PropertyValidationSupport support) {
				super.addValidation(support);
				double cantidad=(Double)getValue("cantidad");
				if(cantidad<=0)
					support.getResult().addError("La cantidad debe ser >0");
			}			
		};
		RecepcionDeMaquilaDetForm form=new RecepcionDeMaquilaDetForm(model);
		if(getProductos().isEmpty()){
			actualizarProductos();
		}
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			EntradaDeMaquila det=(EntradaDeMaquila)model.getBaseBean();
			int renglon=partidasSource.size()+1;
			det.setRenglon(renglon);
			partidasSource.add(det);
			//asignarRenglones();
			getRecepcion().agregarPartida(det);
			afterInserPartida(det);
		}
		form.setProductos(null);
	}
	
	public void afterInserPartida(EntradaDeMaquila det){		
		validate();
	}
	
	public void elminarPartida(int index){
		EntradaDeMaquila det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getRecepcion().eleiminarPartida(det);
			if(ok){
				partidasSource.remove(index);
				for(int i=0;i<partidasSource.size();i++){
					EntradaDeMaquila row=partidasSource.get(i);
					row.setRenglon(i+1);
					partidasSource.set(i, row);
				}
				//asignarRenglones();
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void editar(int index){
		EntradaDeMaquila source=partidasSource.get(index);
		
		if(source!=null){
			EntradaDeMaquila target=beforeUpdate(source);
			DefaultFormModel model=new DefaultFormModel(target);
			RecepcionDeMaquilaDetForm form=new RecepcionDeMaquilaDetForm(model);
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
	
	private void asignarRenglones2(){
		int ren=1;
		for(EntradaDeMaquila e:getRecepcion().getPartidas()){
			e.setRenglon(ren++);
		}
	}
	
	protected EntradaDeMaquila beforeUpdate(final EntradaDeMaquila source){
		EntradaDeMaquila target=new EntradaDeMaquila();
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log"});
		return target;
	}
	
	protected void afterUpdate(EntradaDeMaquila source,EntradaDeMaquila target){
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		validate();
	}
	
	private HibernateTemplate getHibernateTemplate(){
		return Services.getInstance().getHibernateTemplate();
	}
	
}
