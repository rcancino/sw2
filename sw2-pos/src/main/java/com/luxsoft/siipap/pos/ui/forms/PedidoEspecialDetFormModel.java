package com.luxsoft.siipap.pos.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Date;

import org.springframework.util.Assert;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.PedidoDet;

public class PedidoEspecialDetFormModel extends DefaultFormModel implements PropertyChangeListener{

	
	//private Sucursal sucursal;
	private boolean credito=false;
	private Date fecha=new Date();
	private Header header;	
	
	
	
	public PedidoEspecialDetFormModel(PedidoDet bean) {
		super(bean);
	}
	
	protected void init(){
		Assert.isTrue(getPedidoDet().isEspecial(),"Este controlador solo funciona para partidas de pedido de medidas especiales");		
		addBeanPropertyChangeListener(this);
		//setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		setFecha(Services.getInstance().obtenerFechaDelSistema());
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getPedidoDet().getCantidad()==0){
			support.getResult().addError("La cantidad no puede ser 0");
			
		}if(getPedidoDet().getAncho()<=0){
			support.getResult().addError("El ancho debe ser> 0");
		}else if(getPedidoDet().getLargo()<=0){
			support.getResult().addError("El largo debe ser > 0");
		}
		if(getPedidoDet().getProducto()!=null)
			if(getPedidoDet().getProducto().getPaquete()>1){
				//System.out.println("Validando paquete");
				double cantidad=getPedidoDet().getCantidad();
				double paquete=(double)getPedidoDet().getProducto().getPaquete();
				double modulo=cantidad%paquete;
				//System.out.println("Modulo: "+modulo);
				if(modulo!=0){
					double faltante=paquete-modulo;
					support.getResult().addError("Este producto se vende en multiplos de: "+getPedidoDet().getProducto().getPaquete()+ " Faltante: "+faltante);
				}
			}
	}

	public void dispose(){
		removeBeanPropertyChangeListener(this);
	}
	
	protected Producto getProductio(){
		return (Producto)getValue("producto");
	}
	
	protected PedidoDet getPedidoDet(){
		return (PedidoDet)getBaseBean();
	}
	
	private void actualizarPrecio(){
		getPedidoDet().actualizar();
		//getPedidoDet().actualizarPrecioEspecial();
		/*
		if(getProductio()==null)
			return;
		CantidadMonetaria precio=CantidadMonetaria.pesos(getProductio().getPrecioPorKiloContado());
		if(isCredito())
			precio=CantidadMonetaria.pesos(getProductio().getPrecioPorKiloCredito());
			
		double gramos=(double)getProductio().getGramos();
		
		double ancho=getPedidoDet().getAncho();
		double largo=getPedidoDet().getLargo();
		
		precio=precio.multiply(ancho).multiply(largo).multiply(gramos);
		precio=precio.divide(10000d);
		setValue("precio", precio.amount());
		//getPedidoDet().actualizar();
		 * 
		 */
	}
		
	
	public boolean isCredito() {
		return credito;
	}

	public void setCredito(boolean credito) {
		this.credito = credito;
	}
	
	public void updateHeader() {
		if(header!=null){
			Producto p=getProductio();
			if(p!=null){
				header.setTitulo(MessageFormat.format("{0} ({1})",p.getDescripcion(),p.getClave()));
				String pattern="Calibre:{0} \t Caras:{1} " +
						"\nGramos:{2}\tModo de venta:{3}" +
						"\nPrecio X Kg Crédito: {4,number,currency}" +
						"\tPrecio X Kg Contado: {5,number,currency} "
						;
				String desc=MessageFormat.format(pattern
						,p.getCalibre()
						,p.getCaras()
						,p.getGramos()						
						,p.getModoDeVenta()!=null?(p.getModoDeVenta().equals("B")?"Bruto":"Neto"):""
						,p.getPrecioPorKiloCredito()
						,p.getPrecioPorKiloContado()
						);
				if(p.getPaquete()>1)
					desc+=" Paquete: "+p.getPaquete();
				header.setDescripcion(desc);
			}
			else{
				header.setTitulo("Seleccione un producto");
				header.setDescripcion("");
			}
		}
	}
	

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("producto")){
			actualizarPrecio();
			updateHeader();
		}else if("cantidad".equals(evt.getPropertyName())){
			getPedidoDet().actualizar();
		}else if("ancho".equals(evt.getPropertyName())){
			actualizarPrecio();
		}else if("largo".equals(evt.getPropertyName())){
			actualizarPrecio();
		}else if("cortes".equals(evt.getPropertyName())){
			getPedidoDet().actualizar();
		}else if("precioCorte".equals(evt.getPropertyName())){
			getPedidoDet().actualizar();
		}
	}
	
	/*
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}*/

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Header getHeader() {
		if(header==null){
			header=new Header("Seleccione un producto","");
			header.setDescRows(5);
			updateHeader();
		}
		return header;
	}	

	

}
