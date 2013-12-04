package com.luxsoft.siipap.inventarios.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Movimiento generico de inventario
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_MOVI")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Movimiento extends BaseBean implements Replicable{
	
	
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="MOVI_ID")
	protected String id;
	
	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@Column(name="FECHA",nullable=false)
	private Date fecha=new Date();
	
	@ManyToOne (optional=false)
    @JoinColumn (name="SUCURSAL_ID", nullable=false,updatable=false)    
	private Sucursal sucursal;

	@Enumerated(EnumType.STRING)
	@Column (name="CONCEPTO",nullable=false, length=3)
	@NotNull
	public Concepto concepto;
	
	
	@Column(name="COMENTARIO",length=150)
	private String comentario;
	
	@Column(name="PORINVENTARIO")
	private Boolean porInventario;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="movimiento")	
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN,org.hibernate.annotations.CascadeType.REPLICATE})
	@Fetch(value=FetchMode.SUBSELECT)
	private Set<MovimientoDet> partidas=new HashSet<MovimientoDet>();
	
	@ManyToOne(optional = true,cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.LAZY)
	@JoinColumn(name = "AUTORIZACION_ID", nullable = true)
	private AutorizacionDeMovimiento autorizacion;
	
	@Embedded
	private UserLog userLog=new UserLog();

	@Column(name="DOCUMENTO",nullable=true)	
	private int documento=0;	

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	

	

	public int getDocumento() {
		return documento;
	}

	public void setDocumento(int documento) {
		this.documento = documento;
	}

	public String getId() {
		return id;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	public Concepto getConcepto() {
		return concepto;
	}

	public void setConcepto(Concepto concepto) {
		Object old=this.concepto;
		this.concepto = concepto;
		firePropertyChange("concepto", old, concepto);
	}

	public UserLog getUserLog() {
		return userLog;
	}

	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}
	
	public AutorizacionDeMovimiento getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(AutorizacionDeMovimiento autorizacion) {
		this.autorizacion = autorizacion;
	}

	public Set<MovimientoDet> getPartidas() {
		return Collections.unmodifiableSet(partidas);
	}
	
	public List<MovimientoDet> getPartidasAsList(){
		return new ArrayList<MovimientoDet>(getPartidas());
	}
	
	public boolean agregarPartida(final MovimientoDet det){
		det.setMovimiento(this);
		Assert.notNull(sucursal,"No se pueden generar movimientos sin asignar primero la sucursal");
		Assert.notNull(concepto,"No se pueden generar movimientos sin asignar primero el concepto");
		//System.out.println("**************************** Agregando partida"+ det.getClave()+"-------"+ det.getSucursal().getNombre() );
		det.setSucursal(getSucursal());
		det.setConcepto(getConcepto().name());
		//det.setRenglon(partidas.size()+1);
		return partidas.add(det);
	}
	
	public boolean eliminarPartida(final MovimientoDet det){
		det.setMovimiento(null);
		return partidas.remove(det);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
		result = PRIME * result + ((sucursal == null) ? 0 : sucursal.hashCode());
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
		final Movimiento other = (Movimiento) obj;
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
		if (sucursal == null) {
			if (other.sucursal != null)
				return false;
		} else if (!sucursal.equals(other.sucursal))
			return false;
		return true;
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append(getSucursal())
		.append(getFecha())
		.toString();
	}
	
	
	


	public static enum Concepto{
		AJU("Ajuste de Inventario Fisico",0)
		,CIM("Corrección de inventario mensual",0)
		,CIS("Consumo Interno",-1)
		,MER("Merma",-1)
		,RMC("Reposición de material",-1)
		,OIM("Otros Ingresos De Mercancia",1)
		,VIR("Ingreso De Viruta",1)
		;
		
		
		private final String desc;
		private final int tipo;
		
		private Concepto(String desc,int tipo){
			this.desc=desc;
			this.tipo=tipo;
		}

		public int getTipo() {
			return tipo;
		}

		public String toString(){
			return name()+"  "+desc;
		}
		
		public double ajustar(double cantidad){
			if(tipo==0)
				return cantidad;
			else if(tipo==-1) 
				return Math.abs(cantidad)*-1;
			else 
				return Math.abs(cantidad);
		}
	}
	
	@Column(name="TX_IMPORTADO",nullable=true)
	protected Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	protected Date replicado;

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

	public Boolean getPorInventario() {
		if(porInventario==null)
			porInventario=Boolean.FALSE;
		return porInventario;
	}

	public void setPorInventario(Boolean porInventario) {
		Object old=this.porInventario;
		this.porInventario = porInventario;
		firePropertyChange("porInventario", old, porInventario);
	}
	
	
	
}
