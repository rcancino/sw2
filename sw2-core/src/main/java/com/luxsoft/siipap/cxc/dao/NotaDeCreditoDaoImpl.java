package com.luxsoft.siipap.cxc.dao;

import java.util.List;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;

public class NotaDeCreditoDaoImpl extends GenericDaoHibernate<NotaDeCredito, String> implements NotaDeCreditoDao{

	public NotaDeCreditoDaoImpl() {
		super(NotaDeCredito.class);
		
	}

	@Override
	public NotaDeCredito save(NotaDeCredito nota) {
		//nota.acutalizarDisponible();
		//nota.actualizarDetalleEnAplicaciones();
		for(Aplicacion ap:nota.getAplicaciones()){
			ap.actualizarDetalle();
			/*BigDecimal saldo=ap.getCargo().getSaldo();
			saldo=saldo.subtract(ap.getImporte());
			if(saldo.doubleValue()<0)
				saldo=BigDecimal.ZERO;
			ap.getCargo().setSaldo(saldo);*/
			
		}
		return super.save(nota);
	}

	public NotaDeCredito buscarPorSiipapId(Long id) {
		String hql="from NotaDeCredito n left join fetch n.aplicaciones where n.siipapId=?";
		List<NotaDeCredito> data=getHibernateTemplate().find(hql, id);
		return data.isEmpty()?null:data.get(0);
 	}
	
	

}
