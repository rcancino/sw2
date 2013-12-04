package com.luxsoft.siipap.cxp.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ANTICIPO")
public class CXPAnticipo extends CXPPago{
	
	@Override
	public String getInfo() {
		if(getRequisicion()!=null)
			return "Req :"+getRequisicion().getId();
		return "SIN REQ";
	}

	@Override
	public String getTipoId() {
		return "ANTICIPO";
	}

}
