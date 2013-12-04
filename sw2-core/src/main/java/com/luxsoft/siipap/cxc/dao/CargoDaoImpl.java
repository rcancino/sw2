package com.luxsoft.siipap.cxc.dao;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;

public class CargoDaoImpl extends GenericDaoHibernate<Cargo, String> implements CargoDao{

	public CargoDaoImpl() {
		super(Cargo.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.cxc.dao.CargoDao#actualizarSaldo(java.lang.String)
	 
	public Cargo actualizarSaldo(final String id) {
		return (Cargo) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Cargo c=(Cargo)session.load(Cargo.class, id);
				BigDecimal aplicado=getAplicado(c);
				BigDecimal sal=c.getTotal().subtract(aplicado);
				c.setSaldo(sal);
				return session.merge(c);
			}			
		});
		
	}	
	
	*//**
	 * El monto aplicado al cargo
	 * 
	 * @param c
	 * @return
	 *//*
	public BigDecimal getAplicado(final Cargo c){
		String hql="select sum(x.importe) from Aplicacion x where x.cargo.id=?";
		List<BigDecimal> aplicado=getHibernateTemplate().find(hql, c.getId());
		if(aplicado.isEmpty())
			return BigDecimal.ZERO;
		else 
			return aplicado.get(0);
	}*/

}
