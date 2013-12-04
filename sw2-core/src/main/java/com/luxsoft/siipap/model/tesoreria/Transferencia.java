package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;

/**
 * Transferencia de dinero entre cuentas.
 * Genera un cargo y un abono a las cuentas involucradas
 * 
 *  Pueden ser de la misma empresa
 * o entre empresas
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table (name="SW_TRANSFERENCIAS")
public class Transferencia {
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="TRANSFER_ID")
	private Long id;
	
	@SuppressWarnings("unused")
	@Version
	@AccessType(value="field")
	private int version;
	
	
	@ManyToOne (optional=false)
	@JoinColumn(name="ORIGEN_ID")
	private Cuenta origen;
	
	@ManyToOne (optional=false)
	@JoinColumn(name="DESTINO_ID",nullable=false,updatable=false)
	private Cuenta destino;
	
	@Column(name="REFERENCIA_ORI")
	private String referenciaOri;
	
	@Column(name="REFERENCIA_DEST")
	private String referenciaDest;
	
	@Type(type="date") @Column (name="FECHA",nullable=false)
	private Date fecha=new Date();
	
	@Column(name="IMPORTE_ORI",precision=19,scale=2,nullable=false)
	private BigDecimal importeOri=BigDecimal.ZERO;
	
	@Column(name="TC",precision=10,scale=4,nullable=false)
	private BigDecimal tc=BigDecimal.ONE;
	
	@Column(name="IMPORTE_DEST",precision=19,scale=2,nullable=false)
	private BigDecimal importeDest=BigDecimal.ZERO;
	
	@Column(name="COMISION",precision=19,scale=2,nullable=false)
	private BigDecimal comision=BigDecimal.ZERO;
	
	@Column(name="COMENTARIO")
	private String comentario;
	
	@ManyToOne (optional=false,cascade={CascadeType.PERSIST})
	@JoinColumn(name="AUT_ID",nullable=false,updatable=false)
	private Autorizacion autorizacion;
	
	@ManyToOne (optional=false,cascade={CascadeType.ALL})
	@JoinColumn(name="CARGO_ID",nullable=false)
	private CargoAbono cargo;
	
	@ManyToOne (optional=false,cascade={CascadeType.ALL})
	@JoinColumn(name="ABONO_ID",nullable=false)
	private CargoAbono abono;
	
	@ManyToOne (optional=true,cascade={CascadeType.ALL})
	@JoinColumn(name="COMISION_ID",nullable=true)
	//@Transient
	private CargoAbono cargoComision;
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	public Transferencia(){}	

	public CargoAbono getAbono() {
		return abono;
	}
	public void setAbono(CargoAbono abono) {
		this.abono = abono;
	}
	
	public CargoAbono getCargoComision() {
		return cargoComision;
	}
	public void setCargoComision(CargoAbono cargoComision) {
		this.cargoComision = cargoComision;
	}	

	public CargoAbono getCargo() {
		return cargo;
	}
	public void setCargo(CargoAbono cargo) {
		this.cargo = cargo;
	}
	
	public Autorizacion getAutorizacion() {
		return autorizacion;
	}
	public void setAutorizacion(Autorizacion autorizacion) {
		this.autorizacion = autorizacion;
	}

	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public BigDecimal getComision() {
		return comision;
	}
	public void setComision(BigDecimal comision) {
		this.comision = comision;
	}
	
	
	public Cuenta getDestino() {
		return destino;
	}
	public void setDestino(Cuenta destino) {
		this.destino = destino;
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

	public BigDecimal getImporteDest() {
		return importeDest;
	}

	public void setImporteDest(BigDecimal importeDest) {
		this.importeDest = importeDest;
	}

	public BigDecimal getImporteOri() {
		return importeOri;
	}

	public void setImporteOri(BigDecimal importeOri) {
		this.importeOri = importeOri;
	}

	public Cuenta getOrigen() {
		return origen;
	}

	public void setOrigen(Cuenta origen) {
		this.origen = origen;
	}

	public String getReferenciaDest() {
		return referenciaDest;
	}

	public void setReferenciaDest(String referenciaDest) {
		this.referenciaDest = referenciaDest;
	}

	public String getReferenciaOri() {
		return referenciaOri;
	}

	public void setReferenciaOri(String referenciaOri) {
		this.referenciaOri = referenciaOri;
	}

	public BigDecimal getTc() {
		return tc;
	}
	public void setTc(BigDecimal tc) {
		this.tc = tc;
	}
	
	public UserLog getUserLog() {
		return userLog;
	}
	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}
	
	/**
	 * FactorryMethod para la generacion de el cargo y abono de
	 * la transferencia
	 * 
	 * @return un arreglo de dos elementos CargoAbono siendo el primero
	 * 		   el Cargo y el ultimo el Abono
	 */
	public CargoAbono[] generarCargoAbono(){
		if(getImporteOri()==null || getImporteOri().equals(BigDecimal.ZERO)){
			throw new TransferenciaException("No se ha fijado el importe de la transferencia ");
		}
		if(getCargo()!=null ){
			throw new TransferenciaException("Los cargos para esta transferencia ya han sido generados");
		}
		
		// Cargo
		CargoAbono cargo=CargoAbono.crearCargo(getOrigen(), getImporteOri(), getFecha(),getDestino().getBanco().getNombre(),null,null);
		cargo.setFormaDePago(FormaDePago.TRANSFERENCIA);		
		cargo.setComentario(getComentario());
		setCargo(cargo);
		
		
		//Abono
		final BigDecimal newImporte=getImporteOri().multiply(getTc());
		final CargoAbono abono=CargoAbono.crearAbono(getDestino(), newImporte, getFecha(), null, null);
		abono.setFormaDePago(FormaDePago.TRANSFERENCIA);
		setImporteDest(abono.getImporte());
		abono.setComentario(getComentario());
		setAbono(abono);		
				
		return new CargoAbono[]{cargo,abono};
	}
	
	/**
	 * Permite generar un cargo a la cuenta origen por motivo de comision por transferencia
	 * Este cargo es muy comun en los bancos nacionales
	 * 
	 * @return El cargo para la comision
	 */
	public CargoAbono generarCargoPorComision(){
		
		if(getComision()==null || getComision().equals(BigDecimal.ZERO))
			return null;
		
		if(getCargoComision()!=null ){
			throw new TransferenciaException("El Cargo por comision para esta transferencia ya han sido generados");
		}
		
		BigDecimal comision=getComision().multiply(getTc());
		CargoAbono cargo=CargoAbono.crearCargo(getOrigen(), comision, getFecha(), getDestino().getBanco().getNombre(), null, null);
		cargo.setComentario("Comision por transferencia");
		setCargoComision(cargo);
		
		return cargo;
	}
	
	/**
	 * Comodity method para completar la inicializacion de los cagos y abono
	 * 
	 * @param s
	 * @param conceptoCargo
	 * @param conceptoComision
	 * @param conceptoAbono
	 * @param aut
	 */
	public void registrarDatos(final Sucursal s,final Concepto conceptoCargo,final Concepto conceptoComision
			,final Concepto conceptoAbono,final Autorizacion aut){
		if(getCargo()!=null){
			getCargo().setConcepto(conceptoCargo);
			getCargo().setSucursal(s);
			getCargo().setAutorizacion(aut);
			getAbono().setConcepto(conceptoAbono);
			getAbono().setSucursal(s);
			getAbono().setAutorizacion(aut);
			if(getCargoComision()!=null){
				getCargoComision().setConcepto(conceptoComision);
				getCargoComision().setSucursal(s);
				getCargoComision().setAutorizacion(aut);
			}
			
		}		
	}
	

	public boolean equals(Object other){
		if(other==null) return false;
		if(other==this) return true;
		Transferencia t=(Transferencia)other;
		return new EqualsBuilder()
		.append(getOrigen(), t.getOrigen())
		.append(getDestino(), t.getDestino())
		.append(getReferenciaOri(), t.getReferenciaOri())
		.append(getReferenciaDest(), t.getReferenciaDest())
		.append(getUserLog(), t.getUserLog())
		.append(getId(), t.getId())		
		.isEquals();
	}
	
	public int hashCode(){
		return new HashCodeBuilder(17,35)
		.append(getOrigen())
		.append(getDestino())
		.append(getReferenciaOri())
		.append(getReferenciaDest())
		.append(getUserLog())
		.append(getId())
		.toHashCode();
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE,false);
	}
	
	
	/**
	 * Bean para representar de una manera resumida el resultado de una transferencia
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static class Resultado{
		
		private Cuenta origen;
		private Cuenta destino;
		private CantidadMonetaria saldoOrigen;
		private CantidadMonetaria saldoDestino;
		
		public Resultado(Cuenta origen, Cuenta destino, CantidadMonetaria saldoOrigen, CantidadMonetaria saldoDestino) {
			this.origen = origen;
			this.destino = destino;
			this.saldoOrigen = saldoOrigen;
			this.saldoDestino = saldoDestino;
		}

		public Cuenta getDestino() {
			return destino;
		}
		
		public Cuenta getOrigen() {
			return origen;
		}
		
		public CantidadMonetaria getSaldoDestino() {
			return saldoDestino;
		}
		
		public CantidadMonetaria getSaldoOrigen() {
			return saldoOrigen;
		}
		
		public String toString(){
			String pattern="Transferencia por: {0} \n" +
					"Saldo final de la cta (Origen) {1} :\n {2}\n" +
					"Saldo final de la cta (Destino){3} :\n {4}";
			return MessageFormat.format(pattern
					,getOrigen().getNumero()
					,getSaldoOrigen()
					,getDestino().getNumero()
					,getSaldoDestino()					
					);
		}
		
	}	

}
