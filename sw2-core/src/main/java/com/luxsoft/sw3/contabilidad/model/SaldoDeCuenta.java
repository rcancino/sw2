package com.luxsoft.sw3.contabilidad.model;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

@Entity
@Table (name="SX_CONTABILIDAD_SALDOS")
public class SaldoDeCuenta {
	
	@Id @GeneratedValue (strategy=GenerationType.AUTO)
	@Column (name="SALDO_ID")
	private Long id;
	
	@Version
	private int version;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="CUENTA_ID", nullable=false) 
    @NotNull
	private CuentaContable cuenta;
	
	@Column(name="YEAR",nullable=false)
    @NotNull
	private int year;
	
	@Column(name="MES",nullable=false)
	@NotNull 
	@Range(min=1,max=13)
	private int mes;	
	
	@Column (name="DEBE",nullable=false,scale=6,precision=16)
	private BigDecimal debe=BigDecimal.ZERO;
	
	@Column (name="HABER",nullable=false,scale=6,precision=16)
	private BigDecimal haber=BigDecimal.ZERO;
	
	@Column (name="SALDO_INICIAL",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal saldoInicial=BigDecimal.ZERO;
	
	@Column (name="SALDO_FINAL",nullable=false,scale=6,precision=16)
	@NotNull
	private BigDecimal saldoFinal=BigDecimal.ZERO;
	
	@Column(name="CIERRE",nullable=true)
	private Date cierre;
	
	@OneToMany(cascade={
			 CascadeType.PERSIST
			,CascadeType.MERGE
			,CascadeType.REMOVE
			}
			,fetch=FetchType.LAZY,mappedBy="saldo")	
	@Cascade(value={org.hibernate.annotations.CascadeType.DELETE_ORPHAN
			,org.hibernate.annotations.CascadeType.REPLICATE})
	private Set<SaldoDeCuentaPorConcepto> conceptos=new HashSet<SaldoDeCuentaPorConcepto>(); 
	
	
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

	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {
		this.cuenta = cuenta;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public BigDecimal getDebe() {
		if(debe==null)
			debe=BigDecimal.ZERO;
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		this.debe = debe;
	}

	public BigDecimal getHaber() {
		if(haber==null)
			haber=BigDecimal.ZERO;
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		this.haber = haber;
	}

	public BigDecimal getSaldoInicial() {
		return saldoInicial;
	}

	public void setSaldoInicial(BigDecimal saldoInicial) {
		this.saldoInicial = saldoInicial;
	}

	public BigDecimal getSaldoFinal() {
		return saldoFinal;
	}

	public void setSaldoFinal(BigDecimal saldoFinal) {
		this.saldoFinal = saldoFinal;
	}

	public Date getCierre() {
		return cierre;
	}

	public void setCierre(Date cierre) {
		this.cierre = cierre;
	}

	

	public Set<SaldoDeCuentaPorConcepto> getConceptos() {
		return conceptos;
	}

	public void setConceptos(Set<SaldoDeCuentaPorConcepto> conceptos) {
		this.conceptos = conceptos;
	}
	
	/**
	 * Agrega un saldo para este  concepto si este ya existe  lo regresa
	 * 
	 * @param concepto
	 * @return
	 */
	public SaldoDeCuentaPorConcepto addConcepto(final ConceptoContable concepto){
		SaldoDeCuentaPorConcepto det=getSaldoPorConcepto(concepto);
		if(det==null){
			det=new SaldoDeCuentaPorConcepto();
			det.setConcepto(concepto);
			det.setYear(getYear());
			det.setMes(getMes());
			det.setSaldo(this);
			conceptos.add(det);
			return det;
		}else
			return det;
		
	}
	
	public SaldoDeCuentaPorConcepto addConcepto(SaldoDeCuentaPorConcepto c){
		c.setSaldo(this);
		conceptos.add(c);
		return c;
	}
	public void remove(SaldoDeCuentaPorConcepto c){
		c.setSaldo(null);
		conceptos.remove(c);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cuenta == null) ? 0 : cuenta.hashCode());
		result = prime * result + mes;
		result = prime * result + year;
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
		SaldoDeCuenta other = (SaldoDeCuenta) obj;
		if (cuenta == null) {
			if (other.cuenta != null)
				return false;
		} else if (!cuenta.equals(other.cuenta))
			return false;
		if (mes != other.mes)
			return false;
		if (year != other.year)
			return false;
		return true;
	}
		

	public String toString(){
		String pattern="Cta: {0}  Per:{1}/{2} Ini: {3} Cargos:{4} Abonos:{5} Final:{6}";
		return MessageFormat.format(pattern, getCuenta().getClave(),getYear(),getMes(),getSaldoInicial(),getDebe(),getHaber(),getSaldoFinal());
	}
	
	public void actualizar(){
		BigDecimal saldoInicial=BigDecimal.ZERO;
		BigDecimal debe=BigDecimal.ZERO;
		BigDecimal haber=BigDecimal.ZERO;
		
		for(SaldoDeCuentaPorConcepto det:conceptos){
			det.actualizar();
			saldoInicial=saldoInicial.add(det.getSaldoInicial());
			haber=haber.add(det.getHaber());
			debe=debe.add(det.getDebe());
			
		}
		setSaldoInicial(saldoInicial);
		setDebe(debe);
		setHaber(haber);
		BigDecimal movimientos=getDebe().subtract(getHaber());
		setSaldoFinal(getSaldoInicial().add(movimientos));
	}
	
	public void limpiar(){
		for(SaldoDeCuentaPorConcepto sc:getConceptos()){
			sc.setSaldo(null);
		}
		getConceptos().clear();
	}

	public SaldoDeCuentaPorConcepto getSaldoPorConcepto(final ConceptoContable concepto) {
		return (SaldoDeCuentaPorConcepto)CollectionUtils.find(getConceptos(), new Predicate() {			
			public boolean evaluate(Object object) {
				SaldoDeCuentaPorConcepto ss=(SaldoDeCuentaPorConcepto)object;
				return ss.getConcepto().getClave().equals(concepto.getClave());
			}
		});
	}
	
}
