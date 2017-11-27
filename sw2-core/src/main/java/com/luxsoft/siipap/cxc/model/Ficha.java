package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.sw3.replica.Replicable;

/**
 * Entidad para registrar las fichas de deposito por concepto de pago de un cliente
 * 
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name="SX_FICHAS")
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public class Ficha implements Replicable,Serializable{
	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="FICHA_ID")
	protected String id;
	
	@Column(name="FOLIO")	
	private Integer folio=0;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGEN", nullable = false, length = 3)
	private OrigenDeOperacion origen=OrigenDeOperacion.MOS;
	
	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha = new Date();
	
	@Formula("(select sum(X.CHEQUE) FROM SX_FICHASDET X where X.FICHA_ID=FICHA_ID )")
	private BigDecimal cheque;
	
	@Formula("(select sum(X.EFECTIVO) FROM SX_FICHASDET X where X.FICHA_ID=FICHA_ID )")
	private BigDecimal efectivo;
	
	@ManyToOne(optional = false)			
	@JoinColumn(name = "CUENTA_ID", nullable = false)
	private Cuenta cuenta;
	
	@Column(name="COMENTARIO")
	private String comentario;
	
	@Column(name="TOTAL",nullable=false)
	private BigDecimal total=BigDecimal.ZERO;
	
	//@Enumerated (EnumType.STRING)
	//@Column(name="TIPO",nullable=false,updatable=false)
	@Column(name="TIPO_FICHA" ,length=50)
	private String tipoDeFicha;
	
	@Column(name="EVALORES",nullable=false)
	private boolean envioForaneo=true;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch= FetchType.LAZY			
				)
	@JoinColumn(name="FICHA_ID",nullable=false)
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE})
	@IndexColumn(name="RENGLON",base=1)
	private List<FichaDet> partidas=new ArrayList<FichaDet>();
	

	@Column(name="CREADO_USERID",updatable=false,length=50)
	private String createUser;
	
    @Column(name="MODIFICADO_USERID",length=50)
    private String updateUser;
	
    @Type (type="timestamp")
	@Column(name="CREADO")
	private Date creacion=new Date();
	
    @Type (type="timestamp")
	@Column(name="MODIFICADO")
	private Date modificado;
    
    @ManyToOne(optional = true
			,fetch=FetchType.EAGER
			,cascade={CascadeType.MERGE,CascadeType.PERSIST})			
	@JoinColumn(name = "CARGOABONO_ID", nullable = true)
	private CargoAbono ingreso;
    
    @Column(name = "FECHA_CORTE")
	private Date corte;
    
    @Column(name = "CANCELADA")
	private Date cancelada; 
    
    @Column(name="CIERRE")
	private Boolean cierre=Boolean.FALSE;
    
    @Column(name="ANTICIPO_CORTE")
	private Boolean anticipoCorte=Boolean.FALSE;
    
    @Column(name = "FECHA_DEP")
	private Date fechaDep = new Date();
    
    
	public Date getfechaDep() {
		return fechaDep;
	}

	public void setFechaDep(Date fechaDep) {
		this.fechaDep = fechaDep;
	}
    
    public Boolean isAnticipoCorte() {
		if(anticipoCorte==null)
			anticipoCorte=Boolean.FALSE;			
		return anticipoCorte;
	}

	public void setAnticipoCorte(Boolean anticipoCorte) {
		Object old=this.anticipoCorte;
		this.anticipoCorte = anticipoCorte;
		
	}
    
	
	public Boolean isCierre() {
		if(cierre==null)
			cierre=Boolean.FALSE;			
		return cierre;
	}

	public void setCierre(Boolean cierre) {
		Object old=this.cierre;
		this.cierre = cierre;
		
	}
	
	public Boolean getCierre() {
		return cierre;
	}
    
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

	public String getId() {
		return id;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	public OrigenDeOperacion getOrigen() {
	        return origen;
	}
	public void setOrigen(OrigenDeOperacion origen) {
	        this.origen = origen;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	

	public BigDecimal getCheque() {
		if(cheque==null) cheque=BigDecimal.ZERO;
		return cheque;
	}

	public BigDecimal getEfectivo() {
		if(efectivo==null) efectivo=BigDecimal.ZERO;
		return efectivo;
	}

	public BigDecimal getImporte() {
		return getCheque().add(getEfectivo());
	}	

	public Cuenta getCuenta() {
		return cuenta;
	}

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	

	public List<FichaDet> getPartidas() {
		return partidas;
	}
	
	public void agregarPartida(final FichaDet det){
		det.setFicha(this);
		partidas.add(det);
		
	}
	
	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}	
	
	public Date getCreacion() {
		return creacion;
	}

	public Date getModificado() {
		return modificado;
	}	
	
	

	public void setModificado(Date modificado) {
		this.modificado = modificado;
	}

	public Integer getFolio() {
		return folio;
	}

	public void setFolio(Integer folio) {
		this.folio = folio;
	}
	
	public String getTipoDeFicha() {
		return tipoDeFicha;
	}

	public void setTipoDeFicha(String tipoDeFicha) {
		this.tipoDeFicha = tipoDeFicha;
	}
	
	public String toString(){
		String pattern="Folio:{0} del ({1,date,short})";
		return MessageFormat.format(pattern, folio,fecha);
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
		Ficha other = (Ficha) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	public static String FICHA_OTROSBANCOS="OTROS BANCOS";
	public static String FICHA_MISMO_BANCO="MISMO BANCO";
	public static String FICHA_EFECTIVO="EFECTIVO";
	
	public static enum TiposDeFicha{
		EFECTIVO,MISMO_BANCO,OTROS_BANCOS
	}

	public boolean isEnvioForaneo() {
		return envioForaneo;
	}

	public void setEnvioForaneo(boolean envioForaneo) {
		this.envioForaneo = envioForaneo;
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

	public AdressLog getAddresLog() {
		if(addresLog==null){
			addresLog=new AdressLog();
		}
		return addresLog;
	}

	public void setAddresLog(AdressLog addresLog) {
		this.addresLog = addresLog;
	}

	public void setCreacion(Date creacion) {
		this.creacion = creacion;
	}

	public CargoAbono getIngreso() {
		return ingreso;
	}

	public void setIngreso(CargoAbono ingreso) {
		this.ingreso = ingreso;
	}

	public Date getCorte() {
		return corte;
	}

	public void setCorte(Date corte) {
		this.corte = corte;
	}

	public Date getCancelada() {
		return cancelada;
	}

	public void setCancelada(Date cancelada) {
		this.cancelada = cancelada;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public void actualizarTotal(){
		if(getTipoDeFicha().equals(FICHA_EFECTIVO))
			return;
		BigDecimal tot=BigDecimal.ZERO;
		for(FichaDet det:getPartidas()){
			tot=tot.add(det.getImporte());
		}
		setTotal(tot);
	}

	

}
