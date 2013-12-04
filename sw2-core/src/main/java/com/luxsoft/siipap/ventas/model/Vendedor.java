package com.luxsoft.siipap.ventas.model;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.validator.AssertTrue;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;

@Entity
@Table(name = "SX_VENDEDORES")
public class Vendedor extends BaseBean{

	@TableGenerator(
            name="idsGen", 
            table="SX_TABLE_IDS", 
            pkColumnName="GEN_KEY", 
            valueColumnName="GEN_VALUE", 
            pkColumnValue="VENDEDOR_ID", 
            allocationSize=1)
	@Id @Column(name="CLAVE")
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
	
	@Column(name = "ACTIVO", nullable=false)
	private boolean activo=false;
	
	@Column(name = "COMISION",nullable=false)
	private  double comision=0;
	
	@Column(name = "COMISION_CON",nullable=false)
	private  double comisionContado=0;

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
		String pattern = "{0} {1} {2}";
        return MessageFormat.format(pattern, getApellidoP(), getApellidoM(), getNombres());
    }

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 71;
		result = prime * result
				+ ((apellidoM == null) ? 0 : apellidoM.hashCode());
		result = prime * result
				+ ((apellidoP == null) ? 0 : apellidoP.hashCode());
		result = prime * result + ((nombres == null) ? 0 : nombres.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(obj==null) return false;
		if (getClass() != obj.getClass())
			return false;
		Vendedor other = (Vendedor) obj;
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
		return toNombre();
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	
	@Embedded
	private UserLog log=new UserLog();

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}
	
	public double getComision() {
		return comision;
	}

	public void setComision(double comision) {
		this.comision = comision;
	}
	
	

	public double getComisionContado() {
		return comisionContado;
	}



	public void setComisionContado(double comisionContado) {
		this.comisionContado = comisionContado;
	}



	/**
	 * 
	 * @return
	 */
	@AssertTrue(message="El formato de la comision debe ser  de 0 a 99 ")
	public boolean validarComision(){
		return (comision>=0&& comision<100);
	}
	
	@AssertTrue(message="El formato de la comision debe ser  de 0 a 99 ")
	public boolean validarComisionContado(){
		return (comisionContado>=0&& comisionContado<100);
	}
	
	public String getNombreCompleto(){
		String pattern="{0} {1}";
		return MessageFormat.format(pattern, getNombres(),getApellidoP());
	}

}
