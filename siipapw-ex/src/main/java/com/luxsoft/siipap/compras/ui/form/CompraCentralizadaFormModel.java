package com.luxsoft.siipap.compras.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.ComprasManager;

/**
 * Controlador y PresentationModel para la fomra de mantenimiento de Compra
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CompraCentralizadaFormModel extends DefaultFormModel {
	
	private EventList<CompraUnitaria> partidasSource;
	private EventList<Proveedor> proveedores;
	private EventList<Producto> productos;
	
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public CompraCentralizadaFormModel() {
		super(new Compra2());
		prepararCompraNueva();
	}
	
	public CompraCentralizadaFormModel(Compra2 compra) {
		super(compra);
		preparaCompraExistente();
	}
	
	protected void init(){
		if(getCompra().getPartidas().isEmpty())
			partidasSource=GlazedLists.eventList(new ArrayList<CompraUnitaria>());
		else
			partidasSource=GlazedLists.eventList(getCompra().getPartidas());
		getModel("descuentoEspecial").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				aplicarDescuento();
			}			
		});
		//Preparar de solo lectura si la compra esta cerrada
		setReadOnly(getCompra().getCierre()!=null);
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getCompra().getPartidas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		super.addValidation(support);
	}

	
	protected void prepararCompraNueva(){		
		setValue("fecha", new Date());
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
	
	private void actualizarProductos(){
		if(getCompra().getProveedor()!=null){
			String hql="select pp.producto from Proveedor p left join p.productos pp where p.id=?";
			getProductos().clear();
			getProductos().addAll(ServiceLocator2.getHibernateTemplate().find(hql,getCompra().getProveedor().getId()));			
		}
	}
	
	protected void preparaCompraExistente(){
		if(getCompra().getId()==null)
			prepararCompraNueva();
		
	}
	
	public EventList<CompraUnitaria> getPartidasSource() {
		return partidasSource;
	}

	
	
	public Compra2 getCompra(){
		return (Compra2)getBaseBean();
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
			productos=new FilterList<Producto>(productos,Matchers.beanPropertyMatcher(Producto.class, "activoCompras", Boolean.TRUE));
			//productos.addAll(ServiceLocator2.getProductoManager().buscarProductosActivos());			
		}
		return productos;
	}
	
	private EventList<Sucursal> sucursales;
	protected EventList<Sucursal> getSucursales(){
		if(sucursales==null){
			sucursales=GlazedLists.eventList(ServiceLocator2.getLookupManager().getSucursalesOperativas());
		}
		return sucursales;
	}
	
	public void insertar(){
		if(getCompra().getCierre()!=null)
			return; //No se pueden agregar partidas a una compra cerrada
		if(getCompra().getProveedor()==null){
			return;
		}
		CompraUnitaria target=new CompraUnitaria();
		target.setCompra(getCompra());
		target.setSucursal(getCompra().getSucursal());
		DefaultFormModel model=new DefaultFormModel(target);
		CompraCentralizadaDetForm form=new CompraCentralizadaDetForm(model);
		if(getProductos().isEmpty())
			actualizarProductos();
		form.setProductos(getProductos());
		form.setSucursales(getSucursales());
		form.setEspecial(getCompra().isEspecial());
		form.open();
		if(!form.hasBeenCanceled()){
			CompraUnitaria det=(CompraUnitaria)model.getBaseBean();
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
			if(!getCompra().isEspecial())
				getManager().asignarPrecioDescuento(det);
		det.actualizar();
		validate();
	}
	
	public void elminarPartida(int index){
		
		CompraUnitaria det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getCompra().eleiminarPartida(det);
			if(ok){
				partidasSource.remove(index);
				aplicarDescuento();
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void elminarPartida(CompraUnitaria det){
		if(det!=null){
			boolean ok=getCompra().eleiminarPartida(det);
			if(ok){
				partidasSource.remove(det);
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
		CompraUnitaria target=beforeUpdate(source);
		DefaultFormModel model=new DefaultFormModel(target);
		CompraCentralizadaDetForm form=new CompraCentralizadaDetForm(model);
		form.setProductos(getProductos());
		form.setSucursales(getSucursales());	
		form.setEspecial(getCompra().isEspecial());
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
		BeanUtils.copyProperties(source, target, new String[]{"id","version","log"});
		return target;
	}
	
	protected void afterUpdate(CompraUnitaria source,CompraUnitaria target){
		//TODO Presentar la forma para ajustar descuentos ??
		BeanUtils.copyProperties(target,source, new String[]{"id","version","log"});
		source.actualizar();
		validate();
	}
	
	public void aplicarDescuento(){
		if(getCompra().isEspecial()){
			double desc=getCompra().getDescuentoEspecial();
			System.out.println("Aplicando descuento especial... por: "+desc);
			for(CompraUnitaria cu:getCompra().getPartidas()){
				cu.setDesc1(desc);
				cu.setDesc2(0);
				cu.actualizar();
				int index=partidasSource.indexOf(cu);
				if(index!=-1){
					partidasSource.set(index,cu);
				}
			}
		}
		getCompra().actualizar();
	}
	
	
	protected ComprasManager getManager(){
		return ServiceLocator2.getComprasManager();
	}
	
}
