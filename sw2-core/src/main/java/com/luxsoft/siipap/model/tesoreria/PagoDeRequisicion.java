package com.luxsoft.siipap.model.tesoreria;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.AutorizacionException;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.UserLog;

/**
 * Entidad para registrar el pago de una {@link Requisicion}
 * Ofrece comportamiento y estado para el desarrollo de este proceso
 * 
 * @author Ruben Cancino
 *@deprecated No usar mas
 */
@Entity
@Table(name="SW_TPAGO")
public class PagoDeRequisicion extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="PAGO_ID")
	private Long id;
	
	
	@OneToOne(optional=false,fetch=FetchType.EAGER)
	@JoinColumn(name="REQUISICION_ID")
	private Requisicion requisicion;
	
	
	@OneToOne(cascade={CascadeType.ALL},optional=false)
	@JoinColumn(name="CARGOABONO_ID")
	private CargoAbono cargo;
	
	@Type (type="date")
	@Column(name="FECHA",nullable=false)
	private Date fecha=new Date();
	
	@Column(name="COMENTARIO")
	private String comentario;
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	public PagoDeRequisicion() {}

	public PagoDeRequisicion(Requisicion requisicion) {
		this.requisicion = requisicion;
	}

	public CargoAbono getCargo() {
		return cargo;
	}
	public void setCargo(CargoAbono cargo) {
		this.cargo = cargo;
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
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

	public Requisicion getRequisicion() {
		return requisicion;
	}
	public void setRequisicion(Requisicion requisicion) {
		this.requisicion = requisicion;
	}

	public UserLog getUserLog() {
		return userLog;
	}
	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}
	
	
	public void registrarCargo(final Cuenta cta){		
		if(getRequisicion().getAutorizacion()==null){
			throw new AutorizacionException("La requisicion no esta autorizada");
		}
		CargoAbono cargo=CargoAbono.crearCargo(
				cta
				, getRequisicion().getTotal().amount()
				, getFecha()
				, getRequisicion().getAfavor()
				, getRequisicion().getConcepto()
				//, getRequisicion().getSucursal()
				,null
				);
		cargo.setAutorizacion(getRequisicion().getAutorizacion());
		this.cargo=cargo;
	}

	@Override
	public boolean equals(Object o) {
		if(o==null) return false;
		if(o==this) return true;
		if(getClass()!=o.getClass())
			return false;
		PagoDeRequisicion otro=(PagoDeRequisicion)o;
		return new EqualsBuilder()
		.append(this.requisicion.getId(), otro.getRequisicion().getId())
		.isEquals();
		
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return "";
	}
	

}
