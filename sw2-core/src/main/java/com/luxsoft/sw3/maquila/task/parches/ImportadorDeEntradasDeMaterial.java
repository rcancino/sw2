package com.luxsoft.sw3.maquila.task.parches;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.maquila.model.Almacen;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.Maquilador;

/**
 * Importa de Oracle las entradas de material
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImportadorDeEntradasDeMaterial implements RowMapper{
	
	public ImportadorDeEntradasDeMaterial importarRececiones(){
		String sql="select * from SW_RECEPCIONES_MAQUILA";
		List<EntradaDeMaterial> rows=ServiceLocator2.getAnalisisJdbcTemplate().query(sql, this);
		System.out.println("Entradas a importar: "+rows.size());
		int current=0;
		for(EntradaDeMaterial m:rows){
			agregarPartidas(m);
			ServiceLocator2.getUniversalDao().save(m);
			current++;
			System.out.println("Entrada importada: "+m+ ""+current+ "de "+rows.size());
		}
		return this;
	}

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		EntradaDeMaterial e=new EntradaDeMaterial();
		e.setId(rs.getLong("RECEPCION_ID"));
		e.setEntradaDeMaquilador(rs.getString("entradaDeMaquilador"));
		e.setFecha(rs.getDate("fecha"));		
		e.setObservaciones(rs.getString("OBSERVACIONES"));
		Almacen almacen=find(rs.getLong("ALMACEN_ID"));
		e.setAlmacen(almacen);
		return e;
	}
	
	public void importarEntradas(){
		List<EntradaDeMaterial> entradas=ServiceLocator2
		.getHibernateTemplate().find("from EntradaDeMaterial e left join fetch e.partidas");
		for(EntradaDeMaterial e:entradas){
			agregarPartidas(e);
			ServiceLocator2.getUniversalDao().save(e);
		
		}
	}
	
	private void agregarPartidas(EntradaDeMaterial e){
		System.out.println("Procesando entrada e:");
		String sql="select * from sw_movi_maquila_migracion where  RECEPCION_ID=?";
		List<EntradaDeMaterialDet> partidas=ServiceLocator2.getAnalisisJdbcTemplate()
		.query(sql, new Object[]{e.getId()}, new RowMapper(){

			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				EntradaDeMaterialDet det=new EntradaDeMaterialDet();
				
				det.setId(rs.getLong("MOVIMIENTO_ID"));
				det.setBobinas(rs.getInt("BOBINAS"));
				String clave=rs.getString("CLAVE");
				//Long prodId=rs.getLong("BOBINA_ID");
				Producto p=ServiceLocator2.getProductoManager().buscarPorClave(clave);
				det.setProducto(p);
				
				det.setEntradaDeMaquilador(rs.getString("ENTRADADEMAQUILADOR"));
				//det.setFabricante(rs.getString("fabricante"));
				//det.setFactura(rs.getString("factura"));
				det.setFecha(rs.getDate("fecha"));
				
				BigDecimal imp=rs.getBigDecimal("IMPORTE");
				det.setImporte(imp);
				det.setKilos(rs.getBigDecimal("KILOS"));
				det.setMetros2(rs.getBigDecimal("METROS2"));
				det.setObservaciones(rs.getString("observaciones"));
				det.setPrecioPorKilo(rs.getDouble("PKILO"));
				det.setPrecioPorM2(rs.getDouble("PRECIOM2"));
				
				return det;
			}
			
		});
		for(EntradaDeMaterialDet det:partidas){
			e.agregarEntrada(det);
		}
	}
	
	private static Almacen find(Long id){
		String hql="from Almacen a left join fetch a.maquilador m where a.id=?";
		List<Almacen> a=ServiceLocator2.getHibernateTemplate().find(hql,id);
		return a.isEmpty()?null:a.get(0);
	}
	
	public static void main(String[] args) {
		new ImportadorDeEntradasDeMaterial().importarRececiones();
	}

}
