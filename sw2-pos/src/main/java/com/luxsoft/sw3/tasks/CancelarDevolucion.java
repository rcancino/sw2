package com.luxsoft.sw3.tasks;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.ReplicationMode;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;

public class CancelarDevolucion {
	

	public static void cancelar(Long documento,Long sucursalId) {
		
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		Devolucion devo=buscar(documento, sucursalId, source);
		devo.setComentario("CANCELACION AUTORIZADA");
		devo.setTotal(BigDecimal.ZERO);
		devo.setImporte(BigDecimal.ZERO);
		devo.setImpuesto(BigDecimal.ZERO);
		devo.getPartidas().clear();
		devo=(Devolucion)source.merge(devo);		
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		target.replicate(devo, ReplicationMode.OVERWRITE);
		
		// FIX POR FALLA EN REPLICA AL ELIMINAR PARTIDAS
		Devolucion devoDest=buscar(documento, sucursalId, target);		
		devoDest.getPartidas().clear();
		target.saveOrUpdate(devoDest);
		
		System.out.println("Devo cancelado: "+devo.getId());
		if(!devo.getVenta().getOrigen().equals(OrigenDeOperacion.CRE)){
			System.out.println("NO OLVIDE ELIMINAR EL ABONO CORRESPONDIENTE EL RMD");
		}
		
	}
	
	private static Devolucion buscar(Long documento,Long sucursalId,HibernateTemplate template) {
		String hql="from Devolucion d " +
		//" left join d.venta v " +
		" left join fetch d.partidas " +
		" where d.venta.sucursal.id=? " +
		"   and d.numero=?";
		List<Devolucion> devos=template.find(hql,new Object[]{sucursalId,documento});
		return devos.get(0);
	}
	
	
	public static void main(String[] args) {
		cancelar(38L, 2L);
	}

	

}
