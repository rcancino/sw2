package com.luxsoft.sw3.impap.services;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.service.ServiceLocator2;

public class ImportadorDeCompras {
	
	public Compra2 importarCompra(Long folio){
		JdbcTemplate template=new JdbcTemplate(DataSourceUtils.getInstance().getPapelDataSource());
		String sqlMaestro="select * from SX_COMPRAS2 where SUCURSAL_ID=1 and FOLIO=?";
		List<Map<String, Object>> maestros=template.queryForList(sqlMaestro, new Object[]{folio});
		if(maestros.isEmpty())
			throw new RuntimeException("No existe la compra: "+folio);
		Map<String, Object> maestro=maestros.get(0);
		
		
		System.out.println("Compra: "+maestro);
		String sqlPartidas="select COMPRADET_ID,CLAVE,COMENTARIO,COSTO,DEPURACION,DEPURADO,DESC1,DESC2,DESC3,DESC4,DESC5,DESC6,DESCRIPCION,DESCF,FACTOR,IMPORTE_BRUTO,IMPORTE_DESC,IMPORTE_NETO,PRECIO,SOLICITADO,SUC_NAME,UNIDAD,version,SUCURSAL_ID,PRODUCTO_ID,COMPRA_ID,FOLIO_ORIGEN,ADUANA,REGISTRO_ADUANA,RECIBIDO_GLOBAL from sx_compras2_det where COMPRA_ID=?";
		List<Map<String,Object>> partidas=template.queryForList(sqlPartidas,new Object[]{maestro.get("COMPRA_ID")});
		System.out.println("Partidas: "+partidas.size());
		String INSERT="INSERT INTO SX_COMPRAS2 (";
		String VALUES="(";
		Iterator<Map.Entry<String, Object>> entryIter=maestro.entrySet().iterator();
		while(entryIter.hasNext()){
			Entry<String,Object> entry=entryIter.next();
			String campo=entry.getKey();
			INSERT+=campo;
			VALUES+="?";
			if(entryIter.hasNext()){
				INSERT+=",";
				VALUES+=",";
			}
		}
		INSERT+=")";
		VALUES+=")";
		entryIter=maestro.entrySet().iterator();
		Object[] params=new Object[maestro.keySet().size()];
		int index=0;
		while(entryIter.hasNext()){
			Entry<String,Object> entry=entryIter.next();
			params[index++]=entry.getValue();	
		}
		
		System.out.println(INSERT);
		System.out.println(VALUES);
		System.out.println(ArrayUtils.toString(params));
		
		ServiceLocator2.getJdbcTemplate().update(INSERT+" VALUES "+VALUES,params);
		for(Map<String,Object> row:partidas){
			INSERT="INSERT INTO SX_COMPRAS2_DET (";
			VALUES="(";
			entryIter=row.entrySet().iterator();
			while(entryIter.hasNext()){
				Entry<String,Object> entry=entryIter.next();
				String campo=entry.getKey();				
				INSERT+=campo;
				VALUES+="?";
				if(entryIter.hasNext()){
					INSERT+=",";
					VALUES+=",";
				}
			}
			INSERT+=")";
			VALUES+=")";
			entryIter=row.entrySet().iterator();
			params=new Object[row.keySet().size()];
			index=0;
			while(entryIter.hasNext()){
				Entry<String,Object> entry=entryIter.next();				
				params[index++]=entry.getValue();	
			}
			
			System.out.println(INSERT);
			System.out.println(VALUES);
			System.out.println(ArrayUtils.toString(params));
			ServiceLocator2.getJdbcTemplate().update(INSERT+" VALUES "+VALUES,params);
		}
		try {
			DataSourceUtils.getInstance().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Compra2 res=ServiceLocator2.getComprasManager().buscarInicializada((String)maestro.get("COMPRA_ID"));
		return res;
	}
	
	
	public static void main(String[] args) {
		new ImportadorDeCompras().importarCompra(86L);
	}

}
