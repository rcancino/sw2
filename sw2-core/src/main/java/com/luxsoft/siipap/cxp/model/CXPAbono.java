package com.luxsoft.siipap.cxp.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;


/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
public abstract class CXPAbono extends CXPCargoAbono{
	
	@OneToMany(cascade={
			CascadeType.ALL,
			 CascadeType.PERSIST,
			 CascadeType.MERGE,
			 CascadeType.REMOVE
			}
			,mappedBy="abono",fetch=FetchType.LAZY)	
	@Cascade(value=org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	private Set<CXPAplicacion> aplicaciones=new HashSet<CXPAplicacion>();
	
	@Formula("(select sum(X.IMPORTE) FROM SX_CXP_APLICACIONES X where X.ABONO_ID=CXP_ID)")
	private BigDecimal aplicado=BigDecimal.ZERO;
	
	public BigDecimal getAplicado(){
		if(aplicado==null)
			aplicado=BigDecimal.ZERO;
		return aplicado;
	}

	public BigDecimal getDisponible() {
		return getTotal().subtract(getAplicado());
	}

	public Set<CXPAplicacion> getAplicaciones() {
		return aplicaciones;
	}
	
	public boolean eliminarAplicacion(CXPAplicacion ap){
		return aplicaciones.remove(ap);
	}
	
	public boolean agregarAplicacion(CXPAplicacion ap){
		ap.setAbono(this);
		return aplicaciones.add(ap);
	}
	
	public abstract String getTipoId();
	
	public abstract String getInfo();
	
	public BigDecimal getAplicadoCalculado(){
		BigDecimal val=BigDecimal.ZERO;
		for(CXPAplicacion aplic:aplicaciones){
			val=val.add(aplic.getImporte());
		}
		return val;
	}

}
