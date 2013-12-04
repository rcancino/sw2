package com.luxsoft.sw3.bi.inventarios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.luxsoft.siipap.service.ServiceLocator2;

import ca.odell.glazedlists.EventList;



public class ComparativoModel {
	
	
	
	
	@SuppressWarnings("unchecked")
	public void cargarCostosPorArticulo(EventList<CostosPorArticuloRow> articulos){
		try{
		//final TransformedList list=GlazedListsSwing.swingThreadProxyList(articulos);
		String sql="select d.clave,d.descripcion1,c.clave as familia,c.descripcion as familiaNombre,d.kilos ,a.periodo,nvl(saldo,0) as saldo ,nvl(a.costo,0) as costop,nvl(e.costo,0) as costou "+
					" from sw_promedios a"+
					" join sw_articulos d on(d.id=a.articulo_id)"+
					" join sw_familias c on(d.familia_id=c.id)"+
					" left join SW_COSTOULTIMO e on(a.articulo_id=e.articulo_id and a.periodo=e.periodo)"+
					" left join v_saldos f on(d.clave=f.clave and f.periodo=a.periodo)"+
					//" where saldo>0 and rownum<5000"+
					" order by d.clave";
		//String sql="select a.clave,a.descripcion1,a.kilos ,b.periodo,b.saldo from sw_articulos a join v_saldos b on(a.clave=b.clave) where rownum<100";
		List<Map> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql);
		for(Map row:rows){
			String clave=(String)row.get("clave");
			String desc=(String)row.get("descripcion1");
			//String fam=(String)row.get("familia");
			//String famName=(String)row.get("familiaNombre");
			BigDecimal kilos=(BigDecimal)row.get("kilos");
			String per=(String)row.get("periodo");			
			BigDecimal exis=(BigDecimal)row.get("saldo");
			BigDecimal prom=(BigDecimal)row.get("costop");
			BigDecimal ult=(BigDecimal)row.get("costou");
			CostosPorArticuloRow rr=new CostosPorArticuloRow(clave.trim(),desc.trim());
			rr.setKilos(kilos);
			rr.setPeriodo(per);
			//rr.setFamilia(fam);
			//rr.setFamiliaDesc(famName);
			rr.setExistencia(exis);
			rr.setCostoPromedio(prom);
			rr.setCostoUltimo(ult);
			//list.add(rr);
			articulos.getReadWriteLock().writeLock().lock();
			try{
				System.out.println("Cargando: "+rr);
				articulos.add(rr);
			}finally{
				articulos.getReadWriteLock().writeLock().unlock();
			}
			
			
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	
	

}
