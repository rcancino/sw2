package com.luxsoft.siipap.cxc.parches;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Crear aplicaciones pendientes 
 * para de Pagos con nota sobre  Notas de Cargo que por limitaciones
 * tecnicas no se migraron en la carga inicial del nuevo modulo de cxc
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class Parche1_AjusteDeSaldoNotasDeCargo {
	
	public NotaDeCredito buscarAbono(final Long siipapId){
		String hql="from NotaDeCredito n left join fetch n.aplicaciones ap where n.siipapId=?";
		List<NotaDeCredito> res=ServiceLocator2.getHibernateTemplate().find(hql, siipapId);
		return res.isEmpty()?null:res.get(0);
	}
	
	public NotaDeCargo buscarCargo(final Long siipapId){
		String hql="from NotaDeCargo a where a.siipapWinId=?";
		List<NotaDeCargo> res=ServiceLocator2.getHibernateTemplate().find(hql, siipapId);
		return res.isEmpty()?null:res.get(0);
	}
	
	public void execute(){
		
		String sql="select nota_id,NOTAPAGO_ID ,importe,fecha ,pago_id from " +
				"sw_pagos where nota_id is not null " +
				" and NOTAPAGO_ID is not null " +
				" and nota_id in(select nota_id from sw_notas where fecha>? " +
				" and tipo=\'M\') ";
		SqlParameterValue p=new SqlParameterValue(Types.DATE,DateUtil.toDate("31/12/2007"));
		List<Map<String, Object>> rows=ServiceLocator2.getAnalisisJdbcTemplate().queryForList(sql,new Object[]{p});
		
		for(Map<String, Object> row:rows){
			
			System.out.println("Procesando: "+row);
			
			Number notaId=(Number)row.get("NOTA_ID");
			Number notaPagoId=(Number)row.get("NOTAPAGO_ID");
			BigDecimal importe=(BigDecimal)row.get("IMPORTE");
			Date fecha=(Date)row.get("FECHA");
			Number siipapid=(Number)row.get("PAGO_ID");
			
			NotaDeCargo cargo=buscarCargo(notaId.longValue());
			System.out.println("Cargo: "+cargo);
			if(cargo==null) 
				continue;
			
			NotaDeCredito nota=buscarAbono(notaPagoId.longValue());
			System.out.println("Abono encontrado: "+nota);
			if(nota!=null){
				if(nota.getDisponible().doubleValue()>=importe.doubleValue()){
					AplicacionDeNota an=new AplicacionDeNota();
					an.setCargo(cargo);
					an.setComentario("AJUSTER PARCHE1");
					an.setFecha(fecha);
					an.setImporte(importe);
					an.setSiipapId(siipapid.longValue());
					nota.agregarAplicacion(an);
					nota.actualizarDetalleEnAplicaciones();
					ServiceLocator2.getCXCManager().salvarNota(nota);
				}
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		new Parche1_AjusteDeSaldoNotasDeCargo().execute();
	}

}
