package com.luxsoft.siipap.model.tesoreria;

import java.math.BigDecimal;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.UserLog;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 * Requisicion Detalle y sus entidades
 * 
 * @author Ruben Cancino
 *
 */

@Entity
@Table(name="SW_TREQUISICIONDET")
public class RequisicionDe extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="requisicionde_id")
	private Long id;
	
	@ManyToOne(fetch=FetchType.EAGER,optional=false)
    @JoinColumn(name="REQUISICION_ID", nullable=false,updatable=false)
	private Requisicion requisicion;
	
	@Column(name="DOCUMENTO", length=20,nullable=false)
	@NotNull @Length(max=20)
	private String documento;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="DEPARTAMENTO_ID",nullable=true)
	private Departamento departamento;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="SUCURSAL_ID",nullable=false)
	@NotNull
	private Sucursal sucursal;
	
	
	@Column(name="fechaD",nullable=false)	
	@Type (type="date")
	@NotNull
	private Date fechaDocumento=new Date();
	
	
	@Type(type="com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns={
			 @Column(name="IMPORTE",scale=2)
			,@Column(name="IMPORTE_MON",length=3)
		
	})
	private CantidadMonetaria importe=CantidadMonetaria.pesos(0);
	
	@Type(type="com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns={
			 @Column(name="IMPUESTO",scale=2)
			,@Column(name="IMPUESTO_MON",length=3)
		
	})
	private CantidadMonetaria impuesto=CantidadMonetaria.pesos(0);
	
	@Type(type="com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns={
			 @Column(name="TOTAL",scale=2)
			,@Column(name="TOTAL_MON",length=3)
		
	})
	private CantidadMonetaria total=CantidadMonetaria.pesos(0);
	
	@Type(type="com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns={
			 @Column(name="TOTAL_DOCTO",scale=2)
			,@Column(name="TOTAL_DOCTO_MON",length=3)
		
	})
	private CantidadMonetaria totalDocto=CantidadMonetaria.pesos(0);
	
	
	@Column(name="comentario", length=100)
	private String comentario;
	
	@ManyToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinTable(
			name = "SX_GAS_FACXREQ2",
			joinColumns = {@JoinColumn(name = "REQUISICIONESDET_ID")},
			inverseJoinColumns = {@JoinColumn(name = "FACTURA_ID")}
			)			
	private GFacturaPorCompra facturaDeGasto;
	
	@ManyToOne(optional=true,fetch=FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinColumn(name="CXP_ID",nullable=true)			
	private CXPCargo facturaDeCompras;
	
	/*@OneToOne(optional=true)
    @JoinColumn(name="ANALISIS_ID",  nullable=true, updatable=true)
    */
	@OneToOne(mappedBy="requisicionDet",fetch=FetchType.LAZY,cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			})
	//@Transient
	private AnalisisDeFactura analisis;
	
	
	@Embedded
	private UserLog userLog=new UserLog();
	
	
	/**
	 * Identificador unico en base de datos
	 * 
	 * @return
	 */
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Maestro de la requisicion
	 * 
	 * @return
	 */
	public Requisicion getRequisicion() {
		return requisicion;
	}
	public void setRequisicion(Requisicion requisicion) {
		this.requisicion = requisicion;
	}
	
	/**
	 * Ducumento del cual se esta requiriendo el pago
	 * 
	 * @return
	 */
	public String getDocumento() {
		return documento;
	}
	public void setDocumento(String documento) {
		Object old=this.documento;
		this.documento = documento;
		firePropertyChange("documento", old, documento);
	}
	
	/**
	 * Fecha del documento que del que se requiere el pago
	 * 
	 * @return
	 */
	public Date getFechaDocumento() {
		return fechaDocumento;
	}
	public void setFechaDocumento(Date fechaDocumento) {
		this.fechaDocumento = fechaDocumento;
	}

	/**
	 * Comentario relacionado con el documento 
	 * 
	 * @return
	 */
	public String getComentario() {
		return comentario;
	}
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
	
	/**
	 * Importe requerido para el pago
	 * 
	 * @return
	 */
	public CantidadMonetaria getImporte() {
		return importe;
	}
	public void setImporte(CantidadMonetaria importe) {
		this.importe = importe;
	}

	/**
	 * Impuesto requerido para el pago
	 * 
	 * @return
	 */
	public CantidadMonetaria getImpuesto() {
		//if(getFacturaDeGasto()!=null)
			//return getImpuestoMN();
		return impuesto;
	}
	public void setImpuesto(CantidadMonetaria impuesto) {
		this.impuesto = impuesto;
	}	

	/**
	 * Total del pago requerido
	 * 
	 * @return
	 */
	public CantidadMonetaria getTotal() {
		return total;
	}
	public void setTotal(CantidadMonetaria total) {
		Object old=this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}
	
	public CantidadMonetaria getTotalMN(){
		return getTotal().multiply(getTc());
	}
	
	public BigDecimal getTc(){
		if(getRequisicion()!=null){
			return getRequisicion().getTipoDeCambio();
		}
		return BigDecimal.ONE;
	}
	
	/**
	 * Departamento origen de la requisicion
	 * 
	 * @return {@link Departamento}
	 */	
	public Departamento getDepartamento() {
		return departamento;
	}
	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}
	
	/**
	 * 
	 * Sucursal origen de la requisición
	 * 
	 * @return {@link Sucursal}
	 */	
	public Sucursal getSucursal() {
		return sucursal;
	}
	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
		
	
	
	/**
	 * Bitacora de uso
	 * 
	 * @return
	 */
	public UserLog getUserLog() {
		return userLog;
	}
	public void setUserLog(UserLog userLog) {
		this.userLog = userLog;
	}
	
	/**
	 * Actualiza el importe y el impuesto a partir del total
	 * Util para cuando lo que se tiene es solo el total del pago
	 * 
	 *
	*/	
	public void actualizarDelTotal(){
		if(getFacturaDeGasto()!=null){
			actualizarImportesDeGastosProrrateado();			
		}else{
			double val=getTotal().amount().doubleValue();
			CantidadMonetaria tot=new CantidadMonetaria(val,getRequisicion().getMoneda());
			//CantidadMonetaria tot=new CantidadMonetaria(val,MonedasUtils.PESOS);
			CantidadMonetaria imp=MonedasUtils.calcularImporteDelTotal(tot);
			imp=imp.multiply(getRequisicion().getTipoDeCambio());
			setImporte(imp);			
			setImpuesto(MonedasUtils.calcularImpuesto(getImporte()));			
		}
		setTotal(getImporte().add(getImpuesto()));
		
	}
	 
	
	/**
	 * Actualizacion de impuesto y total con el iva al 15%
	 *
	 
	public void actualizarTotalStandar(){
		setImpuesto(MonedasUtils.calcularImpuesto(getImporte()));
		setTotal(MonedasUtils.calcularTotal(getImporte()));
	}
	*/
	
	public GFacturaPorCompra getFacturaDeGasto() {
		return facturaDeGasto;
	}
	public void setFacturaDeGasto(GFacturaPorCompra factura) {
		this.facturaDeGasto = factura;
	}
	
	
	
	
	public CantidadMonetaria getTotalDocto() {
		return totalDocto;
	}
	public void setTotalDocto(CantidadMonetaria totalDocto) {
		this.totalDocto = totalDocto;
	}
	/**
	 * Porcentaje de la factura que se esta requiriendo
	 *  
	 * @param facturaDeGasto
	 * @param valor
	 * @return
	 */
	public double prorrateo(){
		GFacturaPorCompra factura=getFacturaDeGasto();
		if(getFacturaDeGasto()!=null){			
			BigDecimal total=factura.getCompra().getTotal();
			if((total==null) || (total.equals(BigDecimal.ZERO)))
				return 1;
			double val=getTotal().amount().doubleValue()/factura.getTotalMN().amount().doubleValue();
			return val;
		}
		return 1;
		
	}
	
	
	public void actualizarImportesDeGastosProrrateado(){
		if(getFacturaDeGasto()!=null){
			CantidadMonetaria imp=new CantidadMonetaria(getFacturaDeGasto().getImporte().amount().doubleValue(),getRequisicion().getMoneda());
			setImporte(calcularMontoProrrateado(imp));
			CantidadMonetaria tax=new CantidadMonetaria(getFacturaDeGasto().getImpuesto().amount().doubleValue(),getRequisicion().getMoneda());
			setImpuesto(calcularMontoProrrateado(tax));
		}
		 
	}
		
	/**
	 * Regresa en pesos el monto prorrateado
	 * 
	 * @param valor
	 * @return
	 */
	private CantidadMonetaria calcularMontoProrrateado(CantidadMonetaria valor){
		if(getFacturaDeGasto()!=null){
			double pro=prorrateo();
			double vd=valor.multiply(pro).amount().doubleValue();
			CantidadMonetaria res=CantidadMonetaria.pesos(vd);
			res=res.multiply(getRequisicion().getTipoDeCambio());
			return res;
		}
		return null;
	}
	/*
	public AnalisisDeFactura getAnalisis() {
		return analisis;
	}
	public void setAnalisis(AnalisisDeFactura analisis) {
		this.analisis = analisis;
	}
	*/
	public CXPCargo getFacturaDeCompras() {
		return facturaDeCompras;
	}
	public void setFacturaDeCompras(CXPCargo facturaDeCompras) {
		this.facturaDeCompras = facturaDeCompras;
		if(facturaDeCompras!=null){
			setDocumento(facturaDeCompras.getDocumento());
			setFechaDocumento(facturaDeCompras.getFecha());
			setTotalDocto(new CantidadMonetaria(facturaDeCompras.getTotal(),facturaDeCompras.getMoneda()));
		}
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((documento == null) ? 0 : documento.hashCode());
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
		final RequisicionDe other = (RequisicionDe) obj;
		if (documento == null) {
			if (other.documento != null)
				return false;
		} else if (!documento.equals(other.documento))
			return false;
		return true;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE,false);
	}
	
	public CantidadMonetaria getIetu(){
		if(getFacturaDeGasto()!=null){
			CantidadMonetaria ietuCom=getFacturaDeGasto().getCompra().getIetu();
			return calcularMontoProrrateado(ietuCom);
		}else
			return CantidadMonetaria.pesos(0);
	}
	
	public CantidadMonetaria getImpuestoParaPolizaMN(){
		if(getFacturaDeGasto()!=null){
			CantidadMonetaria tax=getFacturaDeGasto().getCompra().getImpuestoEnCantidadMonetaria();
			return calcularMontoProrrateado(tax);
		}else
			return CantidadMonetaria.pesos(0);
	}
	
	public CantidadMonetaria getRetencion1(){
		if(getFacturaDeGasto()!=null){
			CantidadMonetaria r1=getFacturaDeGasto().getCompra().getRet1MN();
			CantidadMonetaria res= calcularMontoProrrateado(r1);
			return res;
		}else
			return CantidadMonetaria.pesos(0);
	}
	
	public CantidadMonetaria getRetencion2(){
		if(getFacturaDeGasto()!=null){
			return calcularMontoProrrateado(getFacturaDeGasto().getCompra().getRet2MN());
		}else
			return CantidadMonetaria.pesos(0);
	}
	public AnalisisDeFactura getAnalisis() {
		return analisis;
	}
	public void setAnalisis(AnalisisDeFactura analisis) {
		this.analisis = analisis;
	}
	
	
	
}
