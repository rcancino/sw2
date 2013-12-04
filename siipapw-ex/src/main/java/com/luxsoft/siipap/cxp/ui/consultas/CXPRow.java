package com.luxsoft.siipap.cxp.ui.consultas;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.cxp.model.CXPUtils;
import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * Bean para el analisis de movimientos de cuenta por proveedor
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CXPRow {
	
	protected Long id;
	protected Long cargoId=0l;
	protected Long abonoId=0l;
	protected String clave;
	protected String nombre;
	
	protected String documento;
	protected String tipo;
	protected String tipoDesc;
	
	protected Date fecha;
	protected Date creado;
	protected Date vencimiento;
	private int atraso;
	protected String formaDePago;
	protected String pagoRef;
	
	protected BigDecimal cargo=BigDecimal.ZERO;
	protected BigDecimal abono=BigDecimal.ZERO;
	
	protected BigDecimal importeAnalisis=BigDecimal.ZERO;
	
	private BigDecimal saldoCargo=BigDecimal.ZERO;
	private BigDecimal disponible=BigDecimal.ZERO;
	
	
	private BigDecimal saldoAnalisis=BigDecimal.ZERO;
	private BigDecimal saldoAcumulado=BigDecimal.ZERO;
	private BigDecimal diferencia=BigDecimal.ZERO;
	
	private Currency moneda;
	private double tc;
	
	
	public CXPRow(final CXPFactura ca,final Date corte){
		this.tipo="CAR";
		this.tipoDesc="FAC";
		this.moneda=ca.getMoneda();
		this.tc=ca.getTc();
		this.id=ca.getId();
		this.cargoId=ca.getId();
		this.clave=ca.getClave();
		this.documento=ca.getDocumento();
		this.nombre=ca.getProveedor().getNombreRazon();
		this.fecha=ca.getFecha();
		this.creado=ca.getCreado();
		this.cargo=ca.getTotalMN().amount();
		this.importeAnalisis=ca.getTotalAnalisisMN().amount();
		this.vencimiento=ca.getVencimiento();
		this.atraso=ca.getAtraso();
		this.saldoCargo=CXPUtils.getSaldoAlCorteMN(ca, corte).amount();
		this.saldoAnalisis=CXPUtils.getSaldoAnalizadoAlCorteMN(ca, corte).amount();
		diferencia=ca.getDiferencia(corte);
	}
	
	public CXPRow(final CXPNota ca,final Date corte){
		this.tipo="ABN";
		this.tipoDesc=ca.getInfo();
		this.moneda=ca.getMoneda();
		this.tc=ca.getTc();
		this.id=ca.getId();
		this.abonoId=ca.getId();
		this.clave=ca.getClave();
		this.documento=ca.getDocumento();
		this.nombre=ca.getProveedor().getNombreRazon();
		this.fecha=ca.getFecha();
		this.creado=ca.getCreado();
		this.abono=ca.getTotalMN().amount();
		this.disponible=CXPUtils.getDisponibleAlCorte(ca, corte).amount();
		this.diferencia=ca.getDiferencia(corte);
	}
	
	public CXPRow(final CXPPago ca,final Date corte){
		this.tipo="ABN";
		this.tipoDesc="PAGO";
		this.moneda=ca.getMoneda();
		this.tc=ca.getTc();
		this.id=ca.getId();
		this.abonoId=ca.getId();
		this.clave=ca.getClave();
		this.documento=ca.getDocumento();
		this.nombre=ca.getProveedor().getNombreRazon();
		this.fecha=ca.getFecha();
		this.creado=ca.getCreado();
		this.abono=ca.getTotalMN().amount();
		this.disponible=CXPUtils.getDisponibleAlCorte(ca, corte).amount();
		this.diferencia=ca.getDiferencia(corte);
	}
	
	public CXPRow(final CXPAplicacion ca){
		this.tipo="APL";
		this.tipoDesc=ca.getAbono().getTipoId();
		this.id=ca.getId();
		this.cargoId=ca.getCargo().getId();
		this.abonoId=ca.getAbono().getId();
		this.clave=ca.getCargo().getClave();
		this.documento=ca.getCargo().getDocumento();
		this.nombre=ca.getCargo().getProveedor().getNombreRazon();
		this.fecha=ca.getFecha();
		this.creado=ca.getCreado();
		this.cargo=ca.getImporteMN().multiply(BigDecimal.valueOf(-1d));
		this.abono=ca.getImporteMN().multiply(BigDecimal.valueOf(-1d));
		this.pagoRef=ca.getAbono().getInfo();
		this.moneda=ca.getAbono().getMoneda();
		this.tc=ca.getAbono().getTc();
		//this.importeAnalisis=ca.getTotalAnalisis().amount();
		//this.vencimiento=ca.getVencimiento();
		//this.atraso=ca.getAtraso();
		//this.saldoCargo=CXPUtils.getSaldoAlCorte(ca, corte).amount();
		//this.saldoAnalisis=CXPUtils.getSaldoAnalizadoAlCorte(ca, corte).amount();
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
		if(getSaldoCargo().doubleValue()<=0)
			return 0;
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
		CXPRow other = (CXPRow) obj;
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

	


	public CantidadMonetaria getImporteOrigen() {
		
		CantidadMonetaria  i=CantidadMonetaria.pesos(getCargo().add(getAbono()));
		if(getTipo().equals("APL"))
			i=CantidadMonetaria.pesos(getCargo());
		return i.divide(getTc());
	}

	public BigDecimal getDiferencia() {
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
	}

	
	
	
	
}
