package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;
import java.util.Date;

import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Una version limitada del cargo de un cliente
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CargoRow implements Serializable{
	
	private String tipo;
	private Date fecha;
	private Long documento;
	private int sucursal;
	private String sucursalName;
	private int plazo;
	private Date vencimiento;
	private String clave;
	private String nombreRazon;	
	private String tipoVencimiento;
	
	private CantidadMonetaria total=CantidadMonetaria.pesos(0);
	private CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
	
	/** Datos generales del cliente **/
	private CantidadMonetaria limite=CantidadMonetaria.pesos(0);
	private int plazoCliente;

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getNombreRazon() {
		return nombreRazon;
	}

	public void setNombreRazon(String nombreRazon) {
		this.nombreRazon = nombreRazon;
	}

	public int getSucursal() {
		return sucursal;
	}

	public void setSucursal(int sucursal) {
		this.sucursal = sucursal;
	}

	public Date getVencimiento() {
		//return DateUtils.addDays(getFecha(), getPlazo());
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}

	public String getTipoVencimiento() {
		return tipoVencimiento;
	}

	public void setTipoVencimiento(String tipoVencimiento) {
		this.tipoVencimiento = tipoVencimiento;
	}

	public CantidadMonetaria getLimite() {
		return limite;
	}

	public void setLimite(CantidadMonetaria limite) {
		this.limite = limite;
	}
	

	public CantidadMonetaria getTotal() {
		return total;
	}

	public void setTotal(CantidadMonetaria total) {
		this.total = total;
	}

	public CantidadMonetaria getSaldo() {
		return saldo;
	}

	public void setSaldo(CantidadMonetaria saldo) {
		this.saldo = saldo;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}
	
	

	public String getSucursalName() {
		return sucursalName;
	}

	public void setSucursalName(String sucursalName) {
		this.sucursalName = sucursalName;
	}

	public int getPlazo() {
		return plazo;
	}

	public void setPlazo(int plazo) {
		this.plazo = plazo;
	}

	public int getPlazoCliente() {
		return plazoCliente;
	}

	public void setPlazoCliente(int plazoCliente) {
		this.plazoCliente = plazoCliente;
	}
	
	public int getAtraso(){
		Date today=new Date();		
		long res=today.getTime()-getVencimiento().getTime();
		if(res>0){				
			long dias=(res/(86400*1000));			
			return (int)dias;
		}else{
			return 0;
		}
	}
	
	public int getAtrasoReal(){
		Date today=new Date();		
		long res=today.getTime()-getFecha().getTime();
		if(res>0){			
			long dias=(res/(86400*1000));			
			return ((int)dias-getPlazo());
		}else{
			return 0;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((documento == null) ? 0 : documento.hashCode());
		result = prime * result + sucursal;
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CargoRow other = (CargoRow) obj;
		if (documento == null) {
			if (other.documento != null)
				return false;
		} else if (!documento.equals(other.documento))
			return false;
		if (sucursal != other.sucursal)
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}
	
	

}
