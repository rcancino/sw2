package com.luxsoft.siipap.cxp.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Formula;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.tesoreria.Requisicion;



/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Entity
@DiscriminatorValue("PAGO")
public class CXPPago extends CXPAbono{
	
	
	@OneToOne
	@JoinColumn(name="REQUISICION_ID")
	private Requisicion requisicion;
	
/*	@Formula
	//("(select MAX(X.CAR_ORIGEN) FROM SX_CXC_APLICACIONES X  where X.ABONO_ID=ABONO_ID AND X.FECHA=SAF )") 
	("(SELECT X.ABONO_ID FROM SX_CXP_APLICACIONES X WHERE X.ABONO_ID=CXP_ID AND X.TIPO_ABONO='PAGO') ")
	private CXPPago cxpApl;*/

	public Requisicion getRequisicion() {
		return requisicion;
	}

	public void setRequisicion(Requisicion requisicion) {
		this.requisicion = requisicion;
		setDocumento(requisicion.getId().toString());
		setComentario("Pago automatico por requisicion");
		setTotal(requisicion.getTotal().amount());
		
	}

	@Override
	public String getInfo() {
		if(getRequisicion()!=null)
			return "Req :"+getRequisicion().getId();
		return "SIN REQ";
	}

	@Override
	public String getTipoId() {
		return "PAGO";
	}
	
	public String toString(){
		return new ToStringBuilder(this,ToStringStyle.SIMPLE_STYLE)
		.append("CXP_ID:",getId())
		.appendSuper(super.toString())
		.toString();
	}
	
/*	public CXPPago getcxpApl() {
		if(cxpApl==null)
			return null;
		return cxpApl;
	}*/
	

	
	

}
