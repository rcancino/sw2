package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name="SX_CXP_RECIBOS")
public class ContraRecibo extends BaseBean {
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="RECIBO_ID")
	private Long id;
	
	@Column (name="FECHA",nullable=false)
	@Type (type="date")
	@NotNull
	private Date fecha=new Date();
	
	
	@ManyToOne (optional=false)
	@JoinColumn (name="PROVEEDOR_ID", nullable=false)
	@NotNull
	private Proveedor proveedor;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="recibo")
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<ContraReciboDet> partidas=new HashSet<ContraReciboDet>();
	
	
	@Column (name="TOTAL")
	private BigDecimal total=BigDecimal.ZERO;
	
	@Column(name="COMENTARIO")
	@Length(max=255)
	private String comentario;
	
	@Transient
	private Currency moneda=MonedasUtils.PESOS;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		Object old=this.proveedor;
		this.proveedor = proveedor;
		firePropertyChange("proveedor", old, proveedor);
	}

	public Set<ContraReciboDet> getPartidas() {
		return partidas;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		Object old =this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}
	
	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public boolean agregarPartida(final ContraReciboDet det){
		Assert.notNull(det,"La partida a agregar no debe ser nula");
		det.setRecibo(this);
		det.setVencimiento(CXPUtils.calcularVencimiento(getFecha(), det.getFecha(), getProveedor()));
		return partidas.add(det);
	}
	
	public boolean removerPartida(final ContraReciboDet det){
		Assert.notNull(det,"La partida no debe ser nula");		
		boolean res= partidas.remove(det);
		if(res)
			det.setRecibo(null);
		return res;
	}
	

	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		ContraRecibo c=(ContraRecibo)o;
		return new EqualsBuilder()
		.append(getId(),c.getId())
		.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(getId())
		.toHashCode();
	}
	
	public void actualizarImporte(){
		CantidadMonetaria importe=CantidadMonetaria.pesos(0);
		for(ContraReciboDet du:partidas){
			importe=importe.add(CantidadMonetaria.pesos(du.getTotal().doubleValue()));
		}
		setTotal(importe.amount());
	}

	@Override
	public String toString() {
		String pattern="Id: {0} {1} {2} {3} {4}";
		return MessageFormat.format(pattern
				,getId()
				,getProveedor()
				,getFecha()
				,getTotal()
				);
	}

	
	@Embedded
	private UserLog log=new UserLog();


	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoenda(Currency moneda) {
		Object old=this.moneda;
		this.moneda = moneda;
		firePropertyChange("moneda", old, moneda);
	}
	
	
    
	
}