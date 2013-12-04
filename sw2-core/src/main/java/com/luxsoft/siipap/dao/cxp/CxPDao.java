package com.luxsoft.siipap.dao.cxp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.cxp.CxPOldSupport;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * DAO para leer registros de la tabla SW_CXP
 * 
 * @author Ruben Cancino
 *
 */
public class CxPDao {
	
	private static CxPMappingQuery query;
	
	@SuppressWarnings("unchecked")
	public static List<CxPOldSupport>  buscarPagos(final Date fecha){
		if(query==null)
			query=new CxPMappingQuery(ServiceLocator2.getDataSource());
		return query.execute(new Object[]{fecha});
	}
	
	public static List<CxPOldSupport.CxPGrupo> buscarGruposDePagos(final Date fecha){
		final List<CxPOldSupport> rows=buscarPagos(fecha);
		final EventList<CxPOldSupport> source=GlazedLists.eventList(rows);
		final GroupingList<CxPOldSupport> grupos=new GroupingList<CxPOldSupport>(source,GlazedLists.beanPropertyComparator(CxPOldSupport.class, "pagoRef"));
		
		final List<CxPOldSupport.CxPGrupo> res=new ArrayList<CxPOldSupport.CxPGrupo>();
		
		for(int index=0;index<grupos.size();index++){
			List<CxPOldSupport> pago=grupos.get(index);
			CxPOldSupport.CxPGrupo gpo=new CxPOldSupport.CxPGrupo(pago);
			res.add(gpo);
		}
		return res;
	}
	
	public static  class CxPMappingQuery extends MappingSqlQuery{
		
		static String sql="SELECT a.CXP_ID,A.TIPO,A.PROVEEDOR_ID,A.CLAVE,C.NOMBRE,C.CUENTACONTABLE,A.FECHA,A.REFERENCIA,B.FECHA AS FECHAF" +
				" ,A.TOTALMN,A.COMENTARIO " +
				"FROM SW_CXP a " +
				"JOIN SW_CXP B ON(A.FACTURA_ID=B.CXP_ID) " +
				"JOIN SW_PROVEEDORES C ON(A.PROVEEDOR_ID=C.ID) " +
				"WHERE a.tipo=\'PAG\' " +
				"  and a.fecha=?" +
				" order by a.referencia asc";

		public CxPMappingQuery(DataSource ds) {
			super(ds, sql);
			super.declareParameter(new SqlParameter("",Types.DATE));
			compile();
			
		}

		
		@Override
		protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			CxPOldSupport c=new CxPOldSupport();
			c.setId(rs.getLong("CXP_ID"));
			c.setTipo(rs.getString("TIPO"));
			c.setProveedorId(rs.getLong("PROVEEDOR_ID"));
			c.setClave(rs.getString("CLAVE"));
			c.setNombre(rs.getString("NOMBRE"));
			c.setCuenta(rs.getString("CUENTACONTABLE"));
			c.setFecha(rs.getDate("FECHA"));
			c.setReferencia(rs.getString("REFERENCIA"));
			c.setFechaF(rs.getDate("FECHAF"));
			c.setTotal(rs.getBigDecimal("TOTALMN"));
			c.setComentario(rs.getString("COMENTARIO"));
			return c;
		}
		
		
		
	}

}
