package com.luxsoft.sw3.inventarios;

import java.util.Date;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.util.DateUtil;
//import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;



public class ArreglaAju {
	
	public void run(){
		Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
		Services.getInstance().getInventariosManager().generarAjusteDeInventario(sucursal, DateUtil.toDate("28/09/2013"));
	}
	
	public static void main(String[] args) throws Exception{
		//POSDBUtils.whereWeAre();
		new ArreglaAju().run();
	}

}
