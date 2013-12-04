package com.luxsoft.siipap.model.gastos;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;

/**
 * Persona custodios de activo fijo
 * 
 * @author Ruben Cancino
 *
 */
@Entity @Table(name="SW_CONSIGNATARIOS")
public class Consignatario extends BaseBean{
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	private Long id;
	
	@Column(name="APELLIDOP",length=50,nullable=false)
	@NotNull @Length(max=50)
	private String apellidoP;
	
	@Column(length=50,nullable=false)
	@NotNull @Length(max=50)	
	private String apellidoM;
	
	@Column(length=150,nullable=false)
	@NotNull
	@Length(max=150)
	private String nombres;
	
	@ManyToOne (optional=true)
	@JoinColumn (name="SUCURSAL_ID", nullable=true)
	private Sucursal sucursal;
	
	@ManyToOne (optional=true)
	@JoinColumn (name="DEPARTAMENTO_ID", nullable=true)
	private Departamento departamento;
	
	@Column (name="EMPLADO_ID",length=5)
	@Length(max=5)
	private String empleadoId;
	
	@Length(max=200)
	private String comentario;
	
	@Embedded
	private UserLog log=new UserLog();
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getApellidoP() {
		return apellidoP;
	}
	public void setApellidoP(String apellidoP) {
		Object old=this.apellidoP;
		this.apellidoP = apellidoP;
		firePropertyChange("apellidoP", old, apellidoP);
	}
	
	public String getApellidoM() {
		return apellidoM;
	}
	public void setApellidoM(String apellidoM) {
		Object old=this.apellidoM;
		this.apellidoM = apellidoM;
		firePropertyChange("apellidoM", old, apellidoM);
	}
	
	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		Object old=this.nombres;
		this.nombres = nombres;
		firePropertyChange("nombres", old, nombres);
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Departamento getDepartamento() {
		return departamento;
	}
	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}

	public String getEmpleadoId() {
		return empleadoId;
	}
	public void setEmpleadoId(String empleadoId) {
		this.empleadoId = empleadoId;
	}
	

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	

	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((apellidoM == null) ? 0 : apellidoM.hashCode());
		result = PRIME * result + ((apellidoP == null) ? 0 : apellidoP.hashCode());
		result = PRIME * result + ((nombres == null) ? 0 : nombres.hashCode());
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
		final Consignatario other = (Consignatario) obj;
		if (apellidoM == null) {
			if (other.apellidoM != null)
				return false;
		} else if (!apellidoM.equals(other.apellidoM))
			return false;
		if (apellidoP == null) {
			if (other.apellidoP != null)
				return false;
		} else if (!apellidoP.equals(other.apellidoP))
			return false;
		if (nombres == null) {
			if (other.nombres != null)
				return false;
		} else if (!nombres.equals(other.nombres))
			return false;
		return true;
	}
	
	public String toString(){
		String pattern="{0} {1} {2}";
		return MessageFormat.format(pattern, getApellidoP(),getApellidoM(),getNombres());
	}

}
