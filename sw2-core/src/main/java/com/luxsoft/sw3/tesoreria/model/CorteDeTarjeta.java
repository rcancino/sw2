package com.luxsoft.sw3.tesoreria.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.sw3.model.AdressLog;

@Entity
@Table(name="SX_CORTE_TARJETAS")
public class CorteDeTarjeta extends BaseBean{

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="CORTE_ID")
	protected Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	@NotNull(message="La sucursal es mandatoria")
	private Sucursal sucursal;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha=new Date();
	
	@Column(name = "FECHA_CORTE")
	@Type(type = "date")
	private Date corte=new Date();
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)	
	@JoinColumn(name = "CUENTA_ID", nullable = false)
	@NotNull(message="La cuenta destino es mandatoria")
	private Cuenta cuenta;
	
	@Column(name="TARJETA_TIPO",nullable=false,length=20)
	@NotNull(message="Seleccione el tipo de tarjeta")
	@Length(max=20)
	private String tipoDeTarjeta="VISA/MASTERCARD";
	
	@Column(name="TOTAL",nullable=false)
	private BigDecimal total=BigDecimal.ZERO;
	
	/*@Column(name="TOTAL",nullable=false)
	private BigDecimal comisionCredito=BigDecimal.ZERO;
	
	@Column(name="TOTAL",nullable=false)
	private BigDecimal comisionDebito=BigDecimal.ZERO;*/
	
	@Column(name="COMENTARIO")
	@Length(max=250)
	private String comentario;
	
	@ManyToOne(optional = true,fetch=FetchType.EAGER,cascade={CascadeType.MERGE,CascadeType.PERSIST})			
	@JoinColumn(name = "CARGOABONO_ID", nullable = true)
	private CargoAbono ingreso;
	
	@CollectionOfElements(fetch=FetchType.LAZY)
	@JoinTable(
			name = "SX_CORTE_TARJETAS_APLICACIONES",
			joinColumns = @JoinColumn(name = "CORTE_ID")
			)
	//@Fetch(value=FetchMode.SUBSELECT)
	private Set<CargoAbonoPorCorte> aplicaciones=new HashSet<CargoAbonoPorCorte>();
	
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="corte")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN})			
	private Set<CorteDeTarjetaDet> partidas=new HashSet<CorteDeTarjetaDet>();
		
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createUser",	column=@Column(name="CREADO_USR"	,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updateUser",	column=@Column(name="MODIFICADO_USR",nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="creado", 		column=@Column(name="CREADO"		,nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="modificado", 	column=@Column(name="MODIFICADO"	,nullable=true,insertable=true,updatable=true))
	   })
	private UserLog log=new UserLog();
	
	@Embedded
	@AttributeOverrides({
	       @AttributeOverride(name="createdIp",	column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedIp",	column=@Column(nullable=true,insertable=true,updatable=true)),
	       @AttributeOverride(name="createdMac",column=@Column(nullable=true,insertable=true,updatable=false)),
	       @AttributeOverride(name="updatedMac",column=@Column(nullable=true,insertable=true,updatable=true))
	   })
	private AdressLog addresLog=new AdressLog();
	
	public static String[] TIPOS_DE_TARJETAS={"VISA/MASTERCARD","AMEX"};

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		Object old=this.sucursal;
		this.sucursal = sucursal;
		firePropertyChange("sucursal", old, sucursal);
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}

	public Date getCorte() {
		return corte;
	}

	public void setCorte(Date corte) {
		Object old=this.corte;
		this.corte = corte;
		firePropertyChange("corte", old, corte);
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public void setCuenta(Cuenta cuenta) {
		Object old=this.cuenta;
		this.cuenta = cuenta;
		firePropertyChange("cuenta", old, cuenta);
	}

	public String getTipoDeTarjeta() {
		return tipoDeTarjeta;
	}

	public void setTipoDeTarjeta(String tipoDeTarjeta) {
		Object old=this.tipoDeTarjeta;
		this.tipoDeTarjeta = tipoDeTarjeta;
		firePropertyChange("tipoDeTarjeta", old, tipoDeTarjeta);
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		Object old=this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public CargoAbono getIngreso() {
		return ingreso;
	}

	public void setIngreso(CargoAbono ingreso) {
		this.ingreso = ingreso;
	}	

	public Set<CorteDeTarjetaDet> getPartidas() {
		return partidas;
	}
	
	public boolean agregarPartida(CorteDeTarjetaDet det){
		det.setCorte(this);
		return partidas.add(det);
	}
	
	public boolean eliminarPartida(CorteDeTarjetaDet det){
		det.setCorte(null);
		return partidas.remove(det);
	}

	public UserLog getLog() {
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public AdressLog getAddresLog() {
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}
	

	public Set<CargoAbonoPorCorte> getAplicaciones() {
		return aplicaciones;
	}
	
	
	
	public void setAplicaciones(Set<CargoAbonoPorCorte> aplicaciones) {
		this.aplicaciones = aplicaciones;
	}

	public boolean agregarAplicacion(CargoAbonoPorCorte aplic){
		aplic.setCorte(this);
		return this.aplicaciones.add(aplic);
	}
	

	public void actualizarTotal(){
		BigDecimal total=BigDecimal.ZERO;
		for(CorteDeTarjetaDet det:getPartidas()){
			total=total.add(det.getPago().getTotal());
		}
		setTotal(total);
	}	
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		CorteDeTarjeta other = (CorteDeTarjeta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString(){
		return this.sucursal.getNombre()+"  "+this.id;
	}
	
}
