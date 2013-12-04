package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Type;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Cargo generico de un proveedor
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
public  abstract class CXPCargo extends CXPCargoAbono{
	
	@Column(name = "REVISION", nullable = true)
	@Type(type = "date")
	private Date revision=new Date();
	
	@Column(name = "VTO", nullable = true)
	@Type(type = "date")
	private Date vencimiento=new Date();
	
	@Column (name="FLETE",nullable=true)
	private BigDecimal flete=BigDecimal.ZERO;
	
	@Column (name="CARGOS",nullable=true)
	private BigDecimal cargos=BigDecimal.ZERO;
	
	@Column (name="FLETE_IVA",nullable=true)
	private BigDecimal impuestoflete=BigDecimal.ZERO;
	
	@Column (name="FLETE_RET",nullable=true)
	private BigDecimal retencionflete=BigDecimal.ZERO;
		
	@Column (name="DESCUENTOF",nullable=true)
	private double descuentoFinanciero=0;
	
	@Column (name="SALDO")
	protected BigDecimal saldo = BigDecimal.ZERO;
	
	
	@Column(name = "VTODF", nullable = true)
	@Type(type = "date")
	private Date vencimientoDF;
	
	@ManyToOne (optional=true,fetch=FetchType.EAGER,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn (name="AUT_ID", nullable=true, updatable=false)
	private AutorizacionDeCargoCXP autorizacion;
	
	@Formula(
			"(select IFNULL(sum(z.IMPORTE),0) from sx_cxp x  LEFT join sx_cxp_aplicaciones z on(x.CXP_ID=z.ABONO_ID)  where X.TIPO=\'NOTA\' and X.CONCEPTO_NOTA=\'BONIFICACION\' and z.CARGO_ID=CXP_ID)"
			)
	private BigDecimal bonificado=BigDecimal.ZERO;
	
	@Formula(
			"(select IFNULL(sum(z.IMPORTE),0) from sx_cxp x  LEFT join sx_cxp_aplicaciones z on(x.CXP_ID=z.ABONO_ID)  where X.TIPO=\'NOTA\' and X.CONCEPTO_NOTA=\'DEVLUCION\' and z.CARGO_ID=CXP_ID)"
			//"(select IFNULL(sum(X.IMPORTE),0) FROM SX_CXP_APLICACIONES X where X.CARGO_ID=CXP_ID AND X.TIPO_ABONO in (\'DEVLUCION\') )"
			)
	private BigDecimal devoluciones=BigDecimal.ZERO;
	
//	@Formula("(select IFNULL(sum(X.IMPORTE),0) FROM SX_CXP_APLICACIONES X where X.CARGO_ID=CXP_ID AND X.TIPO_ABONO=\'PAGO\')")
	@Formula("(select IFNULL(sum(X.IMPORTE),0) FROM SX_CXP_APLICACIONES X where X.CARGO_ID=CXP_ID)")
	private BigDecimal pagos=BigDecimal.ZERO;
	
	@Formula("(select IFNULL(sum(X.TOTAL),0) FROM SW_TREQUISICIONDET X where X.CXP_ID=CXP_ID)")
	private BigDecimal requisitado=BigDecimal.ZERO;
	
	@Formula("TOTAL-(select IFNULL(sum(X.IMPORTE),0) FROM SX_CXP_APLICACIONES X where X.CARGO_ID=CXP_ID)")
	private BigDecimal saldoReal=BigDecimal.ZERO;
	
	@OneToMany(mappedBy="cargo",fetch=FetchType.LAZY)
	private Set<CXPAplicacion> aplicaciones=new HashSet<CXPAplicacion>();
	
	public void setProveedor(Proveedor proveedor) {
		super.setProveedor(proveedor);
		if(proveedor!=null){
			setDescuentoFinanciero(proveedor.getDescuentoFinanciero());
			actualizarVencimiento();
			actualizarVencimientoDF();
		}
	}

		

	public Date getRevision() {
		return revision;
	}

	public void setRevision(Date revision) {
		this.revision = revision;
	}

	public Date getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(Date vencimiento) {
		Object old=this.vencimiento;
		this.vencimiento = vencimiento;
		firePropertyChange("vencimiento", old, vencimiento);
	}

	public BigDecimal getCargos() {
		return cargos;
	}
	public void setCargos(BigDecimal cargos) {
		Object old=this.cargos;
		this.cargos = cargos;
		firePropertyChange("cargos", old, cargos);
	}
	
	public CantidadMonetaria getTotalCargos(){
		return MonedasUtils.calcularTotal(new CantidadMonetaria(getCargos(),getMoneda()));
	}
	
	public CantidadMonetaria getCargosCM(){
		return new CantidadMonetaria(getCargos(),getMoneda());
	}
	
	public CantidadMonetaria getFleteMN(){
		double res=getFlete().doubleValue()*getTc();
		return CantidadMonetaria.pesos(res);
	}

	public BigDecimal getFlete() {
		if(flete==null) flete=BigDecimal.ZERO;
		return flete;
	}

	public void setFlete(BigDecimal flete) {
		Object old=this.flete;
		this.flete = flete;
		actualizarImportesDelFlete();
		firePropertyChange("flete", old, flete);
		
	}
	
	public CantidadMonetaria getTotalFlete(){
		return new CantidadMonetaria(getFlete(),getMoneda());
	}
	
	public CantidadMonetaria getImpuestoFleteMN(){
		double res=getImpuestoflete().doubleValue();
		return CantidadMonetaria.pesos(res*getTc());
	}

	public BigDecimal getImpuestoflete() {
		if(impuestoflete==null) impuestoflete=BigDecimal.ZERO;
		return impuestoflete;
	}

	public void setImpuestoflete(BigDecimal impuestoflete) {
		Object old=this.impuestoflete;
		this.impuestoflete = impuestoflete;
		firePropertyChange("impuestoflete", old, impuestoflete);
	}
	
	public CantidadMonetaria getRetencionFleteMN(){
		double res=getRetencionflete().doubleValue();
		return CantidadMonetaria.pesos(res*getTc());
	}

	public BigDecimal getRetencionflete() {
		return retencionflete;
	}

	public void setRetencionflete(BigDecimal retencionflete) {
		Object old=this.retencionflete;
		this.retencionflete = retencionflete;
		firePropertyChange("retencionflete", old, retencionflete);
	}

	public double getDescuentoFinanciero() {
		return descuentoFinanciero;
	}

	public void setDescuentoFinanciero(double descuentoFinanciero) {
		double old=this.descuentoFinanciero;
		this.descuentoFinanciero = descuentoFinanciero;
		firePropertyChange("descuentoFinanciero", old, descuentoFinanciero);
	}
	
	public CantidadMonetaria getImporteDescuentoFinanciero(){
		if(getDescuentoFinanciero()==0)
			return new CantidadMonetaria(0,getMoneda());
		CantidadMonetaria totalBase=getTotalAnalisis();
		CantidadMonetaria cargos=getTotalCargos();
		totalBase=totalBase.add(getTotalFlete()).add(cargos);
		
		return totalBase.multiply(getDescuentoFinanciero()/100);
	}
	
	public BigDecimal getBonificado() {
		return bonificado;
	}
	
	public CantidadMonetaria getBonificadoCM(){
		return new CantidadMonetaria(getBonificado(),getMoneda());
	}
	
	public CantidadMonetaria getDevolucionesCM(){
		return new CantidadMonetaria(getDevoluciones(),getMoneda());
	}
	
	public BigDecimal getPagos() {
		if(pagos==null)
			pagos=BigDecimal.ZERO;
		return pagos;
	}	
	
	public BigDecimal getSaldoCalculado(){
		return getTotal()
			.subtract(getPagos())
		//	.subtract(getBonificado())
			.subtract(getDiferencia())
		//	.subtract(getDevoluciones())
		//	.subtract(new BigDecimal(getDescuentoFinanciero()))
			;
	}
	

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}
	public BigDecimal getSaldo() {
		return saldo;
	}

	
	public Date getVencimientoDF() {
		return vencimientoDF;
	}

	public void setVencimientoDF(Date vencimientoDF) {
		Object old=this.vencimientoDF;
		this.vencimientoDF = vencimientoDF;
		firePropertyChange("vencimientoDF", old, vencimientoDF);
	}
	
	

	public AutorizacionDeCargoCXP getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(AutorizacionDeCargoCXP autorizacion) {
		this.autorizacion = autorizacion;
	}
	
	public Set<CXPAplicacion> getAplicaciones() {
		return aplicaciones;
	}
		

	public BigDecimal getRequisitado() {
		if(requisitado==null)requisitado=BigDecimal.ZERO;
		return requisitado;
	}
	
	public int getAtraso(){
		if(getSaldoCalculado().doubleValue()<=0)
			return 0;
		Date today=new Date();		
		long res=today.getTime()-getVencimiento().getTime();
		if(res>0){				
			long dias=(res/(86400*1000));			
			return (int)dias;
		}else{
			return 0;
		}
	}

	
	/**
	 * Actualiza el vencimiento en funcion de los dias de credito
	 * del proveedor
	 * 
	 */
	public void actualizarVencimiento(){
		if(getProveedor()==null)
			throw new IllegalStateException("No se ha definido el proveedor de la factura");		
		CXPUtils.calcularVencimiento(new Date(), getFecha(),getProveedor());
	}
	
	public void actualizarVencimientoDF(){
		if(getProveedor()==null)
			throw new IllegalStateException("No se ha definido el proveedor de la factura");
		CXPUtils.calcularVencimientoDescuentoF(new Date(), getFecha(),getProveedor());
	}
		
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.appendSuper(super.toString())
		.append(getSaldo())
		.append(this.vencimiento)
		.toString();
	}
	
	@Transient
	private double retencionfletePor=4;

	public double getRetencionfletePor() {
		return retencionfletePor;
	}

	public void setRetencionfletePor(double retencionfletePor) {
		this.retencionfletePor = retencionfletePor;
	}
	
	private void actualizarImportesDelFlete(){
		double por=retencionfletePor/100d;
		CantidadMonetaria flete=CantidadMonetaria.pesos(getFlete().doubleValue());
		setRetencionflete(flete.multiply(por).amount());
		setImpuestoflete(MonedasUtils.calcularImpuesto(flete).amount());
	}
	
	
	public CantidadMonetaria getPorRequisitar(){
		return getPorRequisitar(true);
	}
	
	public CantidadMonetaria getPorRequisitar(boolean conDescuentoFinanciero){
		
		CantidadMonetaria pendiente=new CantidadMonetaria(getSaldo(),getMoneda());
		
		if(pendiente.amount().doubleValue()<0)
			pendiente=new CantidadMonetaria(0,getMoneda());
		return pendiente;
		
	}
	
	public CantidadMonetaria getPorRequisitarSimple(boolean conDescuentoFinanciero){
		return getPorRequisitar(conDescuentoFinanciero);
	}
	
	public BigDecimal getSaldoReal() {
		return saldoReal;
	}
	
	public CantidadMonetaria getTotalAnalisis(){
		return getTotalCM();
	}
	
	public BigDecimal getTotalAnalizadoConFlete(){
		return BigDecimal.ZERO;
	}



	public BigDecimal getDevoluciones() {
		return devoluciones;
	}



	public void setDevoluciones(BigDecimal devoluciones) {
		this.devoluciones = devoluciones;
	}

	
}
