package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

import com.luxsoft.siipap.model.core.Cancelacion;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Tabla para el control de las cancelaciones de cargos
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_CXC_CARGOS_CANCELADOS")
public class CancelacionDeCargo extends Cancelacion implements Replicable,Serializable{
	
	static final long serialVersionUID = 89596L;
	
	@OneToOne(optional=false)
    @JoinColumn(name="CARGO_ID",unique=true)
    @Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private Cargo cargo;
	
	@ManyToOne(optional = false,cascade=CascadeType.ALL)
	@JoinColumn(name = "AUT_ID", nullable = false)
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private AutorizacionParaCargo autorizacion;
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;

	@Override
	public String getInfo() {		
		return "CARGO: "+getDocumento();
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public AutorizacionParaCargo getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(AutorizacionParaCargo autorizacion) {
		this.autorizacion = autorizacion;
	}

	public Date getImportado() {
		return importado;
	}

	public void setImportado(Date importado) {
		this.importado = importado;
	}

	public Date getReplicado() {
		return replicado;
	}

	public void setReplicado(Date replicado) {
		this.replicado = replicado;
	}

	
	
	
	
}
