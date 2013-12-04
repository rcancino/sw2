package com.luxsoft.sw3.maquila.ui.forms;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeBobinasParaAnalisis;


/**
 * Controlador y PresentationModel para la fomra y mantenimiento al analisis de material
 * {@link AnalisisDeMaterial}
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AnalisisDeMaterialFormModel extends DefaultFormModel {
	
	private EventList<EntradaDeMaterialDet> partidasSource;
	private EventList<Proveedor> proveedores;
	private EventList<Producto> productos;
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public AnalisisDeMaterialFormModel() {
		super(new AnalisisDeMaterial());
	}
	
	public AnalisisDeMaterialFormModel(AnalisisDeMaterial analisis) {
		super(analisis);
	}
	
	protected void init(){		
		if(getAnalisis().getId()==null){
			partidasSource=GlazedLists.eventList(new ArrayList<EntradaDeMaterialDet>());
		}
		else
			partidasSource=GlazedLists.eventList(getAnalisis().getEntradas());
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getAnalisis().getEntradas().size()==0){
			//support.addError("", "Debe registrar por lo menos una partida");
		}
		if(StringUtils.isBlank(getAnalisis().getFactura())){
			support.addError("", "Debe registrar la factura");
		}
		super.addValidation(support);
	}
	
	public EventList<EntradaDeMaterialDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public AnalisisDeMaterial getAnalisis(){
		return (AnalisisDeMaterial)getBaseBean();
	}
	
	public EventList<Proveedor> getProveedores(){
		if(proveedores==null){
			proveedores=GlazedLists.eventList(getHibernateTemplate().find("from Proveedor p where p.maquilador=true"));
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
		for(EntradaDeMaterialDet target:SelectorDeBobinasParaAnalisis.seleccionar()){
			DefaultFormModel model=new DefaultFormModel(target);
			AnalisisDeMaterialDetForm form=new AnalisisDeMaterialDetForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				EntradaDeMaterialDet det=(EntradaDeMaterialDet)model.getBaseBean();
				boolean ok=getAnalisis().agregarEntrada(det);
				if(ok){
					afterInserPartida(det);
					partidasSource.add(det);
					getAnalisis().actualizar();
				}else{
					MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
				}
			}
		}
		
	}
	
	public void afterInserPartida(EntradaDeMaterialDet det){		
		validate();
	}
	
	public void elminarPartida(int index){
		EntradaDeMaterialDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getAnalisis().eliminarEntrada(det);
			if(ok){
				partidasSource.remove(index);
				getAnalisis().actualizar();
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
			AnalisisDeMaterialDetForm form=new AnalisisDeMaterialDetForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				afterUpdate(source, target);
				partidasSource.set(index, source);
				getAnalisis().actualizar();
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
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		validate();
	}
	
	private HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
	
}
