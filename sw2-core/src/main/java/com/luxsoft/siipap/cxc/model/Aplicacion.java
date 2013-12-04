package com.luxsoft.siipap.cxc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Serie;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Parent;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.EntityUserLog;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Aplicacion de pago o abono a una cuenta por cobrar
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@Table(name = "SX_CXC_APLICACIONES")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TIPO",discriminatorType=DiscriminatorType.STRING,length=4)
@GenericGenerator(name="hibernate-uuid",strategy="uuid"
		,parameters={
				@Parameter(name="separator",value="-")
			}
		)
public abstract class Aplicacion implements EntityUserLog,Serializable
{
	
	static final long serialVersionUID = 89594L;

	
	@Id @GeneratedValue(generator="hibernate-uuid")
	@Column(name="APLICACION_ID")
	protected String id;
	
	
	
	@Column(name="fecha",nullable=false)
	@Type(type = "date")
	@NotNull	
	private Date fecha=new Date();
	
	@ManyToOne(optional = false,fetch=FetchType.EAGER)
    @JoinColumn(name = "ABONO_ID", nullable = false, updatable = false,insertable=false)
	private Abono abono;
	
	@ManyToOne(optional = false
			,cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.EAGER)
	@JoinColumn(name = "CARGO_ID", nullable = false)
	private Cargo cargo;
	
	@Column(name="IMPORTE",nullable=false)
	private BigDecimal importe=BigDecimal.ZERO;
	
	/**
	 * Para el caso de cargos(Ventas) a precio bruto, para la nota de credito
	 * de descuento
	 * 
	 * Util en reportes
	 */
	@Column(name="DESCUENT_NOTA",nullable=false)
	private BigDecimal descuentoPorNota=BigDecimal.ZERO;
	
	@Column(name="COMENTARIO")
	private String comentario;
	
	@ManyToOne(optional = true,cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch=FetchType.LAZY)
	@JoinColumn(name = "AUTORIZACION_ID", nullable = true)
	@Cascade(value={org.hibernate.annotations.CascadeType.REPLICATE})
	private AutorizacionDeAplicacionCxC autorizacion;
	
	@Embedded
	private UserLog log=new UserLog();

	public String getId() {
		return id;
	}
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Abono getAbono() {
		return abono;
	}

	public void setAbono(Abono abono) {
		this.abono = abono;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}	

	public BigDecimal getImporte() {
		return importe;
	}
	
	public CantidadMonetaria getImporteCM(){
		return new CantidadMonetaria(getImporte(),getAbono().getMoneda());
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public UserLog getLog() {
		if(log==null)
			log=new UserLog();
		return log;
	}
	
	public void setLog(UserLog log) {
		this.log = log;
	}	
	
	public BigDecimal getDescuentoPorNota() {
		return descuentoPorNota;
	}

	public void setDescuentoPorNota(BigDecimal descuentoPorNota) {
		this.descuentoPorNota = descuentoPorNota;
	}

	public abstract String getTipo();
	
	@Column(name="SIIPAP_ID",nullable=true)
	private Long siipapId;

	/**
	 * El Id del cargo en Siipapwin, si es una CXPFactura
	 * se refiere a SW_VENTAS.VENTA_ID si es una nota de cargo
	 * se refiere a SW_NOTAS.NOTA_ID
	 * @return
	 */
	public Long getSiipapId() {
		return siipapId;
	}

	public void setSiipapId(Long siipapCargoId) {
		this.siipapId = siipapCargoId;
	}
	
	@Embedded
	private Detalle detalle=new Detalle();

	public Detalle getDetalle() {
		return detalle;
	}
	
	
	public void setDetalle(Detalle detalle) {
		this.detalle = detalle;
	}

	public void actualizarDetalle(){
		Detalle d=new Detalle();
		d.actualizar(cargo, abono);
		detalle=d;
		actualizarLog();
	}
	
	private void actualizarLog(){
		UserLog log=new UserLog();
		log.setCreado(new Date());
		log.setModificado(new Date());
		setLog(log);
		//log.setCreateUser(user);
		//log.setUpdateUser(user);
	}
	
	/**
	 * Calcula de forma dinamica el saldo pendiente del cargo considerando el importe de
	 * 
	 * @return
	 */
	public BigDecimal getPendienteDePago(){
		BigDecimal importeAplicable=BigDecimal.ZERO;
		BigDecimal aplicado=cargo.getAplicado();
		BigDecimal notaDescuento=cargo.getDescuentos();
		if(notaDescuento.doubleValue()==0){
			double descuentoNota=cargo.getDescuentoNota()/100;
			CantidadMonetaria importeDescuento=cargo.getSaldoSinPagosCM();
			importeDescuento=importeDescuento.multiply(descuentoNota);
			importeAplicable=cargo.getTotalCM().subtract(importeDescuento).amount();
			importeAplicable=importeAplicable.subtract(aplicado);
		}else{
			importeAplicable=cargo.getTotal().subtract(aplicado);
		}
		return importeAplicable.subtract(getImporte());
	}
	
	/**
	 * Calcula en forma dinamica el  % de descuento
	 * real con respecto al cargo
	 * 
	 * @return
	 */
	public double getDescuentoAplicado(){		
		BigDecimal total=cargo.getTotal();
		BigDecimal devoluciones=cargo.getDevoluciones();
		BigDecimal desc=cargo.getDescuentos();
		BigDecimal bonific=cargo.getBonificaciones();
		
		BigDecimal valor=total
			.subtract(devoluciones)
			.subtract(desc)
			.subtract(bonific);
		double res;
		if(valor.doubleValue()>0)
			res=getImporte().doubleValue()/valor.doubleValue();
		else
			res=0;		
		return res;
	}
	
	

	public String toString(){
		if((detalle!=null) && (detalle.getDocumento()!=null)){
			return detalle.getDocumento().toString();
		}else 
			return cargo.toString();
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cargo == null) ? 0 : cargo.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		result = prime * result + ((importe == null) ? 0 : importe.hashCode());
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
		Aplicacion other = (Aplicacion) obj;
		if (cargo == null) {
			if (other.cargo != null)
				return false;
		} else if (!cargo.equals(other.cargo))
			return false;
		if (fecha == null) {
			if (other.fecha != null)
				return false;
		} else if (!fecha.equals(other.fecha))
			return false;
		if (importe == null) {
			if (other.importe != null)
				return false;
		} else if (!importe.equals(other.importe))
			return false;
		return true;
	}




	@Embeddable
	public   static class Detalle implements Serializable{
		
		static final long serialVersionUID = 8959L;
		
		@Column(name="CAR_DOCTO")
		private Long documento;
		
		@Parent
		private Aplicacion aplicacion;
		
		@Column(name="CAR_ORIGEN",length=3)
		private String origen;
		
		@Column(name="CAR_TIPO",length=3)
		private String carTipo;
		
		@Column(name="CAR_PBRUTO")
		private boolean precioBruto;
		
		@Column(name="CAR_POSTF")
		private boolean postFechado;
		
		@Column(name="CAR_SUCURSAL")
		private String sucursal;
		
		@Column(name="CAR_FECHA")
		@Type(type = "date")
		private Date fechaCargo;
		
		@Column(name = "CLAVE", length = 7)
		private String clave;
		
		@Column(name = "NOMBRE")
		private String nombre;
		
		@Column(name="ABN_FOLIO")
		private String folio;
		
		@Column(name = "ABN_DESCRIPCION",length=50)
		private String formaDePago;
		
		@Column(name = "ABN_BANCO",length=50)
		private String banco;
		
		
		@Column(name="CAR_JURIDICO")
		private boolean carJuridico=false;
		
		@Column(name = "CAR_TOTAL", nullable = false)
		private BigDecimal totalCargo = BigDecimal.ZERO;
		
		@Column(name="CE",nullable=false,columnDefinition=" bit default false")
		private boolean contraEntrega=false;
		
		@Column(name="CAR_ANTICIPO",nullable=false)
		private boolean carAnticipo=false;
		
		public void actualizar(Cargo cargo,final Abono abono){
			setDocumento(cargo.getDocumento());
			setOrigen(cargo.getOrigen().name());
			if(cargo.isJuridico())
				setOrigen(OrigenDeOperacion.JUR.name());
			//setCarJuridico(cargo.isJuridico());
			setPrecioBruto(cargo.isPrecioBruto());
			setPostFechado(cargo.isPostFechado());
			setSucursal(cargo.getSucursal().getNombre());
			setClave(cargo.getClave());
			setNombre(cargo.getNombre());
			setFechaCargo(cargo.getFecha());
			setTotalCargo(cargo.getTotal());
			if(cargo instanceof Venta){
				Venta v=(Venta)cargo;
				setContraEntrega(v.isContraEntrega());
			}
				
			if(abono instanceof NotaDeCredito){
				NotaDeCredito nota=(NotaDeCredito)abono;
				setFolio(String.valueOf(nota.getFolio()));
				setFormaDePago(nota.getInfo());
			}else if(abono instanceof PagoConCheque){
				PagoConCheque p=(PagoConCheque)abono;
				setFolio(String.valueOf(p.getNumero()));
				setFormaDePago(p.getInfo());
				setBanco(p.getBanco());
			}else if(abono instanceof PagoConTarjeta){
				PagoConTarjeta p=(PagoConTarjeta)abono;
				setFolio(p.getAutorizacionBancaria());
				String msg="TAR";
				if(p.getTarjeta()!=null){
					if(p.getTarjeta().isDebito()){
						msg+="_DEB";
					}else{
						if(StringUtils.containsIgnoreCase(p.getTarjeta().getNombre(),"AMERICAN"))
							msg+="_AMEX";
						else
							msg+="_CRED";
					}
				}
				//setFormaDePago(p.getInfo());
				setFormaDePago(msg);
				setBanco(p.getBanco());
			}else if(abono instanceof PagoConDeposito){
				PagoConDeposito p=(PagoConDeposito)abono;
				setFolio(p.getReferenciaBancaria());
				setFormaDePago(p.getInfo());
				setBanco(p.getBanco());
			}else if(abono instanceof PagoConEfectivo){
				PagoConEfectivo p=(PagoConEfectivo)abono;
				setFormaDePago("EFECTIVO");
				setBanco(p.getBanco());
			}else if(abono instanceof PagoDeDiferencias){
				PagoDeDiferencias p=(PagoDeDiferencias)abono;
				setFormaDePago(p.getInfo());
			}else if(abono instanceof PagoEnEspecie){
				PagoEnEspecie pe=(PagoEnEspecie)abono;
				setFormaDePago(pe.getInfo());
			}
			if(abono instanceof Pago){
				Pago p=(Pago)abono;
				if(p.isAnticipo()){
					String fp=getFormaDePago();
					setFormaDePago("ANT  "+fp);
				}else{
					if(p.getAplicaciones().size()>1){
						Date SAF=p.getPrimeraAplicacion();
						if(SAF!=null){
							Date fechaAplicacion=new Date();
							boolean sameDay=DateUtils.isSameDay(SAF, fechaAplicacion);
							if(!sameDay){
								String fp=getFormaDePago();
								setFormaDePago("SAF  "+fp);
							}
						}
						
					}
				}
			}
			setCarTipo(cargo.getTipoDocto());
			if(cargo instanceof Venta){
				Venta v=(Venta)cargo;
				setCarAnticipo(v.isAnticipo());
			}
			
		}

		public Long getDocumento() {
			return documento;
		}

		public void setDocumento(Long documento) {
			this.documento = documento;
		}

		public String getOrigen() {
			return origen;
		}

		public void setOrigen(String origen) {
			this.origen = origen;
		}

		public boolean isPrecioBruto() {
			return precioBruto;
		}

		
		public Date getFechaCargo() {
			return fechaCargo;
		}

		public void setFechaCargo(Date fechaCargo) {
			this.fechaCargo = fechaCargo;
		}

		public void setPrecioBruto(boolean precioBruto) {
			this.precioBruto = precioBruto;
		}

		public boolean isPostFechado() {
			return postFechado;
		}

		public void setPostFechado(boolean postFechado) {
			this.postFechado = postFechado;
		}

		public String getSucursal() {
			return sucursal;
		}

		public void setSucursal(String sucursal) {
			this.sucursal = sucursal;
		}

		public String getClave() {
			return clave;
		}

		public void setClave(String clave) {
			this.clave = clave;
		}

		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		public String getFolio() {
			return folio;
		}

		public void setFolio(String folio) {
			this.folio = folio;
		}

		public String getFormaDePago() {
			return formaDePago;
		}

		public void setFormaDePago(String formaDePago) {
			this.formaDePago = formaDePago;
		}

		public String getBanco() {
			return banco;
		}

		public void setBanco(String banco) {
			this.banco = banco;
		}

		public boolean isCarJuridico() {
			return carJuridico;
		}

		public void setCarJuridico(boolean carJuridico) {
			this.carJuridico = carJuridico;
		}

		public boolean isContraEntrega() {
			return contraEntrega;
		}

		public void setContraEntrega(boolean contraEntrega) {
			this.contraEntrega = contraEntrega;
		}

		public Aplicacion getAplicacion() {
			return aplicacion;
		}

		public void setAplicacion(Aplicacion aplicacion) {
			this.aplicacion = aplicacion;
		}

		public BigDecimal getTotalCargo() {
			return totalCargo;
		}

		public void setTotalCargo(BigDecimal totalCargo) {
			if(totalCargo==null)
				totalCargo=BigDecimal.ZERO;
			this.totalCargo = totalCargo;
		}

		public String getCarTipo() {
			return carTipo;
		}

		public void setCarTipo(String carTipo) {
			this.carTipo = carTipo;
		}

		public boolean isCarAnticipo() {
			return carAnticipo;
		}

		public void setCarAnticipo(boolean carAnticipo) {
			this.carAnticipo = carAnticipo;
		}

		
		
		
		
	}
	
	/**
	 * Propiedad temporal util para la UI
	 *  
	 */
	@Transient
	private BigDecimal importeDescuento=BigDecimal.ZERO;

	public BigDecimal getImporteDescuento() {
		return importeDescuento;
	}

	public void setImporteDescuento(BigDecimal importeDF) {
		this.importeDescuento = importeDF;
	}

	public AutorizacionDeAplicacionCxC getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(AutorizacionDeAplicacionCxC autorizacion) {
		this.autorizacion = autorizacion;
	}

	
	


}
