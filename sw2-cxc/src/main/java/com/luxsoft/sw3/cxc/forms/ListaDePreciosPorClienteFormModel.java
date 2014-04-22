package com.luxsoft.sw3.cxc.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.ui.form.ProductoFinderCxc;
import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;





/**
 * Controlador y FormModel para la forma de lista de precios por cliente
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ListaDePreciosPorClienteFormModel extends DefaultFormModel {
	
	private EventList<ListaDePreciosClienteDet> partidasSource;
	
	
	
	protected Logger logger=Logger.getLogger(getClass());
	
	private EventSelectionModel<ListaDePreciosClienteDet> selectionModel;

	public ListaDePreciosPorClienteFormModel() {
		super(new ListaDePreciosCliente());
	}
	
	public ListaDePreciosPorClienteFormModel(ListaDePreciosCliente lista) {
		super(lista);
	}
	
	protected void init(){
		getModel("cliente").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				logger.info("Cliente seleccionado: "+getLista().getCliente());
				if(getLista().getId()==null){
					getPartidasSource().clear();
					getLista().getPrecios().clear();
					validate();
				}else{
					throw new IllegalArgumentException("El cliente no es modificable");
				}
			}
		});
		getModel("descuento").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				aplicarDescuento();
			}
		});
		partidasSource=GlazedLists.eventList(getLista().getPrecios());
		partidasSource=new SortedList<ListaDePreciosClienteDet>(partidasSource,null);
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getLista().getCliente()==null){
			support.getResult().addError("El cliente es mandatorio");
		}
		if(getLista().getPrecios().isEmpty()){
			support.getResult().addError("Debe registrar por lo menos un precio");
		}		
		super.addValidation(support);
	}
	
	public EventList<ListaDePreciosClienteDet> getPartidasSource() {
		return partidasSource;
	}	
	
	public ListaDePreciosCliente getLista(){
		return (ListaDePreciosCliente)getBaseBean();
	}
	
	private void aplicarDescuento(){
		for(ListaDePreciosClienteDet det:selectionModel.getSelected()){
			det.setDescuento(getLista().getDescuento());
			det.aplicarDescuentoSobrePrecioDeLista();
			partidasSource.set(partidasSource.indexOf(det), det);
		}
		/*
		for(int index=0;index<partidasSource.size();index++){
			ListaDePreciosClienteDet det=partidasSource.get(index);
			det.setDescuento(getLista().getDescuento());
			det.aplicarDescuentoSobrePrecioDeLista();
			partidasSource.set(index, det);
		}*/
	}
	
	public void insertar(){
		if(getLista().getCliente()==null){
			return;
		}
		List<Producto> list=ProductoFinderCxc.findWithDialog(getLista().getCliente());
		for(Producto p:list){
			ListaDePreciosClienteDet det=new ListaDePreciosClienteDet();
			det.setProducto(p);
			det.setPrecioDeLista(p.getPrecioCredito());
			det.setPrecio(p.getPrecioCredito());
			if(!this.partidasSource.contains(det)){
				boolean ok=getLista().agregarPrecio(det);
				if(ok){
					calcularCostos(det);
					afterInserPartida(det);
					this.partidasSource.add(det);
				}
			}
		}
		if(list.size()>0)
			aplicarDescuento();
	}
	
	public void afterInserPartida(ListaDePreciosClienteDet det){
		validate();
	}
	
	private void calcularCostos(ListaDePreciosClienteDet det) {
		Date d=new Date();
		int year=Periodo.obtenerYear(d);
		int mes=Periodo.obtenerMes(d);
		CostoPromedio cp=ServiceLocator2
			.getCostoPromedioManager()
			.buscarCostoPromedio(year, mes, det.getProducto().getClave());
		if(cp!=null){
			logger.info("Aplicando costo producto: "+det.getClave()+ " Periodo:"+mes+"/"+year);
			//System.out.println("Aplicando costo producto: "+det.getClave()+ " Periodo:"+mes+"/"+year+ "Costo: "+cp.getCostop());
			det.setCostoPromedio(cp.getCostop());
			det.setCostoUltimo(cp.getCostoUltimo());
			det.setCosto(cp.getCostoUltimo());
		}else{
			logger.info("No existe registro de costo promedio para el producto: "+det.getClave()+ " Periodo:"+mes+"/"+year);
			//System.out.println("No existe registro de costo promedio para el producto: "+det.getClave()+ " Periodo:"+mes+"/"+year);
		}
	}

	public void elminarPartida(int index){
		ListaDePreciosClienteDet pago=partidasSource.get(index);
		if(pago!=null){
			boolean ok=getLista().getPrecios().remove(pago);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	/*
	public void editar(int index){
		ListaDePreciosClienteDet source=partidasSource.get(index);
		ListaDePreciosClienteDet target=new ListaDePreciosClienteDet();
		BeanUtils.copyProperties(source, target,new String[]{"id","version",""});
		final DefaultFormModel model=new DefaultFormModel(target);
		final ListaDePreciosPorClienteDetForm form=new ListaDePreciosPorClienteDetForm(model);
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			source.setPrecio(target.getPrecio());
			source.setPrecioKilo(target.getPrecioKilo());
			source.setDescuento(target.getDescuento());
			source.aplicarDescuentoSobrePrecioDeLista();
			partidasSource.set(index, source);
		}
	}
	*/
	protected ListaDePreciosClienteDet beforeUpdate(final ListaDePreciosClienteDet source){
		return source;
	}

	public ListaDePreciosCliente commit() {
		ListaDePreciosCliente corte=getLista();
		return corte;
	}
	
	private List<Producto> productos;
	
	public List<Producto> getProductos(){
		if(productos==null){
			productos=ServiceLocator2.getProductoManager().buscarProductosActivos();
		}
		return productos;
	}
	
	

	public EventSelectionModel<ListaDePreciosClienteDet> getSelectionModel() {
		if(selectionModel==null){
			selectionModel=new EventSelectionModel<ListaDePreciosClienteDet>(getPartidasSource());
		}
		return selectionModel;
	}

	public void aplicarPrecioPorKilo(final EventSelectionModel<ListaDePreciosClienteDet> smodel) {
		boolean seleccion=true;
		if(MessageUtils.showConfirmationMessage("A toda la lista", "Descuento por Kilogramo")){
			seleccion=false;
		}
		String sval=JOptionPane.showInputDialog("Precio por Kg?");
		double precio=NumberUtils.toDouble(sval);
		if(NumberUtils.isNumber(sval)){
			if(seleccion){
				int min=smodel.getMinSelectionIndex();
				int max=smodel.getMaxSelectionIndex();
				for(int index=min;index<=max;index++){
					ListaDePreciosClienteDet det=smodel.getSelected().get(index);
					det.setPrecioKilo(precio);
					det.setDescuento(0d);
					det.aplicarDescuentoSobrePrecioDeLista();
					partidasSource.set(index, det);
				}
			}else{
				for(int index=0;index<partidasSource.size();index++){
					ListaDePreciosClienteDet det=smodel.getSelected().get(index);
					det.setPrecioKilo(precio);
					det.setDescuento(0d);
					det.aplicarDescuentoSobrePrecioDeLista();
					partidasSource.set(index, det);
				}
			}
		}else{
			MessageUtils.showMessage("Valor invalido: "+sval, "Precio por Kg");
			return;
		}
	}
}
