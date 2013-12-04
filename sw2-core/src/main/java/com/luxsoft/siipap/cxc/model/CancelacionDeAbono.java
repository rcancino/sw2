package com.luxsoft.siipap.cxc.model;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.core.Cancelacion;

/**
 * Registro de abonos cancelados
 * 
 * @author Ruben Cancino
 *
 */
@Entity
@Table(name="SX_CXC_ABONOS_CANCELADOS")
public class CancelacionDeAbono extends Cancelacion{
	
	
	@ManyToOne(optional = false,cascade={CascadeType.MERGE,CascadeType.PERSIST})
	@JoinColumn(name = "ABONO_ID", nullable = false, updatable = false,unique=true)
	private Abono abono;

	public CancelacionDeAbono(){}
	
	public CancelacionDeAbono(Abono a){
		this.abono=a;
		setComentario("ABONO CANCELADO");
		setImporte(a.getTotal());
		setMoneda(a.getMoneda());
	}

	@Override
	public String getInfo() {
		return MessageFormat.format("CANCELACION DE ABONO: {0}",getAbono().getInfo());
	}
	
	@ManyToOne(optional = true,cascade=CascadeType.ALL)
	@JoinColumn(name = "AUT_ID", nullable = true)
	private Autorizacion2 autorizacion;

	public Autorizacion2 getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(Autorizacion2 autorizacion) {
		this.autorizacion = autorizacion;
	}
	
	public Abono getAbono() {
		return abono;
	}

	public void setAbono(Abono abono) {
		this.abono = abono;
		setComentario("CANCELACION DE PAGO: "+abono.getInfo());
		setImporte(abono.getTotal());
		abono.setTotal(BigDecimal.ZERO);
		abono.setImporte(BigDecimal.ZERO);
		String msg="CANCELADO "+abono.getComentario();
		abono.setComentario(StringUtils.substring(msg, 0,250));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abono == null) ? 0 : abono.hashCode());
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
		CancelacionDeAbono other = (CancelacionDeAbono) obj;
		if (abono == null) {
			if (other.abono != null)
				return false;
		} else if (!abono.equals(other.abono))
			return false;
		return true;
	}
	
	

}
