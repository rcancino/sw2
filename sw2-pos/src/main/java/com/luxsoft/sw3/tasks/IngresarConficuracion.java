package com.luxsoft.sw3.tasks;

import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.sw3.services.Services;

public class IngresarConficuracion {
	
	public void execute(long sucursalId){
		final Sucursal sucursal=(Sucursal)Services.getInstance().getUniversalDao().get(Sucursal.class, sucursalId);
		final Cuenta cuenta=(Cuenta)Services.getInstance().getUniversalDao().get(Cuenta.class, new Long(151228));
		Configuracion c=new Configuracion();
		c.setSucursal(sucursal);
		c.setCuentaPreferencial(cuenta);
		Services.getInstance().getUniversalDao().save(c);
	}
	
	public static void main(String[] args) {
		new IngresarConficuracion().execute(3);
	}

}
