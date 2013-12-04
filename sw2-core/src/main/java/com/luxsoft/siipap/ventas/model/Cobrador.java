package com.luxsoft.siipap.ventas.model;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.AssertTrue;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;

@Entity
@Table(name = "SX_COBRADORES")
public class Cobrador extends BaseBean{

	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "APELLIDOP")
	private String apellidoP;

	@Column(name="APELLIDOM")
	private String apellidoM;

	@Column(name="NOMBRES",nullable=false)
	private String nombres;

	@Column(name = "RFC", length = 20)
	private String rfc;

	@Column(name = "CURP", length = 18)
	private String curp;
	
	@Column(name = "ACTIVO",nullable=false)
	private boolean activo=false;
	
	@Column(name = "COMISION",nullable=false)
	private  double comision=0;
	
	@Embedded
	private UserLog log=new UserLog();

	public Long getId() {
		return id;
	}

	public String getApellidoP() {
		return apellidoP;
	}

	public void setApellidoP(String apellidoP) {
		this.apellidoP = apellidoP;
	}

	public String getApellidoM() {
		return apellidoM;
	}

	public void setApellidoM(String apellidoM) {
		this.apellidoM = apellidoM;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getCurp() {
		return curp;
	}

	public void setCurp(String curp) {
		this.curp = curp;
	}
	
	public String toNombre() {
		String pattern = "{0} ({1})";
        return MessageFormat.format(pattern,  getNombres(),id);
    }
	
	public double getComision() {
		return comision;
	}

	public void setComision(double comision) {
		this.comision = comision;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,35)
		.append(id)
		.append(nombres)
		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if(obj==null) return false;
		if (getClass() != obj.getClass())
			return false;
		Cobrador other = (Cobrador) obj;
		return new EqualsBuilder()
		.append(id, other.getId())
		.append(nombres, other.getNombres())
		.isEquals();
	}

	public String toString(){
		return this.nombres;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}
	
	/**
	 * 
	 * @return
	 */
	@AssertTrue(message="El formato de la comision debe ser  de 0 a 99 ")
	public boolean validarComision(){
		return (comision>=0&& comision<100);
	}

}
