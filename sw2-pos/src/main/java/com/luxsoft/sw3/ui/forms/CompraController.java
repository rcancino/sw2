package com.luxsoft.sw3.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoPorProveedor;
import com.luxsoft.siipap.model.core.Proveedor;

import com.luxsoft.siipap.pos.ui.selectores.SelectorDeProductos2;

import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.ComprasManager;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.selectores.LookupUtils;

/**
 * Controlador y PresentationModel para la fomra de mantenimiento de Compra
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CompraController extends DefaultFormModel {
	
	private EventList<CompraUnitaria> partidasSource;
	private EventList<Proveedor> proveedores;
	private EventList<Producto> productos;
	private boolean supervisor=false;
	
	protected Logger logger=Logger.getLogger(getClass());

	public CompraController() {
		super(new Compra2());
		prepararCompraNueva();
	}
	
	public CompraController(Compra2 compra) {
		super(compra);
		preparaCompraExistente();
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getCompra().getPartidas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		super.addValidation(support);
	}


	protected void prepararCompraNueva(){
		if(getValue("id")==null)
			setValue("sucursal", Services.getInstance().getConfiguracion().getSucursal());
		setValue("fecha", Services.getInstance().obtenerFechaDelSistema());
		getModel("proveedor").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				//System.out.println("Detectando proveedor: "+getCompra().getProveedor());
				if(getCompra().getId()==null){
					//System.out.println("Detectando proveedor  2: "+getCompra().getProveedor());
					getPartidasSource().clear();
					getProductos().clear();
					getCompra().getPartidas().clear();
					validate();
				}
			}
		});
	}
	
	protected void preparaCompraExistente(){
		if(getCompra().getId()==null)
			prepararCompraNueva();
		
		
	}
	
	public EventList<CompraUnitaria> getPartidasSource() {
		return partidasSource;
	}

	protected void init(){
		/*
		if(getCompra().getPartidas().isEmpty())
			partidasSource=GlazedLists.eventList(new ArrayList<CompraUnitaria>());
		else
			partidasSource=GlazedLists.eventList(getCompra().getPartidas());
			*/
		partidasSource=GlazedLists.eventList(getCompra().getPartidas());
		//Preparar de solo lectura si la compra esta cerrada
		setReadOnly(getCompra().getCierre()!=null);
	}
	
	public Compra2 getCompra(){
		return (Compra2)getBaseBean();
	}
	
	public EventList<Proveedor> getProveedores(){
		if(proveedores==null){
			proveedores=new BasicEventList<Proveedor>();
			LookupUtils.getDefault().loadProveedores(proveedores);
		}
		return proveedores;
	}
	
	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
			//productos.addAll(ServiceLocator2.getProductoManager().buscarProductosActivos());			
		}
		return productos;
	}
	
	private void actualizarProductos(){
		if(getCompra().getProveedor()!=null){
			String hql="select pp.producto from Proveedor p left join p.productos pp where p.id=?";
			getProductos().clear();
			getProductos().addAll(Services.getInstance().getHibernateTemplate().find(hql,getCompra().getProveedor().getId()));			
		}
	}
	
	public void insertar(){
		if(getCompra().getCierre()!=null)
			return; //No se pueden agregar partidas a una compra cerrada
		CompraUnitaria target=new CompraUnitaria();
		target.setSucursal(getCompra().getSucursal());
		DefaultFormModel model=new DefaultFormModel(target);
		CompraDetForm form=new CompraDetForm(model);
		if(getProductos().isEmpty())
			actualizarProductos();
		form.setProductos(getProductos());
		form.setSupervisor(isSupervisor());
		if(isSupervisor())
			form.setSucursales(LookupUtils.getDefault().getSucursales());
		form.open();
		if(!form.hasBeenCanceled()){
			final CompraUnitaria det=(CompraUnitaria)model.getBaseBean();
			CompraUnitaria found=(CompraUnitaria)CollectionUtils.find(getCompra().getPartidas(), new Predicate(){
				public boolean evaluate(Object object) {
					if(object!=null){
						CompraUnitaria c=(CompraUnitaria)object;
						if(c.getSucursal().getId().equals(det.getSucursal().getId())){
							if(c.getProducto().getClave().equals(det.getProducto().getClave())){
								return true;
							}
						}
					}
					return false;
				}
				
			});
			if(found!=null){
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "O. De Compra");
				return;
			}
			beforInsertPartida(det);
			boolean ok=getCompra().agregarPartida(det);
			if(ok){
				afterInserPartida(det);
				partidasSource.add(det);
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "O. De Compra");
			}
		}
		form.setProductos(null);
	}
	
	public void insertarBulk(JComponent parent){
		/*List<Producto> selected=SelectorDeProductos2.find();
		if(!selected.isEmpty()){
			for(Producto p:selected){
				String res=JOptionPane.showInputDialog(parent, p.getDescripcion(),"Cantidad");
				double cantidad=NumberUtils.toDouble(res);
				if(cantidad>0){
					CompraUnitaria det=new CompraUnitaria(p);
					det.setSolicitado(cantidad);
					det.setSucursal(getCompra().getSucursal());
					beforInsertPartida(det);
					boolean ok=getCompra().agregarPartida(det);
					if(ok){
						afterInserPartida(det);
						partidasSource.add(det);
					}else{
						MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "O. De Compra");
					}
				}
				
			}
		}*/
	}
	
	/**
	 * Template para personalizar/ajustar antes de insertar la compra unitaria en bean de  compra
	 * pero despues de generarlo en la forma
	 * 
	 *   
	 * 
	 * @param det
	 */
	public void beforInsertPartida(CompraUnitaria det){
		
	}
	
	/**
	 * Template para personalizar/ajustar despues de insertar la compra unitaria en la compra
	 *  
	 *     - Asignar los precios y descuentos de la lista de precios
	 *     - Modificarlos si se tienen derechos
	 * 
	 * @param det
	 */
	public void afterInserPartida(CompraUnitaria det){
		if(getCompra().getProveedor()!=null)
			getManager().asignarPrecioDescuento(det);
		//TODO Panel de ajuste
		det.actualizar();
		validate();
	}
	
	public void elminarPartida(int index){
		
		CompraUnitaria det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getCompra().eleiminarPartida(det);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void editar(int index){
		if( (getCompra().getCierre()!=null) && isReadOnly() )
			return; //No se pueden agregar partidas a una compra cerrada o es de solo lectura
		CompraUnitaria source=partidasSource.get(index);
		System.err.println("Compra Unitaria Fuente"+source);
		CompraUnitaria target=beforeUpdate(source);
		DefaultFormModel model=new DefaultFormModel(target);
		CompraDetForm form=new CompraDetForm(model);
		if(getProductos().isEmpty())
			actualizarProductos();
		form.setProductos(getProductos());
		form.setSupervisor(isSupervisor());
		if(isSupervisor())
			form.setSucursales(LookupUtils.getDefault().getSucursales());
		form.open();
		if(!form.hasBeenCanceled()){
			afterUpdate(source, target);
			partidasSource.set(index, source);
			validate();
		}
		form.setProductos(null);
	}
	
	protected CompraUnitaria beforeUpdate(final CompraUnitaria source){
		CompraUnitaria target=new CompraUnitaria();
		System.err.println("Compra Unitaria Target"+target);
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log","ancho","largo"});
		return target;
	}
	
	protected void afterUpdate(CompraUnitaria source,CompraUnitaria target){
		//TODO Presentar la forma para ajustar descuentos ??
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		source.actualizar();
		validate();
	}
	
	public boolean isSupervisor() {
		return supervisor;
	}

	public void setSupervisor(boolean supervisor) {
		this.supervisor = supervisor;
	}

	protected ComprasManager getManager(){
		return Services.getInstance().getComprasManager();
	}
}
