package com.luxsoft.cfdi.tasks;

import org.springframework.util.Assert;

import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.services.Services;

public class CancelarCFDI {
	
	
	public void cancelar(String cfdId)throws Exception{
		Empresa empresa=Services.getInstance().getEmpresa();
		CFDI cfdi=Services.getCFDIManager().getCFDI(cfdId);
		Assert.hasLength(cfdi.getUUID());
		Services.getCFDIManager().cancelar(empresa, cfdi.getUUID());
	}
	
	public static void main(String[] args) throws Exception{
		new CancelarCFDI().cancelar("8a8a8199-430243fe-0143-025a7741-0016");
	}

}
