package com.luxsoft.sw3.caja;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.model.AddressLoggable;
import com.luxsoft.sw3.model.AdressLog;
import com.luxsoft.siipap.model.BaseBean;

/**
 * Entidad para registrar la entrada y salida de importes
 * 
 * @author Ruben Cancino Ramos
 * 
 */
@Entity
@Table(name = "SX_CAJA")
@GenericGenerator(name = "hibernate-uuid", strategy = "uuid", parameters = { @Parameter(name = "separator", value = "-") })
public class Caja implements AddressLoggable{

	@Id
	@GeneratedValue(generator = "hibernate-uuid")
	@Column(name = "ID")
	private String id;

	@Version
	private int version;
	
	@Column(name="FOLIO")
	private long folio;

	@ManyToOne(optional = false)
	@JoinColumn(name = "SUCURSAL_ID", nullable = false, updatable = false)
	private Sucursal sucursal;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGEN", nullable = true, length = 3)
	private OrigenDeOperacion origen=OrigenDeOperacion.MOS;
	
	@Column(name="FECHA",nullable=false)
	@NotNull(message="FECHA")
	@Type(type = "date")
	private Date fecha=new Date();
	
	@ManyToOne(optional = true,cascade={CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE})
	@JoinColumn(name = "GASTO_ID", nullable = true)
	private Gasto gasto;
	
	@Column(name="FICHA")
	private String ficha;
	
	@Column(name="PAGO_HXE")
	private String pagoCambioCheque;
	
	
	
	/**
	 * Commodity para capturar un importe q en funcion del tipo y concepto se transforma
	 * en fondo para caja o deposito
	 */
	@Transient
	private BigDecimal importe=BigDecimal.ZERO;

	@Column(name = "CAJA", nullable = false)
	private BigDecimal caja=BigDecimal.ZERO;
	
	@Column(name = "DEPOSITO", nullable = false)
	private BigDecimal deposito=BigDecimal.ZERO;
	
	@Column(name = "PAGOS", nullable = false)
	private BigDecimal pagos=BigDecimal.ZERO;
	
	@Column(name = "CORTE_ACUMULADO", nullable = false)
	private BigDecimal cortesAcumulados=BigDecimal.ZERO;
	
	@Transient
	private BigDecimal cambiosDeCheque=BigDecimal.ZERO;
	
	@Transient
	private BigDecimal disponible=BigDecimal.ZERO;

	@Column(name = "MONEDA", nullable = false)
	private Currency moneda = MonedasUtils.PESOS;

	@Column(name = "TC", nullable = false)
	private double tc = 1;

	@Column(name = "CORTE", nullable = false)
	@Type(type = "timestamp")
	private Date corte;

	@Column(name = "IMPRESO", nullable = true)
	@Type(type = "timestamp")
	private Date impreso;

	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO", nullable = false, length = 20)
	//@Length(max=20)
	private Tipo tipo;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "CONCEPTO", nullable = false, length = 20)	
	private Concepto concepto=Concepto.CORTE_CAJA;
	
	@Column(name="COMENTARIO")
	private String comentario;
	
	@Column(name="CHEQUE_NUMERO")
	private long chequeNumero;
	
	@Column(name="CHEQUE_NOMBRE")
	private String chequeNombre;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "BANCO_ID", nullable = true)
	private Banco banco;
	
	@Transient
	private List<Gasto> rembolsos=new ArrayList<Gasto>();
	
	@Transient
	private Tarjeta tarjeta;
	
	@Transient
	private String numeroDeAutorizacion;
	
	@Column(name="CIERRE")
	private Boolean cierre=Boolean.FALSE;
	
	
	 @Column(name = "FECHA_DEP")
		private Date fechaDep = new Date();
	 
	 
	 @Column(name="ANTICIPO_CORTE")
		private Boolean anticipoCorte=Boolean.FALSE;
	    
	    
	    public Boolean isAnticipoCorte() {
			if(anticipoCorte==null)
				anticipoCorte=Boolean.FALSE;			
			return anticipoCorte;
		}

		public void setAnticipoCorte(Boolean anticipoCorte) {
			Object old=this.anticipoCorte;
			this.anticipoCorte = anticipoCorte;
			
		}
		public Boolean getanticipoCorte() {
			return anticipoCorte;
		}
	
	    
	    
		public Date getfechaDep() {
			return fechaDep;
		}

		public void setFechaDep(Date fechaDep) {
			this.fechaDep = fechaDep;
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
	@AttributeOverrides( {
			@AttributeOverride(name = "createUser", column = @Column(name = "CREADO_USR", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updateUser", column = @Column(name = "MODIFICADO_USR", nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "creado", column = @Column(name = "CREADO", nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "modificado", column = @Column(name = "MODIFICADO", nullable = true, insertable = true, updatable = true)) })
	private UserLog log = new UserLog();

	@Embedded
	@AttributeOverrides( {
			@AttributeOverride(name = "createdIp", column = @Column(nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updatedIp", column = @Column(nullable = true, insertable = true, updatable = true)),
			@AttributeOverride(name = "createdMac", column = @Column(nullable = true, insertable = true, updatable = false)),
			@AttributeOverride(name = "updatedMac", column = @Column(nullable = true, insertable = true, updatable = true)) })
	private AdressLog addresLog = new AdressLog();
	
	

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	

	public long getFolio() {
		return folio;
	}

	public void setFolio(long folio) {
		this.folio = folio;
	}


	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	public String getHora(){
		if(getCorte()!=null)
			return new SimpleDateFormat("HH:mm").format(getCorte());
		return new SimpleDateFormat("HH:mm").format(new Date());
	}

	public Gasto getGasto() {
		return gasto;
	}

	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
		if(gasto!=null){
			setCaja(gasto.getImporte().multiply(BigDecimal.valueOf(-1.0)));
			setConcepto(Concepto.FONDO_FIJO);
			setTipo(Tipo.GASTO);
			setComentario(gasto.getDescripcion());
		}else{
			//setCaja(BigDecimal.ZERO);
		}
	}


	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getCaja() {
		return caja;
	}

	public void setCaja(BigDecimal caja) {
		this.caja = caja;
	}

	public BigDecimal getDeposito() {
		return deposito;
	}

	public void setDeposito(BigDecimal deposito) {
		this.deposito = deposito;
	}

	public BigDecimal getPagos() {
		if(pagos==null)
			pagos=BigDecimal.ZERO;
		return pagos;
	}

	public void setPagos(BigDecimal pagos) {
		this.pagos = pagos;
	}
	
	
	
	public BigDecimal getCambiosDeCheque() {
		if(cambiosDeCheque==null){
			cambiosDeCheque=BigDecimal.ZERO;
		}
		return cambiosDeCheque;
	}

	public void setCambiosDeCheque(BigDecimal cambiosDeCheque) {
		this.cambiosDeCheque = cambiosDeCheque;
	}

	public BigDecimal getDisponibleCalculado(){
		return getPagos().add(getCambiosDeCheque()).subtract(getCortesAcumulados());
	}

	public BigDecimal getCortesAcumulados() {
		if(cortesAcumulados==null)
			cortesAcumulados=BigDecimal.ZERO;
		return cortesAcumulados;
	}

	public void setCortesAcumulados(BigDecimal cortesAcumulados) {
		this.cortesAcumulados = cortesAcumulados;
	}	
	
	public BigDecimal getDisponible() {
		return disponible;
	}

	public void setDisponible(BigDecimal disponible) {
		this.disponible = disponible;
	}

	public Currency getMoneda() {
		return moneda;
	}

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}

	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		this.tc = tc;
	}

	public Date getCorte() {
		return corte;
	}

	public void setCorte(Date corte) {
		this.corte = corte;
	}

	public Date getImpreso() {
		return impreso;
	}

	public void setImpreso(Date impreso) {
		this.impreso = impreso;
	}

	

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public Concepto getConcepto() {
		return concepto;
	}

	public void setConcepto(Concepto concepto) {
		this.concepto = concepto;
	}
	
	

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
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

	public String getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}
	
	public long getChequeNumero() {
		return chequeNumero;
	}

	public void setChequeNumero(long chequeNumero) {
		this.chequeNumero = chequeNumero;
	}

	public String getChequeNombre() {
		return chequeNombre;
	}

	public void setChequeNombre(String chequeNombre) {
		this.chequeNombre = chequeNombre;
	}
	
	

	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco chequeBanco) {
		this.banco = chequeBanco;
	}
	
	

	public String getFicha() {
		return ficha;
	}

	public void setFicha(String ficha) {
		this.ficha = ficha;
	}

	public String getPagoCambioCheque() {
		return pagoCambioCheque;
	}

	public void setPagoCambioCheque(String pagoCambioCheque) {
		this.pagoCambioCheque = pagoCambioCheque;
	}
	
	

	public Tarjeta getTarjeta() {
		return tarjeta;
	}

	public void setTarjeta(Tarjeta tarjeta) {
		this.tarjeta = tarjeta;
	}
	
	

	public String getNumeroDeAutorizacion() {
		return numeroDeAutorizacion;
	}

	public void setNumeroDeAutorizacion(String numeroDeAutorizacion) {
		this.numeroDeAutorizacion = numeroDeAutorizacion;
	}

	/**
	 * Aplica el importe definido usando como criterio el tipo de movimiento
	 * 
	 */
	public void aplicar(){
		switch (this.tipo) {
		case MORRALLA:			
			if(getImporte().doubleValue()<0){				
				setDeposito(getImporte());
			}
			setCaja(getImporte());
			break;
		case REMBOLSO:
			setCaja(getImporte());
		case CHEQUE:
			setDeposito(getImporte());
		case EFECTIVO:
			if(getConcepto().equals(Caja.Concepto.CORTE_CAJA))
				setDeposito(getImporte());
			else
				setCaja(getImporte());
		case DEPOSITO:
		case TRANSFERENCIA:
			setDeposito(getImporte());
			break;
		case TARJETA:
			setDeposito(getImporte());
		default:
			break;
		}
		
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
		Caja other = (Caja) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String toString() {
		String msg=String.format(" Corte: %1$te/%1$tm/%1$tY: %1$tr", getCorte());
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
		.append("Tipo:" ,getTipo())
		.append("Concepto",getConcepto())
		.append("Caja",getCaja())
		.append("Deposito",getDeposito())		
		.append(msg)
				.toString();
	}
	
	public static enum Tipo{
		EFECTIVO
		,CHEQUE
		,TARJETA
		,DEPOSITO
		,TRANSFERENCIA
		,REMBOLSO
		,GASTO
		,MORRALLA
	}
	
	
	public static enum Concepto{
		CORTE_CAJA,
		CAMBIO_CHEQUE,
		CAMBIO_TARJETA,
		FONDO_FIJO
	}
	
	/*@AssertTrue(message="Inconsistencia en los datos para el movimiento")
	public boolean validarDisponible(){
		if(concepto.equals(Concepto.CORTE_CAJA)){
			boolean res=getDisponibleCalculado().doubleValue()>=0;
			return res;
		}
		return true;
		
	}*/

	public List<Gasto> getRembolsos() {
		return rembolsos;
	}

	public void setRembolsos(List<Gasto> rembolsos) {
		this.rembolsos = rembolsos;
		if(rembolsos!=null){
			BigDecimal importe=BigDecimal.ZERO;
			for(Gasto g:rembolsos){
				importe=importe.add(g.getImporte());
			}
			setImporte(importe);
		}
	}
	
	
	@Transient
	public List<Ficha> fichasParaCheques;
	

}
