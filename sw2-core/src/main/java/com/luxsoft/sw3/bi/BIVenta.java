package com.luxsoft.sw3.bi;

import java.math.BigDecimal;
import java.util.Date;

import com.luxsoft.siipap.model.Periodo;

public class BIVenta {
	
	private String CARGO_ID;
	private long sucursal_id;
	private long docto;
	private Date FECHA;
	private long CLIENTE_ID;
	private String ORIGEN;
	private BigDecimal IMPORTE_CORTES;
	private BigDecimal IMPORTE_BRUTO;
	private double DESCUENTOS;
	private BigDecimal cargos;
	private BigDecimal FLETE;
	private double IMPORTE_CALCULADO;
	private BigDecimal importe;
	private BigDecimal impuesto;
	private BigDecimal total;
	private BigDecimal DEVOLUCION2;
	private BigDecimal BONIFICACION;
	private double COSTO;
	private BigDecimal SALDO;
	private Date VTO;
	private String SOCIO_ID;
	private long COBRADOR_ID;
	private String FPAGO;
	private double KILOS;
	private String MODIFICADO_USERID;
	private String CANCELADO;
	private String sucursalNombre;
	private String nombre;
	public String getCARGO_ID() {
		return CARGO_ID;
	}
	public void setCARGO_ID(String cargo_id) {
		CARGO_ID = cargo_id;
	}
	public long getSucursal_id() {
		return sucursal_id;
	}
	public void setSucursal_id(long sucursal_id) {
		this.sucursal_id = sucursal_id;
	}
	public long getDocto() {
		return docto;
	}
	public void setDocto(long docto) {
		this.docto = docto;
	}
	public Date getFECHA() {
		return FECHA;
	}
	public void setFECHA(Date fecha) {
		FECHA = fecha;
	}
	public long getCLIENTE_ID() {
		return CLIENTE_ID;
	}
	public void setCLIENTE_ID(long cliente_id) {
		CLIENTE_ID = cliente_id;
	}
	public String getORIGEN() {
		return ORIGEN;
	}
	public void setORIGEN(String origen) {
		ORIGEN = origen;
	}
	public BigDecimal getIMPORTE_CORTES() {
		return IMPORTE_CORTES;
	}
	public void setIMPORTE_CORTES(BigDecimal importe_cortes) {
		IMPORTE_CORTES = importe_cortes;
	}
	public BigDecimal getIMPORTE_BRUTO() {
		return IMPORTE_BRUTO;
	}
	public void setIMPORTE_BRUTO(BigDecimal importe_bruto) {
		IMPORTE_BRUTO = importe_bruto;
	}
	public double getDESCUENTOS() {
		return DESCUENTOS;
	}
	public void setDESCUENTOS(double descuentos) {
		DESCUENTOS = descuentos;
	}
	public BigDecimal getCargos() {
		return cargos;
	}
	public void setCargos(BigDecimal cargos) {
		this.cargos = cargos;
	}
	public BigDecimal getFLETE() {
		return FLETE;
	}
	public void setFLETE(BigDecimal flete) {
		FLETE = flete;
	}
	public double getIMPORTE_CALCULADO() {
		return IMPORTE_CALCULADO;
	}
	public void setIMPORTE_CALCULADO(double importe_calculado) {
		IMPORTE_CALCULADO = importe_calculado;
	}
	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}
	public BigDecimal getImpuesto() {
		return impuesto;
	}
	public void setImpuesto(BigDecimal impuesto) {
		this.impuesto = impuesto;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getDEVOLUCION2() {
		return DEVOLUCION2;
	}
	public void setDEVOLUCION2(BigDecimal devolucion2) {
		DEVOLUCION2 = devolucion2;
	}
	public BigDecimal getBONIFICACION() {
		return BONIFICACION;
	}
	public void setBONIFICACION(BigDecimal bonificacion) {
		BONIFICACION = bonificacion;
	}
	public double getCOSTO() {
		return COSTO;
	}
	public void setCOSTO(double costo) {
		COSTO = costo;
	}
	public BigDecimal getSALDO() {
		return SALDO;
	}
	public void setSALDO(BigDecimal saldo) {
		SALDO = saldo;
	}
	public Date getVTO() {
		return VTO;
	}
	public void setVTO(Date vto) {
		VTO = vto;
	}
	public String getSOCIO_ID() {
		return SOCIO_ID;
	}
	public void setSOCIO_ID(String socio_id) {
		SOCIO_ID = socio_id;
	}
	public long getCOBRADOR_ID() {
		return COBRADOR_ID;
	}
	public void setCOBRADOR_ID(long cobrador_id) {
		COBRADOR_ID = cobrador_id;
	}
	public String getFPAGO() {
		return FPAGO;
	}
	public void setFPAGO(String fpago) {
		FPAGO = fpago;
	}
	public double getKILOS() {
		return KILOS;
	}
	public void setKILOS(double kilos) {
		KILOS = kilos;
	}
	public String getMODIFICADO_USERID() {
		return MODIFICADO_USERID;
	}
	public void setMODIFICADO_USERID(String modificado_userid) {
		MODIFICADO_USERID = modificado_userid;
	}
	public String getCANCELADO() {
		return CANCELADO;
	}
	public void setCANCELADO(String cancelado) {
		CANCELADO = cancelado;
	}
	public String getSucursalNombre() {
		return sucursalNombre;
	}
	public void setSucursalNombre(String sucursalNombre) {
		this.sucursalNombre = sucursalNombre;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((CARGO_ID == null) ? 0 : CARGO_ID.hashCode());
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
		BIVenta other = (BIVenta) obj;
		if (CARGO_ID == null) {
			if (other.CARGO_ID != null)
				return false;
		} else if (!CARGO_ID.equals(other.CARGO_ID))
			return false;
		return true;
	}
	
	public BigDecimal getImporteNeto(){
		return getImporte().subtract(getDEVOLUCION2()).subtract(getBONIFICACION());
	}
	
	public BigDecimal getUtilidad(){
		return getImporteNeto().subtract(BigDecimal.valueOf(getCOSTO()));
	}
	
	private BigDecimal totalSegmento=BigDecimal.ZERO;
	
	public BigDecimal getTotalSegmento() {
		return totalSegmento;
	}
	public void setTotalSegmento(BigDecimal totalSegmento) {
		this.totalSegmento = totalSegmento;
	}
	
	public double getParticipacion(){
		if(getImporteNeto().doubleValue()>0)
			return getTotalSegmento().multiply(BigDecimal.valueOf(100))
				.divide(getImporteNeto()).doubleValue();
		else
			return 0;
	}
	
	public int getYear(){
		return Periodo.obtenerYear(getFECHA());
	}
	public int getMes(){
		return Periodo.obtenerMes(getFECHA())+1;
	}
	
}
