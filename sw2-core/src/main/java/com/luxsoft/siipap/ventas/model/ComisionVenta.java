package com.luxsoft.siipap.ventas.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.UserLog;

/**
 * Entidad para persistir la comision de los vendedores y/o cobradores
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_COMISIONES")
public  class ComisionVenta extends BaseBean{
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="COMISION_ID")
	private Long id;	
	
	@OneToOne(optional=false)
	@JoinColumn(name="CARGO_ID", unique=true, nullable=false, updatable=false)
	private Venta venta;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="COBRADOR_ID",nullable=true)
	private Cobrador cobrador;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="VENDEDOR_ID",nullable=true)
	private Vendedor vendedor;
	
	/** Datos del vendedor/cobrador **/
	
	@Column(name="COBRADOR_NOM",nullable=true)
	private String cobradorNombre;
	
	
	@Column(name="VENDEDOR_NOM",nullable=true)
	private String vendedorNombre;
	
	/** Datos de la venta **/	
	
	@Column (name="SUCURSAL",nullable=false, length=50)
	private String sucursal;
	
	@Column(name="ORIGEN",length=3,nullable=false)
	private String origen;
	
	@Column(name = "NOMBRE", nullable = false)
	private String nombre;
	
	@Column(name = "CLAVE", length = 7, nullable=false)
	private String clave;
	
	@Column(name = "FECHA_DOCTO", nullable = false)
	@Type(type = "date")
	private Date fechaDocto = new Date();
	
	@Column(name="VTO_DOCTO",nullable=false)
    @Type(type="date")
    private Date vencimiento;
	
	
	@Column(name = "DOCTO",nullable=false)
	private Long documento;
	
	@Column(name = "NUMERO_FISCAL",nullable=false)
	private Integer fiscal;
	
	@Column(name = "TOTAL_DCTO", nullable = false)
	private BigDecimal total ;
	
	/** Datos de los pagos **/
	
	@Column(name="FECHA_PAGO",nullable=false)
    @Type(type="date")
	private Date fechaDelPago;
	
	@Column(name="FECHA_INI",nullable=false)
    @Type(type="date")
	private Date fechaInicial;
	
	@Column(name="FECHA_FIN",nullable=false)
    @Type(type="date")
	private Date fechaFinal;
	
	
	/** Datos de la genracion de la comision **/
	
	
	
	@Column(name = "ATRASO", nullable = false)
	private int atraso;
	
	@Column(name="PAGO_COMISIONABLE",nullable=false)
	private BigDecimal pagoComisionable=BigDecimal.ZERO;
	
	@Column(name="COMISION_VEN",nullable=false,scale=4,precision=4)
	private double comisionVendedor=0;
	
	@Column(name="IMP_COM_VEND",nullable=false)
	private BigDecimal impComisionVend=BigDecimal.ZERO;
	
	@Column(name="COMISION_COB",nullable=false,scale=4,precision=4)
	private double comisionCobrador=0;
	
	@Column(name="IMP_COM_COB",nullable=false)
	private BigDecimal impComisionCob=BigDecimal.ZERO;
	
	@Column(name="CANCEL_COB",length=100)
	private String comentarioCancelacionCob;
	
	@Column(name="CANCEL_VEN",length=100)
	private String comentarioCancelacionVen;
	
	@Column(name="CANCEL_COB_FECHA",nullable=true)
	@Type(type="date")
	private Date fechaCancelacionCobrador;
	
	@Column(name="CANCEL_VEN_FECHA",nullable=true)
	@Type(type="date")
	private Date fechaCancelacionVendedor;
	
	@Column(name="ENVIADO")
	private Boolean enviado=Boolean.FALSE;
	
	@Column(name="REVISADA")
	private Boolean revisada=Boolean.FALSE;
	
	public Boolean isEnviado() {
		if(enviado==null)
			enviado=Boolean.FALSE;			
		return enviado;
	}

	public void setEnviado(Boolean enviado) {
		Object old=this.enviado;
		this.enviado = enviado;
		firePropertyChange("enviado", old, enviado);
	}
	
	
	public Boolean isRevisada() {
		if(revisada==null)
			revisada=Boolean.FALSE;			
		return revisada;
	}

	public void setRevisada(Boolean revisada) {
		Object old=this.revisada;
		this.revisada = revisada;
		firePropertyChange("revisada", old, revisada);
	}
	
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	public ComisionVenta(){}
	
	public ComisionVenta(Venta v){
		setVenta(v);
	}
	
	public Long getId() {
		return id;
	}
	

	public Venta getVenta() {
		return venta;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setVenta(Venta venta) {
		Object old=this.venta;
		this.venta = venta;
		firePropertyChange("venta", old, venta);
		if(venta!=null){
			this.documento=venta.getDocumento();
			this.fiscal=venta.getNumeroFiscal();
			this.sucursal=venta.getSucursal().getNombre();
			this.origen=venta.getOrigen().name();
			this.nombre=venta.getCliente().getNombre();
			this.clave=venta.getCliente().getClave();
			this.cobrador=venta.getCobrador();
			if(venta.getCobrador()==null){
				System.out.println("Cobrador nulo: "+venta.getId());
			}else
				this.cobradorNombre=venta.getCobrador().toString();
			this.vendedor=venta.getVendedor();
			if(venta.getVendedor()!=null)
				this.vendedorNombre=venta.getVendedor().getNombres();
			this.fechaDocto=venta.getFecha();
			this.total=venta.getTotal();
			this.vencimiento=venta.getVencimiento();
			
		}else{
			this.documento=0L;
			this.fiscal=0;
			this.sucursal=null;
			this.origen=null;
			this.nombre=null;
			this.clave=null;
			this.cobrador=null;
			this.cobradorNombre=null;
			this.vendedor=null;
			this.fechaDocto=null;
			this.total=null;
			this.vencimiento=null;
		}
		
	}
	

	
	
	public Cobrador getCobrador() {
		return cobrador;
	}


	public void setCobrador(Cobrador cobrador) {
		this.cobrador = cobrador;
	}


	public Vendedor getVendedor() {
		return vendedor;
	}


	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}


	public String getCobradorNombre() {
		return cobradorNombre;
	}

	public String getVendedorNombre() {
		return vendedorNombre;
	}

	public String getClave() {
		return clave;
	}


	public Date getFechaDocto() {
		return fechaDocto;
	}

	public Date getVencimiento() {
		return vencimiento;
	}

	public Integer getFiscal() {
		return fiscal;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public Date getFechaDelPago() {
		return fechaDelPago;
	}

	
	

	public void setFechaDelPago(Date fechaDelPago) {
		this.fechaDelPago = fechaDelPago;
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}


	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}


	public Date getFechaFinal() {
		return fechaFinal;
	}


	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}


	public int getAtraso() {
		return atraso;
	}


	public void setAtraso(int atraso) {
		this.atraso = atraso;
	}


	public double getComisionVendedor() {
		return comisionVendedor;
	}


	public void setComisionVendedor(double comisionVendedor) {
		this.comisionVendedor = comisionVendedor;
	}


	public BigDecimal getImpComisionVend() {
		return impComisionVend;
	}


	public void setImpComisionVend(BigDecimal impComisionVend) {
		Object old=this.impComisionVend;
		this.impComisionVend = impComisionVend;
		firePropertyChange("impComisionVend", old, impComisionVend);
	}

	public double getComisionCobrador() {
		return comisionCobrador;
	}

	public void setComisionCobrador(double comisionCobrador) {
		double old=this.comisionCobrador;
		this.comisionCobrador = comisionCobrador;
		firePropertyChange("comisionCobrador", old, comisionCobrador);
	}

	public BigDecimal getImpComisionCob() {
		return impComisionCob;
	}

	public void setImpComisionCob(BigDecimal impComisionCob) {
		Object old=this.impComisionCob;
		this.impComisionCob = impComisionCob;
		firePropertyChange("impComisionCob", old, impComisionCob);
	}

	public String getComentarioCancelacionCob() {
		return comentarioCancelacionCob;
	}

	public void setComentarioCancelacionCob(String comentarioCancelacionCob) {
		Object old=this.comentarioCancelacionCob;
		this.comentarioCancelacionCob = comentarioCancelacionCob;
		firePropertyChange("comentarioCancelacionCob", old, comentarioCancelacionCob);
	}
	
	public String getComentarioCancelacionVen() {
		return comentarioCancelacionVen;
	}

	public void setComentarioCancelacionVen(String comentarioCancelacionVen) {
		Object old=this.comentarioCancelacionVen;
		this.comentarioCancelacionVen = comentarioCancelacionVen;
		firePropertyChange("comentarioCancelacionVen", old, comentarioCancelacionVen);
	}

	public Date getFechaCancelacionCobrador() {
		return fechaCancelacionCobrador;
	}

	public void setFechaCancelacionCobrador(Date fechaCancelacionCobrador) {
		this.fechaCancelacionCobrador = fechaCancelacionCobrador;
	}

	public Date getFechaCancelacionVendedor() {
		return fechaCancelacionVendedor;
	}

	public void setFechaCancelacionVendedor(Date fechaCancelacionVendedor) {
		this.fechaCancelacionVendedor = fechaCancelacionVendedor;
	}


	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
	
	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public Long getDocumento() {
		return documento;
	}

	public void setDocumento(Long documento) {
		this.documento = documento;
	}	

	

	public BigDecimal getPagoComisionable() {
		return pagoComisionable;
	}


	public void setPagoComisionable(BigDecimal pagoComisionable) {
		this.pagoComisionable = pagoComisionable;
	}

	public Periodo getPeriodo(){
		return new Periodo(getFechaInicial(),getFechaFinal());
	}

	public UserLog getUserLog() {
		return userLog;
	}

	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}
	
	public void actualizarComisiones(){
		if(this.cobrador!=null){
			BigDecimal pagoRev=BigDecimal.ZERO;
			BigDecimal pagoEnv=BigDecimal.ZERO;
			BigDecimal pagoCom=this.pagoComisionable.multiply(BigDecimal.valueOf(this.comisionCobrador/100));
			this.comisionCobrador=this.cobrador.getComision();
			 if(this.isRevisada()){
				 pagoRev=pagoCom.multiply(new BigDecimal(.75));
			 }if(!this.isEnviado()){
				 pagoEnv=pagoCom.multiply(new BigDecimal(.25));
			 }
			setImpComisionCob(pagoRev.add(pagoEnv));
			
		}
		if(this.vendedor!=null){
			//this.comisionVendedor=this.origen.equals("CRE")?this.vendedor.getComision():this.vendedor.getComisionContado();			
			setImpComisionVend(this.pagoComisionable.multiply(BigDecimal.valueOf(this.comisionVendedor/100)));
		}
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(obj==this){
        	return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ComisionVenta other = (ComisionVenta) obj;
        return new EqualsBuilder()
        .append(sucursal,other.getSucursal())
        .append(origen, other.getOrigen())
        .append(documento, other.getDocumento())
        .isEquals();
        
    }

    @Override
    public int hashCode() {
       return new HashCodeBuilder(17,35)
       .append(sucursal)
       .append(origen)
       .append(documento)
       .toHashCode();
    }    


    public String toString(){
    	return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
    	.append(this.sucursal)
    	.append(this.origen)
    	.append(this.documento)
    	.append(this.fiscal)
    	.append(this.total)
    	.append(this.pagoComisionable)
    	//.append(this.getImpComision())
    	.toString();
    }
	
	

}
