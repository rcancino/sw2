package com.luxsoft.sw3.cfdi;

import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.sw3.cfdi.model.CFDI;

public interface INotaDeCredito {
	
	public CFDI generar(NotaDeCredito nota);

}
