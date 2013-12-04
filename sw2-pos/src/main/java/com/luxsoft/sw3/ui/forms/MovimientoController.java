package com.luxsoft.sw3.ui.forms;

import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.Movimiento.Concepto;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.selectores.LookupUtils;
import com.luxsoft.sw3.ui.services.KernellUtils;

/**
 * Controlador para la forma de Movimientos genericos
 * 
 * @author Ruben Cancino
 *
 */
public class MovimientoController extends DefaultFormModel{

	private EventList<MovimientoDet> partidasSource;
	private EventList<Producto> productos;
	
	private Logger logger=Logger.getLogger(getClass());
	
	public MovimientoController(Movimiento mov) {
		super(mov);		
	}
	
	public Movimiento getMovimiento(){
		return (Movimiento)getBaseBean();
	}
	
	protected void init(){
		partidasSource=GlazedLists.eventList(getMovimiento().getPartidas());
		if(getMovimiento().getId()==null){
			setValue("sucursal", Services.getInstance().getConfiguracion().getSucursal());
		}
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getMovimiento().getPartidas().size()==0)
			support.addError("", "Debe registrar por lo menos una partida");
		super.addValidation(support);
	}
	
	
	public void insertarPartida(){
		Concepto concepto=(Concepto)getValue("concepto");
		if(concepto==null){
			MessageUtils.showMessage("Registre primero el concepto ", "Movimiento de inventario");
			return;
		}
		MovimientoDet target=new MovimientoDet();
		target.setSucursal(getMovimiento().getSucursal());
		target.setFecha(getMovimiento().getFecha());
		target.setConcepto(getMovimiento().getConcepto().name());
		
		DefaultFormModel model=new DefaultFormModel(target);
		MovimientoDeInventarioDetForm form=new MovimientoDeInventarioDetForm(model);
		if(concepto.equals(Concepto.CIS)){
			form.setCis(true);
		}
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			target=(MovimientoDet)model.getBaseBean();
			target.setCantidad(concepto.ajustar(target.getCantidad()));
			boolean ok=getMovimiento().agregarPartida(target);
			if(ok){
				partidasSource.add(target);
				validate();
				logger.info("Partidas registradas: "+partidasSource.size());
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+target.getProducto(), "Movimiento de inventario");
			}
			
		}
	}
	
	public void eliminarPartida(int index){
		MovimientoDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getMovimiento().eliminarPartida(det);
			if(ok){
				partidasSource.remove(index);
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
	}
	
	public void edit(int index){
		MovimientoDet target=partidasSource.get(index);
		if(target!=null){			
			DefaultFormModel model=new DefaultFormModel(target);
			MovimientoDeInventarioDetForm form=new MovimientoDeInventarioDetForm(model);
			form.setProductos(getProductos());
			form.open();
			if(!form.hasBeenCanceled()){
				target=(MovimientoDet)model.getBaseBean();
				validate();
			}
		}
	}
	
	public Movimiento persist(){
		Movimiento target=getMovimiento();
		target.setFecha(new Date());
		if(target!=null){
			if(target.getId()==null){
				target=Services.getInstance().getInventariosManager().salvarMovimiento(target);
				MessageUtils.showMessage("Movimiento generado(actualizado):\n "+target.getConcepto()+ " Docto:"+target.getDocumento()
						, "Movimientos de inventario");
				logger.info("Movimiento persistido: "+target);
			}else{
				target=Services.getInstance().getInventariosManager().salvarMovimiento(target);
				
				
			}
			
			return target;
		}
		return null;
	}
	
	

	public EventList<MovimientoDet> getPartidasSource() {
		return partidasSource;
	}

	protected EventList<Producto> getProductos(){
		if(productos==null){
			productos=new BasicEventList<Producto>();
			LookupUtils.getDefault().loadProductosInventariables(productos);			
		}
		return productos;
	}
	
	

}
