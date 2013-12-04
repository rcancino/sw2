package com.luxsoft.siipap.inventarios.model;

import java.util.Date;

import com.luxsoft.siipap.model.User;



/**
 * Bean para encapsular estado y comportamiento relacionado con una transformacion
 * de producto
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class TransformacionModel {
	
	private Existencia origen;
	private String origenProd;
	private double salida;
	private double existenciaOrigen;
	
	private Existencia destino;
	private String destinoProd;
	private double entrada;
	private double existenciaDestino;
	
	private String comentario;
	private User autorizo;
	private String password;
	
	public Existencia getOrigen() {
		return origen;
	}
	public void setOrigen(Existencia origen) {
		this.origen = origen;
		if(origen!=null){
			setOrigenProd(origen.getDescripcion()+" ("+origen.getClave()+")");
			setExistenciaOrigen(origen.getCantidad());
		}else{
			setOrigenProd(null);
			setExistenciaOrigen(0);
		}
	}
	public double getSalida() {
		return salida;
	}
	public void setSalida(double salida) {
		this.salida = salida;
	}
	public Existencia getDestino() {
		return destino;
	}
	public void setDestino(Existencia destino) {
		this.destino = destino;
		if(destino!=null){
			setDestinoProd(destino.getDescripcion()+" ("+destino.getClave()+")");
			setExistenciaDestino(destino.getCantidad());
		}else{
			setDestinoProd(null);
			setExistenciaDestino(0);
		}
	}
	public double getEntrada() {
		return entrada;
	}
	public void setEntrada(double entrada) {
		this.entrada = entrada;
	}
	public String getOrigenProd() {
		return origenProd;
	}
	public void setOrigenProd(String origenProd) {
		this.origenProd = origenProd;
	}
	public String getDestinoProd() {
		return destinoProd;
	}
	public void setDestinoProd(String destinoProd) {
		this.destinoProd = destinoProd;
	}
	public double getExistenciaOrigen() {
		return existenciaOrigen;
	}
	public void setExistenciaOrigen(double existenciaOrigen) {
		this.existenciaOrigen = existenciaOrigen;
	}
	public double getExistenciaDestino() {
		return existenciaDestino;
	}
	public void setExistenciaDestino(double existenciaDestino) {
		this.existenciaDestino = existenciaDestino;
	}
	
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	
	
	
	public User getAutorizo() {
		return autorizo;
	}
	public void setAutorizo(User autorizo) {
		this.autorizo = autorizo;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public TransformacionDet toTransformaciones(Transformacion t){
		TransformacionDet salida=new TransformacionDet();
		TransformacionDet entrada=new TransformacionDet();
		
		String user=getAutorizo()!=null?getAutorizo().getFullName():null;
		
		salida.setProducto(getOrigen().getProducto());
		salida.setCantidad(Math.abs(getSalida())*-1);
		salida.setCosto(getOrigen().getCostoPromedio());
		salida.setCostoOrigen(getOrigen().getCostoPromedio());
		salida.setCostoPromedio(getOrigen().getCostoPromedio());
		salida.setExistencia(getExistenciaOrigen());
		salida.setSucursal(t.getSucursal());
		salida.setFecha(new Date());
		salida.setComentario(getComentario());
		salida.setCreateUser(user);
		salida.setCreado(new Date());
		
		entrada.setProducto(getDestino().getProducto());
		entrada.setCantidad(Math.abs(getEntrada()));
		entrada.setCosto(getDestino().getCostoPromedio());
		entrada.setCostoOrigen(getDestino().getCostoPromedio());
		entrada.setCostoPromedio(getDestino().getCostoPromedio());
		entrada.setExistencia(getExistenciaDestino());
		entrada.setSucursal(t.getSucursal());
		//System.out.println("Asignando fecha del maestro :"+t.getFecha());
		entrada.setFecha(new Date());
		entrada.setComentario(getComentario());
		entrada.setCreateUser(user);
		entrada.setCreado(new Date());
		salida.setDestino(entrada);
		
		//entrada.setOrigen(salida);
		entrada.actualizarCosto();
		entrada.actualizarCostoOrigen();
		salida.actualizarKilosDelMovimiento();
		entrada.actualizarKilosDelMovimiento();
		return salida;
	}

}
