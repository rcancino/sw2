package com.luxsoft.sw3.embarque;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Para Papel lo importa es
 * 	
 *    Que los pedidos se entregen en tiempo
 *    
 *    
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_EMBARQUES"
	,uniqueConstraints=@UniqueConstraint(columnNames={"SUCURSAL","DOCUMENTO"})
	)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO",discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue("LOCAL")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Embarque extends BaseBean implements Replicable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="EMBARQUE_ID")
	protected String id;
	
	@SuppressWarnings("unused")
	@Version
	private int version;
	
	@Column(name="DOCUMENTO",nullable=false)
	private Long documento;
	
	@Column (name="SUCURSAL",nullable=false, length=50)
	private String sucursal;
	
	/*@ManyToOne (optional=true,fetch=FetchType.EAGER)
	@JoinColumn (name="SUCURSAL_ID", nullable=true, updatable=true)
	@NotNull
	*/
	@Transient
	private Sucursal sucursalID;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha=new Date();
	
	@Column(name = "CERRADO", nullable = true)
	@Type(type = "timestamp")
	private Date cerrado;
	
	@Column(name = "SALIDA", nullable = true)
	@Type(type = "timestamp")
	private Date salida;
	
	@Column(name = "REGRESO", nullable = true)
	@Type(type = "timestamp")
	private Date regreso;;
	
	@Column(name="COMENTARIO")
	@Length(max=255, message="El tamaño maximo del comentario es de 255 caracteres")
	private String comentario;
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "TRANSPORTE_ID", nullable = false, updatable = true)
	@NotNull(message="Debe indicar el transporte")
	private Transporte transporte;
	
	@Column(name="CHOFER", nullable=false)
	@NotNull(message="Debe indicar el chofer")
	private String chofer;
	
	@Column(name="KILOMETRO_INI",nullable=false)
	private long kilometroInicial;
	
	@Column(name="KILOMETRO_FIN",nullable=false)
	private long kilometroFinal;
	
	@Column(name="VALOR",nullable=false)
	private BigDecimal valor=BigDecimal.ZERO;
	
	@Formula("(select ifnull(sum(X.VALOR),0) FROM SX_ENTREGAS X where X.EMBARQUE_ID=EMBARQUE_ID)")
	private BigDecimal valorCalculado=BigDecimal.ZERO;
	
	@Formula("(select ifnull(sum(X.KILOS),0) FROM SX_ENTREGAS X where X.EMBARQUE_ID=EMBARQUE_ID)")
	private double kilos=0;
	
	@CollectionOfElements (fetch=FetchType.LAZY)
	@JoinTable(
			name="SX_EMBARQUES_INCIDENTES",
			joinColumns=@JoinColumn(name="EMBARQUE_ID")	
	)
	@Fetch(value=FetchMode.SUBSELECT)
	@AccessType (value="field")
	private Set<Incidente> incidentes=new HashSet<Incidente>();
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.EAGER,mappedBy="embarque")
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE
			})
	//@Fetch(value=FetchMode.SUBSELECT)
	private Set<Entrega> partidas=new HashSet<Entrega>();
	
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
	
	@Column(name="TX_IMPORTADO",nullable=true)
	private Date importado;
	
	@Column(name="TX_REPLICADO",nullable=true)
	private Date replicado;

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	
	

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getSalida() {
		return salida;
	}

	public void setSalida(Date horaDeSalida) {
		this.salida = horaDeSalida;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		Object old=this.comentario;
		this.comentario = comentario;
		firePropertyChange("comentario", old, comentario);
	}

	public Transporte getTransporte() {
		return transporte;
	}

	public void setTransporte(Transporte transporte) {
		Object old=this.transporte;
		this.transporte = transporte;
		firePropertyChange("transporte", old, transporte);
		if(transporte!=null){
			setChofer(transporte.getChofer().getNombre());
		}
		
	}

	public String getChofer() {
		return chofer;
	}

	public void setChofer(String chofer) {
		Object old=this.chofer;
		this.chofer = chofer;
		firePropertyChange("chofer", old, chofer);
	}

	public long getKilometroInicial() {
		return kilometroInicial;
	}

	public void setKilometroInicial(long kilometroInicial) {
		long old=this.kilometroInicial;
		this.kilometroInicial = kilometroInicial;
		firePropertyChange("kilometroInicial", old, kilometroInicial);
	}

	public long getKilometroFinal() {
		return kilometroFinal;
	}

	public void setKilometroFinal(long kilometroFinal) {
		this.kilometroFinal = kilometroFinal;
	}	

	

	public Set<Incidente> getIncidentes() {
		return incidentes;
	}

	public void setIncidentes(Set<Incidente> incidentes) {
		this.incidentes = incidentes;
	}

	public UserLog getLog() {
		if(log==null){
			log=new UserLog();
		}
		return log;
	}

	public void setLog(UserLog log) {
		this.log = log;
	}

	public AdressLog getAddresLog() {
		if(addresLog==null){
			addresLog=new AdressLog();
		}
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}

	public String getId() {
		return id;
	}	

	public Date getCerrado() {
		return cerrado;
	}

	public void setCerrado(Date cerrado) {
		this.cerrado = cerrado;
	}

	public Set<Entrega> getPartidas() {
		return partidas;
	}

	public boolean agregarUnidad(final Entrega ue){
		ue.setEmbarque(this);
		return partidas.add(ue);
	}

	public Date getRegreso() {
		return regreso;
	}

	public void setRegreso(Date regreso) {
		this.regreso = regreso;
	}

	public BigDecimal getValor() {
		return valor;
	}
	

	public BigDecimal getValorCalculado() {
		return valorCalculado;
	}

	public void setValor(BigDecimal valor) {
		Object old=this.valor;
		this.valor = valor;
		firePropertyChange("valor", old, valor);
	}
	
	

	public double getKilos() {
		return kilos;
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
		Embarque other = (Embarque) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString(){
		return MessageFormat.format("{0} - {1}  Salida: {2,date,medium}", this.id,this.sucursal,this.salida);
	}

	public Sucursal getSucursalID() {
		return sucursalID;
	}

	public void setSucursalID(Sucursal sucursalID) {
		this.sucursalID = sucursalID;
	}
	
	

}
