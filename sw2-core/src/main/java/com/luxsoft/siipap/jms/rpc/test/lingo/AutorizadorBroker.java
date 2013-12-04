package com.luxsoft.siipap.jms.rpc.test.lingo;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.cxc.model.Abono;

public class AutorizadorBroker implements AutorizacionesCxC{
	
	Logger logger=Logger.getLogger(getClass());

	public void autorizarAbonoParaAplicar(Abono a) {
		System.out.println("Solicitando autorizacion para abono: "+a);
		
	}

}
