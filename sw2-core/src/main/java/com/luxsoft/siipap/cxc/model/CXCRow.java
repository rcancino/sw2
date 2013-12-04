package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Bean para el analisis de movimientos de cuenta por proveedor
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CXCRow {
	
	protected Long id=0L;
	protected Long cargoId=0l;
	protected Long abonoId=0l;
	protected String clave="";
	protected String nombre="";
	private String sucursal;
	protected String documento="";
	protected String tipo="";
	protected String tipoDesc="";
	
	protected Date fecha=new Date();
	protected Date creado=new Date();
	protected Date vencimiento;
	private int atraso;
	protected String formaDePago="";
	protected String pagoRef="";
	
	protected BigDecimal cargo=BigDecimal.ZERO;
	protected BigDecimal abono=BigDecimal.ZERO;
	
	protected BigDecimal importeAnalisis=BigDecimal.ZERO;
	
	private BigDecimal saldoCargo=BigDecimal.ZERO;
	private BigDecimal disponible=BigDecimal.ZERO;
	
	
	private BigDecimal saldoAnalisis=BigDecimal.ZERO;
	private BigDecimal saldoAcumulado=BigDecimal.ZERO;
	
	private Currency moneda=MonedasUtils.PESOS;
	private double tc=0;
	
	private String origen;
	protected Date fechaDocto;
	
	
	
	public CXCRow(final Cargo ca,final Date corte){
		this.tipo="CAR";
		this.tipoDesc=ca.getTipoDocto();
		this.moneda=ca.getMoneda();
		this.tc=ca.getTc();
		//this.id=ca.getId();
		this.cargoId=ca.getDocumento();
		this.clave=ca.getClave();
		this.documento=String.valueOf(ca.getDocumento());
		this.nombre=ca.getNombre();
		this.fecha=ca.getFecha();
		if(ca.getLog()!=null)
			this.creado=ca.getLog().getCreado();
		this.cargo=ca.getTotal();
		//this.importeAnalisis=ca.getTotalAnalisis().amount();
		this.vencimiento=ca.getVencimiento();
		this.atraso=ca.getAtraso();
		this.saldoCargo=CXCUtils.getSaldoAlCorte(ca, corte).amount();
		//this.saldoAnalisis=CXPUtils.getSaldoAnalizadoAlCorte(ca, corte).amount();*/
		this.sucursal=ca.getSucursal().getNombre();
		this.origen=ca.getOrigen().name();
		this.fechaDocto=ca.getFecha();
	}
	
	public CXCRow(final Abono ca,final Date corte){
		this.tipo="ABN";
		this.tipoDesc=ca.getInfo();
		this.moneda=ca.getMoneda();
		this.tc=ca.getTc();
		//this.id=ca.getId();
		this.abonoId=(long)ca.getFolio();
		this.clave=ca.getClave();
		this.documento=String.valueOf(ca.getFolio());
		this.nombre=ca.getCliente().getNombreRazon();
		this.fecha=ca.getFecha();
		if(ca.getLog()!=null)
			this.creado=ca.getLog().getCreado();
		this.abono=ca.getTotal();
		this.disponible=CXCUtils.getDisponibleAlCorte(ca, corte).amount();
		this.sucursal=ca.getSucursal().getNombre();
		this.pagoRef=String.valueOf(ca.getFolio());
		this.origen="";
		this.fechaDocto=ca.getFecha();
	}
	
	public CXCRow(final Aplicacion ca){
		this.tipo="APL";
		this.tipoDesc=ca.getAbono().getInfo();
		this.cargoId=ca.getCargo().getDocumento();
		this.abonoId=(long)ca.getAbono().getFolio();
		this.clave=ca.getCargo().getClave();
		this.documento=String.valueOf(ca.getCargo().getDocumento());
		this.nombre=ca.getCargo().getCliente().getNombreRazon();
		this.fecha=ca.getFecha();
		if(ca.getLog()!=null)
			this.creado=ca.getLog().getCreado();
		this.cargo=ca.getImporte().multiply(BigDecimal.valueOf(-1d));
		this.abono=ca.getImporte().multiply(BigDecimal.valueOf(-1d));
		this.pagoRef=ca.getAbono().getInfo();
		this.moneda=ca.getAbono().getMoneda();
		this.tc=ca.getAbono().getTc();
		this.sucursal=ca.getCargo().getSucursal().getNombre();
		this.origen=ca.getCargo().getOrigen().name();
		this.fechaDocto=ca.getCargo().getFecha();
		
	}
	
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long cxpId) {
		this.id = cxpId;
	}
	
	
	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public Long getAbonoId() {
		return abonoId;
	}

	public void setAbonoId(Long abonoId) {
		this.abonoId = abonoId;
	}

	public Long getCargoId() {
		return cargoId;
	}
	public void setCargoId(Long cargoId) {
		this.cargoId = cargoId;
	}
	
	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public Date getVencimiento() {
		return vencimiento;
	}
	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}
	public String getFormaDePago() {
		return formaDePago;
	}
	public void setFormaDePago(String formaDePago) {
		this.formaDePago = formaDePago;
	}
	public String getPagoRef() {
		return pagoRef;
	}
	public void setPagoRef(String pagoRef) {
		this.pagoRef = pagoRef;
	}
	
	
	
	public String getTipoDesc() {
		return tipoDesc;
	}

	public void setTipoDesc(String tipoDesc) {
		this.tipoDesc = tipoDesc;
	}

	public BigDecimal getCargo() {
		return cargo;
	}

	public void setCargo(BigDecimal cargo) {
		this.cargo = cargo;
	}

	public BigDecimal getAbono() {
		return abono;
	}

	public void setAbono(BigDecimal abono) {
		this.abono = abono;
	}

	public BigDecimal getSaldoCargo() {
		return saldoCargo;
	}
	public void setSaldoCargo(BigDecimal saldoCargo) {
		this.saldoCargo = saldoCargo;
	}
	public BigDecimal getSaldoAcumulado() {
		return saldoAcumulado;
	}
	public void setSaldoAcumulado(BigDecimal saldoAcumulado) {
		this.saldoAcumulado = saldoAcumulado;
	}
	
	
	public BigDecimal getSaldoAnalisis() {
		return saldoAnalisis;
	}
	public void setSaldoAnalisis(BigDecimal saldoAnalisis) {
		this.saldoAnalisis = saldoAnalisis;
	}
	public BigDecimal getImporteAnalisis() {
		return importeAnalisis;
	}
	public void setImporteAnalisis(BigDecimal importeAnalisis) {
		this.importeAnalisis = importeAnalisis;
	}
	public int getAtraso() {
		return atraso;
	}
	public void setAtraso(int atraso) {
		this.atraso = atraso;
	}
	
	
	
	public BigDecimal getDisponible() {
		return disponible;
	}

	public void setDisponible(BigDecimal disponible) {
		this.disponible = disponible;
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SIMPLE_STYLE);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clave == null) ? 0 : clave.hashCode());
		result = prime * result
				+ ((documento == null) ? 0 : documento.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
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
		CXCRow other = (CXCRow) obj;
		if (clave == null) {
			if (other.clave != null)
				return false;
		} else if (!clave.equals(other.clave))
			return false;
		if (documento == null) {
			if (other.documento != null)
				return false;
		} else if (!documento.equals(other.documento))
			return false;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (tipo == null) {
			if (other.tipo != null)
				return false;
		} else if (!tipo.equals(other.tipo))
			return false;
		return true;
	}

	public Date getCreado() {
		return creado;
	}

	public void setCreado(Date creado) {
		this.creado = creado;
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		this.tc = tc;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public Date getFechaDocto() {
		return fechaDocto;
	}

	public void setFechaDocto(Date fechaDocto) {
		this.fechaDocto = fechaDocto;
	}

	
	
}
