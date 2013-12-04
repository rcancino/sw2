package com.luxsoft.siipap.model.cxp;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;

/**
 * Cuenta por pagar
 * 
 * @author Ruben Cancino
 * 
 */
@Entity
@Table(name = "SW_CXP2")
public class CxP2 {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "CXP_ID")
	private Long id;

	@Version
	private int version;

	@Column(name = "DOCUMENTO", length = 15, nullable = false)
	@NotNull
	private String documento;

	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha;

	@Column(name = "VTO", nullable = false)
	@Type(type = "date")
	private Date vencimiento;

	@Column(name = "PROVEEDOR", nullable = false)
	private String proveedor;

	@Column(name = "RFC", nullable = true, length = 15)
	private String rfc;

	@Enumerated(EnumType.STRING)
	private Origen origen;

	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { @Column(name = "IMPORTE", scale = 2),
			@Column(name = "IMPORTE_MON", length = 3)

	})
	@AccessType(value = "field")
	private CantidadMonetaria importe;

	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { @Column(name = "IMPUESTO", scale = 2),
			@Column(name = "IMPUESTO_MON", length = 3)

	})
	@AccessType(value = "field")
	private CantidadMonetaria impuesto;

	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { @Column(name = "TOTAL", scale = 2),
			@Column(name = "TOTAL_MON", length = 3)

	})
	@AccessType(value = "field")
	private CantidadMonetaria total;

	@Column(name = "MONEDA", nullable = false)
	private Currency moneda;

	@Column(name = "TC", nullable = false)
	private BigDecimal tc;

	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@AccessType(value = "field")
	@Columns(columns = { 
			@Column(name = "SALDO", scale = 2),
			@Column(name = "SALDO_MON", length = 3)
	})	
	private CantidadMonetaria saldo;

	@Embedded
	private UserLog userLog = new UserLog();

	public static enum Origen {
		COMPRA, GASTO
	}

	public CxP2() {

	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CantidadMonetaria getImporte() {
		return importe;
	}

	public void setImporte(CantidadMonetaria importe) {
		this.importe = importe;
	}

	public CantidadMonetaria getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(CantidadMonetaria impuesto) {
		this.impuesto = impuesto;
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	public Origen getOrigen() {
		return origen;
	}

	public void setOrigen(Origen origen) {
		this.origen = origen;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public CantidadMonetaria getSaldo() {
		return saldo;
	}

	public void setSaldo(CantidadMonetaria saldo) {
		this.saldo = saldo;
	}

	public BigDecimal getTc() {
		return tc;
	}

	public void setTc(BigDecimal tc) {
		this.tc = tc;
	}

	public CantidadMonetaria getTotal() {
		return total;
	}

	public void setTotal(CantidadMonetaria total) {
		this.total = total;
	}

	public UserLog getUserLog() {
		return userLog;
	}

	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}

	public Date getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((documento == null) ? 0 : documento.hashCode());
		result = PRIME * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
		result = PRIME * result + ((moneda == null) ? 0 : moneda.hashCode());
		result = PRIME * result + ((origen == null) ? 0 : origen.hashCode());
		result = PRIME * result + ((proveedor == null) ? 0 : proveedor.hashCode());
		result = PRIME * result + ((rfc == null) ? 0 : rfc.hashCode());
		result = PRIME * result + ((tc == null) ? 0 : tc.hashCode());
		result = PRIME * result + ((total == null) ? 0 : total.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CxP2 other = (CxP2) obj;
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (moneda == null) {
			if (other.moneda != null)
				return false;
		} else if (!moneda.equals(other.moneda))
			return false;
		if (origen == null) {
			if (other.origen != null)
				return false;
		} else if (!origen.equals(other.origen))
			return false;
		if (proveedor == null) {
			if (other.proveedor != null)
				return false;
		} else if (!proveedor.equals(other.proveedor))
			return false;
		if (rfc == null) {
			if (other.rfc != null)
				return false;
		} else if (!rfc.equals(other.rfc))
			return false;
		if (tc == null) {
			if (other.tc != null)
				return false;
		} else if (!tc.equals(other.tc))
			return false;
		if (total == null) {
			if (other.total != null)
				return false;
		} else if (!total.equals(other.total))
			return false;
		return true;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SIMPLE_STYLE,false);
	}

}
