package com.luxsoft.sw3.tasks;

import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.sw3.inventarios.TipoDeMovimiento;
import com.luxsoft.sw3.services.Services;

public class GenerarTiposDeMovimientos {
	
	
	public static void execute(){
		for(Movimiento.Concepto c:Movimiento.Concepto.values()){
			TipoDeMovimiento tipo=new TipoDeMovimiento();
			tipo.setConcepto(c.name());
			tipo.setDescripcion(c.toString());
			tipo.setMaximoSinAutorizacion(-1);
			Services.getInstance().getUniversalDao().save(tipo);
		}
	}
	
	
	public static void main(String[] args) {
		execute();
	}

}
