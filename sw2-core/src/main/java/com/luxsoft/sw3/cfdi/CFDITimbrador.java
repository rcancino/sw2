package com.luxsoft.sw3.cfdi;

import org.springframework.stereotype.Service;

import com.luxsoft.sw3.cfdi.model.CFDI;


@Service("cfdiTimbrador")
public class CFDITimbrador {
	
	
	public CFDI timbrar(CFDI cfdi){
		return cfdi;
	}

}
