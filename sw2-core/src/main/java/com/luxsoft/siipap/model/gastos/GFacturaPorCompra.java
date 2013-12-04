package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.luxsoft.siipap.model.BaseBean;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.contabilidad.ContaUtils;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.model.tesoreria.Requisicion.Estado;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

@Entity
@Table(name="SW_FACTURAS_GASTOS")
public class GFacturaPorCompra extends BaseBean{
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne(optional=false)
	@JoinColumn (name="COMPRA_ID",nullable=false)
	private GCompra compra;
	
	@Column(name = "DOCUMENTO", length = 15, nullable = false)
	@NotNull
	@Length(max=15)
	private String documento;

	@Column(name = "FECHA", nullable = false)
	@Type(type = "date")
	private Date fecha=new Date();
	
	@Column(name = "FECHA_CONTA", nullable = false)
	@Type(type = "date")
	private Date fechaContable=new Date();

	@Column(name = "VTO", nullable = false)
	@Type(type = "date")
	private Date vencimiento;

	@Column(name = "PROVEEDOR", nullable = false)
	private String proveedor;

	@Column(name = "RFC", nullable = true, length = 15)
	private String rfc;
	
	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { 
			@Column(name = "IMPORTE", scale = 2,nullable=false),
			@Column(name = "IMPORTE_MON", length = 3,nullable=false)
	})
	@AccessType(value = "field")
	private CantidadMonetaria importe;

	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { 
			@Column(name = "IMPUESTO", scale = 2,nullable=false),
			@Column(name = "IMPUESTO_MON", length = 3,nullable=false)
	})
	@AccessType(value = "field")
	private CantidadMonetaria impuesto;

	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { 
			@Column(name = "TOTAL", scale = 2,nullable=false),
			@Column(name = "TOTAL_MON", length = 3,nullable=false)

	})
	@AccessType(value = "field")
	private CantidadMonetaria total;
	
	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { 
			@Column(name = "APAGAR", scale = 2,nullable=false),
			@Column(name = "APAGAR_MON", length = 3,nullable=false)

	})
	@AccessType(value = "field")
	private CantidadMonetaria apagar;
	
	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@Columns(columns = { 
			@Column(name = "DIFERENCIA", scale = 2,nullable=false),
			@Column(name = "DIFERENCIA_MON", length = 3,nullable=false)

	})
	@AccessType(value = "field")
	private CantidadMonetaria diferencia=CantidadMonetaria.pesos(0);;

	@Column(name = "MONEDA", nullable = false)
	private Currency moneda=MonedasUtils.PESOS;

	@Column(name = "TC", nullable = false,scale=4, precision=12)
	private BigDecimal tc=BigDecimal.ONE;

	@Type(type = "com.luxsoft.siipap.support.hibernate.CantidadMonetariaCompositeUserType")
	@AccessType(value = "field")
	@Columns(columns = { 
			@Column(name = "SALDO", scale = 2,nullable=false),
			@Column(name = "SALDO_MON", length = 3,nullable=false)
	})	
	private CantidadMonetaria saldo=CantidadMonetaria.pesos(0);
	
	
	@OneToMany(fetch=FetchType.EAGER
			,cascade={
				CascadeType.PERSIST
				,CascadeType.MERGE
				}
			,mappedBy="facturaDeGasto")
	//@org.hibernate.annotations.IndexColumn(name = "RENGLON")
	private Collection<RequisicionDe> requisiciones=new ArrayList<RequisicionDe>();
	
	
	public GFacturaPorCompra(){
	}
	
	

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}



	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}



	/**
	 * @return the compra
	 */
	public GCompra getCompra() {
		return compra;
	}

	/**
	 * @param compra the compra to set
	 */
	public void setCompra(GCompra compra) {
		this.compra = compra;
	}

	/**
	 * @return the documento
	 */
	public String getDocumento() {
		return documento;
	}

	/**
	 * @param documento the documento to set
	 */
	public void setDocumento(String documento) {
		Object old=this.documento;
		this.documento = documento;
		firePropertyChange("documento", old, documento);
	}

	/**
	 * @return the fecha
	 */
	public Date getFecha() {
		return fecha;
	}

	/**
	 * @param fecha the fecha to set
	 */
	public void setFecha(Date fecha) {
		Object old=this.fecha;
		this.fecha = fecha;
		firePropertyChange("fecha", old, fecha);
	}	
	
	/**
	 * Fecha para contablizar el documento. Actualmente solo se
	 * usa para lo relacionado con la generación de polizas contables
	 * 
	 * @return
	 */
	public Date getFechaContable() {
		return fechaContable;
	}
	public void setFechaContable(Date fechaContable) {
		Object old=this.fechaContable;
		this.fechaContable = fechaContable;
		firePropertyChange("fechaContable", old, fechaContable);
	}



	/**
	 * @return the importe
	 */
	public CantidadMonetaria getImporte() {
		if(getCompra()!=null){
			return getCompra().getImporteEnCantidadMonetaria();
		}
		return importe;
	}

	/**
	 * @param importe the importe to set
	 */
	public void setImporte(CantidadMonetaria importe) {
		Object old=this.importe;
		this.importe = importe;
		firePropertyChange("importe", old, importe);
	}

	/**
	 * @return the impuesto
	 */
	public CantidadMonetaria getImpuesto() {
		if(getCompra()!=null){
			return getCompra().getImpuestoEnCantidadMonetaria();
		}
		return impuesto;
	}

	/**
	 * @param impuesto the impuesto to set
	 */
	public void setImpuesto(CantidadMonetaria impuesto) {
		Object old=this.impuesto;
		this.impuesto = impuesto;
		firePropertyChange("impuesto", old, impuesto);
	}

	/**
	 * @return the moneda
	 */
	public Currency getMoneda() {
		return moneda;
	}

	/**
	 * @param moneda the moneda to set
	 */
	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}	

	/**
	 * @return the proveedor
	 */
	public String getProveedor() {
		return proveedor;
	}

	/**
	 * @param proveedor the proveedor to set
	 */
	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	/**
	 * @return the rfc
	 */
	public String getRfc() {
		return rfc;
	}

	/**
	 * @param rfc the rfc to set
	 */
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	/**
	 * @return the saldo
	 */
	public CantidadMonetaria getSaldo() {
		return saldo;
	}

	

	/**
	 * @return the tc
	 */
	public BigDecimal getTc() {
		return tc;
	}

	/**
	 * @param tc the tc to set
	 */
	public void setTc(BigDecimal tc) {
		this.tc = tc;
	}

	/**
	 * @return the total
	 */
	public CantidadMonetaria getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(CantidadMonetaria total) {
		Object old=this.total;
		this.total = total;
		firePropertyChange("total", old, total);
	}
	
	public CantidadMonetaria getApagar() {
		if(apagar==null){
			apagar=new CantidadMonetaria(0,getMoneda());
		}
		return apagar;
	}
	public void setApagar(CantidadMonetaria apagar) {
		Object old=this.apagar;
		this.apagar = apagar;
		firePropertyChange("apagar", old, apagar);
	}

	/**
	 * @return the vencimiento
	 */
	public Date getVencimiento() {
		return vencimiento;
	}

	/**
	 * @param vencimiento the vencimiento to set
	 */
	public void setVencimiento(Date vencimiento) {
		this.vencimiento = vencimiento;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((documento == null) ? 0 : documento.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final GFacturaPorCompra other = (GFacturaPorCompra) obj;
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
	
	public CantidadMonetaria getTotalPedido(){
		return getCompra().getTotalAsCantidadMonetaria();
	}
	public CantidadMonetaria getPendientePedido(){
		return getCompra().getSaldoPorRevisar();
	}
	
	public CantidadMonetaria getTotatMN(){
		return CantidadMonetaria.pesos(getTotal().amount().doubleValue()*getTc().doubleValue());
	}
	

	public Collection<RequisicionDe> getRequisiciones() {
		//return Collections.unmodifiableList(requisiciones);
		return Collections.unmodifiableCollection(requisiciones);
	}

	public boolean agregarRequisicion(final RequisicionDe req){
		return requisiciones.add(req);
	}
	public boolean eliminarRequisicion(final RequisicionDe req){
		return requisiciones.remove(req);
	}
	
	public void eliminarRequisiciones(){
		requisiciones.clear();
	}
	/**
	 * Actualiza el saldo de la factura en funcion de sus pagos
	 *
	 */
	public void actualizarSaldo(){		
		this.saldo=getSaldoCalculado();
		
	}
	
	/**
	 * Regrsa lo pagado en moneda nacional
	 * 
	 * @return
	 */
	public CantidadMonetaria getPagado(){
		CantidadMonetaria pagos=CantidadMonetaria.pesos(0);
		for(RequisicionDe det:requisiciones){
			if(det.getRequisicion().getEstado().equals(Estado.PAGADA)){
				pagos=pagos.add(det.getTotalMN());
			}
		}
		return pagos;
	}
	
	
	/**
	 * Regresa lo pagado a antes de la fecha indicada
	 * @param fecha
	 * @return
	 */
	public CantidadMonetaria getPagadoAlCorte(final Date fecha){
		CantidadMonetaria pagos=CantidadMonetaria.pesos(0);
		for(RequisicionDe det:requisiciones){
			//if(det.getRequisicion().getEstado().equals(Estado.PAGADA)){
			if(det.getRequisicion().getPago()!=null){
				if(det.getRequisicion().getPago().getFecha().compareTo(fecha)<=0){
					pagos=pagos.add(det.getTotalMN());
				}
			}
		}
		return pagos;
	}
	
	/**
	 * Regresa el saldo de la factura en moneda nacional
	 * 
	 * @return
	 */
	public CantidadMonetaria getSaldoCalculado(){
		if(getMoneda().equals(MonedasUtils.DOLARES)){
			System.out.println("DOL");
		}
		CantidadMonetaria tot=getTotalMN();
		CantidadMonetaria pag=getPagado();
		CantidadMonetaria dif=getDiferencia();
		return tot.subtract(pag.subtract(dif));
	}
	
	/**
	 * Regresa el saldo de la factura en moneda nacional
	 * 
	 * @return
	 */
	public CantidadMonetaria getSaldoCalculadoAlCorte(final Date fecha){
		CantidadMonetaria tot=getTotalMN();
		CantidadMonetaria pag=getPagadoAlCorte(fecha);
		CantidadMonetaria dif=getDiferencia();
		return tot.subtract(pag.subtract(dif));
	}
	
	/**
	 * Regresa el importe requisitado en moneda nacional
	 * 
	 * @return
	 */
	public CantidadMonetaria getRequisitado(){
		CantidadMonetaria pagos=CantidadMonetaria.pesos(0);
		for(RequisicionDe det:requisiciones){
			if(det.getRequisicion().getEstado().equals(Estado.SOLICITADA)
					|| det.getRequisicion().getEstado().equals(Estado.AUTORIZADA)){
				pagos=pagos.add(det.getTotalMN());
			}
		}
		return pagos;
		
	}
	
	/**
	 * Monto pendiente de requisitar en moneda nacional
	 * 
	 * @return
	 */
	public CantidadMonetaria getPorRequisitar(){
		CantidadMonetaria pagado=getPagado();
		CantidadMonetaria req=getRequisitado();
		//return getTotalMN().subtract(pagado).subtract(req);
		return getApagar().subtract(pagado).subtract(req);
	}
	
	public boolean puedeRequisitar(){
		return getPorRequisitar().amount().doubleValue()>0;
	}
	
	
	/**
	 * Regresa una lista separada por comas de las requisiciones asignadas a esta factura
	 * 
	 * @return
	 */
	public String getRequisicionesIds(){
		StringBuffer buf=new StringBuffer();
		for(RequisicionDe det:requisiciones){
			if(det.getId()!=null)
				buf.append(det.getId().toString());
			buf.append('(');
			buf.append(det.getRequisicion().getEstado().name());
			buf.append(')');
			buf.append(' ');
		}
		return buf.toString();
	}



	/**
	 * Diferencia  contable que afecta al saldo en moneda nacional
	 * 
	 * @return
	 */
	public CantidadMonetaria getDiferencia() {
		if(diferencia==null){
			diferencia=CantidadMonetaria.pesos(0);
		}
		return diferencia;
	}
	public void setDiferencia(CantidadMonetaria diferencia) {		
		this.diferencia = diferencia;
	}
	
	
	public void actualizarImportes(){
		getApagar();
		getDiferencia();
	}

	public boolean isActualizable(){
		for(RequisicionDe det:requisiciones){
			if(det.getRequisicion().getPago()!=null)
				return false;
		}
		return true;
	}
	
	public CantidadMonetaria getTotalMN(){
		CantidadMonetaria pesos=CantidadMonetaria.pesos(total.amount().doubleValue());
		return pesos.multiply(getTc());
	}
	
	public CantidadMonetaria getSaldoMN(){
		return getSaldoCalculado();
	}
	
	public void acutlizarFechaContable(){
		if(fecha.before(DateUtil.toDate("01/06/2008"))){
			//setFechaContable(fecha);
			fechaContable=fecha;
			return;
		}
		if(fecha!=null){
			boolean ok=ContaUtils.validarPeriodoDelSistema(fecha);
			if(ok){
				//setFechaContable(new Date());
				this.fechaContable=new Date();
			}else{
				//Verificar si la fecha es del mes anterior
				boolean anterior=ContaUtils.esMesAnterior(fecha);
				if(anterior){
					Date udia=ContaUtils.ultimoDiaDelPeriodoAnterior();
					//setFechaContable(udia);
					this.fechaContable=udia;
				}else
					//setFechaContable(fecha);
					this.fechaContable=fecha;
			}
		}
	}
	
	/**
	 * Hack para garantizar que el metodo actualizar fecha se ejectute desde la UI
	 * @return
	 */
	@AssertTrue
	public boolean validarFechaContable(){
		acutlizarFechaContable();
		return true;
	}


}

