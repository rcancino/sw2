package com.luxsoft.sw3.replica;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeVentasCredito extends ReplicadorDeVentas{
	
	public void importar(final Date fecha,Long sucursalId,HibernateTemplate sourceTemplate,HibernateTemplate targetTemplate){
		String sql="select cargo_id from sx_ventas where sucursal_id=? and fecha=? and origen=\'CRE\'";
		Object[] args={
				new SqlParameterValue(Types.NUMERIC,sucursalId)
				,new SqlParameterValue(Types.DATE,fecha)
		};
		List<String> res=ReplicaServices.getInstance().getJdbcTemplate(sucursalId).queryForList(sql, args, String.class);
		//super.importar(fecha, sucursalId, OrigenDeOperacion.CRE, sourceTemplate,targetTemplate);
		for(String id:res){
			System.out.println(id);
			importar(id, sucursalId, sourceTemplate, targetTemplate);
		}
	}
	
	public void importar(final String id
			,final Long sucursalId
			,final HibernateTemplate source
			,final HibernateTemplate target){
		String hql="from Venta v " +
		" left join fetch v.partidas p" +
		" left join fetch v.cliente c" +
		" where v.id=?"
		;
		List<Venta> ventas=source.find(hql, id);
		if(!ventas.isEmpty()){
			Venta v=ventas.get(0);
			v.setPedido(null);
			target.replicate(v, ReplicationMode.OVERWRITE);
		}
	}
	
	
	public static void main(String[] args) {
		ReplicadorDeVentasCredito replicador=new ReplicadorDeVentasCredito();
		//Date fecha=DateUtil.toDate("21/01/2010");
		Long sucurslId=2L;
		
		HibernateTemplate sourceTemplate=ReplicaServices.getInstance().getHibernateTemplate(sucurslId);
		HibernateTemplate targetTemplate=Services.getInstance().getHibernateTemplate();
		//replicador.importar("8a8a8584-26704c3d-0126-708aca9d-0005", sucurslId, sourceTemplate, targetTemplate);
		replicador.importar(DateUtil.toDate("01/03/2012"), sucurslId, sourceTemplate, targetTemplate);
		//replicador.importar(DateUtil.toDate("23/01/2010"), sucurslId, sourceTemplate, targetTemplate);
		
	}
		

}
