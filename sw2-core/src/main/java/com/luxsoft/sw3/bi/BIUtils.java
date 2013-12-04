package com.luxsoft.sw3.bi;

import com.luxsoft.siipap.util.SQLUtils;

public class BIUtils {
	
	
	public static void main(String[] args) {
		String sql="select a.*, b.nombre as sucursalNombre,x.nombre as nombre from bi_ventas a join sx_clientes x on(a.cliente_id=x.cliente_id) join sw_sucursales b on(a.sucursal_id=b.sucursal_id)";
		//SQLUtils.printBeanClasFromSQL(sql);
		SQLUtils.printBeanPropertiesSQL(sql,true);
	}

}
