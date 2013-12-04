package com.luxsoft.siipap.model.gastos;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.UserLog;

/**
 * Enajenacion de Activo fijo
 * 
 * @author Ruben Cancino
 *
 */
@Entity @Table(name="SW_ENAJENACION")
public class Enajenacion extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	private Long id;
		
	private ActivoFijo activo;
	
	@Type(type="date")
	@Column(name="FECHA",nullable=false)
	@NotNull
	private Date fecha;
	
	@Type(type="com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns={
			 @Column(name="IMPORTE",scale=2)
			,@Column(name="IMPORTE_MON",length=3)		
	})
	private CantidadMonetaria importe=CantidadMonetaria.pesos(0);
	
	@Embedded
	private UserLog log=new UserLog();
	
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

	public ActivoFijo getActivo() {
		return activo;
	}
	public void setActivo(ActivoFijo activo) {
		this.activo = activo;
	}	

	public CantidadMonetaria getImporte() {
		return importe;
	}
	public void setImporte(CantidadMonetaria importe) {
		this.importe = importe;
	}

	public UserLog getLog() {
		return log;
	}
	public void setLog(UserLog log) {
		this.log = log;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((activo == null) ? 0 : activo.hashCode());
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
		final Enajenacion other = (Enajenacion) obj;
		if (activo == null) {
			if (other.activo != null)
				return false;
		} else if (!activo.equals(other.activo))
			return false;
		return true;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}
	

}
