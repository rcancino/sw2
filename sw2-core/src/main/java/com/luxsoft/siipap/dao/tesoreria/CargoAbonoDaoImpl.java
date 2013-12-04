package com.luxsoft.siipap.dao.tesoreria;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.model.tesoreria.EstadoDeCuenta;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.KernellSecurity;

@SuppressWarnings("unchecked")
public class CargoAbonoDaoImpl extends GenericDaoHibernate<CargoAbono, Long> implements CargoAbonoDao{

	public CargoAbonoDaoImpl() {
		super(CargoAbono.class);
	}	
	

	@Override
	public CargoAbono save(CargoAbono object) {
		registrarBitacora(object);
		return super.save(object);
	}




	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.dao.tesoreria.CargoAbonoDao#saldo(com.luxsoft.siipap.model.tesoreria.Cuenta)
	 */
	public BigDecimal saldo(final Cuenta cta) {
		return (BigDecimal) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				BigDecimal val=(BigDecimal)session.createQuery("select sum(x.importe) from CargoAbono x" +
						" where x.cuenta=:cta")
						.setEntity(0, cta)
						.uniqueResult();
				
				return val!=null?val:BigDecimal.ZERO;
			}			
		});
	}

	public void limpiarMovimientosImportados(final Date fecha) {
		getHibernateTemplate().execute(new HibernateCallback(){

			@SuppressWarnings("unchecked")
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				List<CargoAbono> cargos=session.createQuery("from CargoAbono c where c.fecha=:fecha and c.origen in(:M1,:M2,:M3,:M4)")
				.setParameter("M1", Origen.VENTA_MOSTRADOR)
				.setParameter("M2", Origen.VENTA_CREDITO)
				.setParameter("M3", Origen.VENTA_CAMIONETA)
				.setParameter("M4", Origen.VENTA_CONTADO)
				.setParameter("fecha", fecha,Hibernate.DATE)
				.list();
				
				System.out.println("Depositos a eliminar: "+cargos.size());
				
				for(CargoAbono c:cargos){
					session.delete(c);
				}
				return null;
			}
			
		});
		
	}

	@SuppressWarnings("unchecked")
	public List<CargoAbono> buscarPagosDeGastos() {
		return getHibernateTemplate().find("from CargoAbono c where c.origen=?", Origen.PAGO_GASTOS);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.dao.tesoreria.CargoAbonoDao#generarEstadoDeCuenta(com.luxsoft.siipap.model.tesoreria.EstadoDeCuenta)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void generarEstadoDeCuenta(final EstadoDeCuenta estado){
		getHibernateTemplate().execute(new HibernateCallback(){

			@SuppressWarnings("unchecked")
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from CargoAbono c where c.cuenta=:id and c.fecha between :f1 and :f2 and c.conciliado=false";
				List<CargoAbono> list=session
					.createQuery(hql)
					.setParameter("id", estado.getCuenta())
					.setParameter("f1", estado.getFechaInicial(),Hibernate.DATE)
					.setParameter("f2", estado.getFechaFinal(),Hibernate.DATE)
					.list();
				estado.setMovimientos(list);
				BigDecimal saldoInicial=buscarSaldo(estado.getCuenta().getId(), estado.getFechaInicial());
				if(saldoInicial!=null)
					estado.setSaldoInicial(saldoInicial);
				return estado;
			}			
		});
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public BigDecimal buscarSaldo(final Long cuentaId,final  Date antesDe) {
		return (BigDecimal) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				BigDecimal val=BigDecimal.ZERO;
				//( (Integer) session.iterate("select count(*) from ....").next() ).intValue();				
				val=(BigDecimal)session.createQuery("select sum(c.importe) from CargoAbono c where c.cuenta.id=? and c.fecha<? and c.conciliado=false")
				.setLong(0,cuentaId)
				.setParameter(1, antesDe,Hibernate.DATE)
				.uniqueResult();
				return val;
			}			
		});
	}
	
	
	public long buscarProximoCheque(final Long cuentaId){
		return (Long) getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				CargoAbono car=(CargoAbono)session.createQuery("from CargoAbono c where c.cuenta.id=? order by c.id desc")
				.setLong(0, cuentaId)
				//.setString(1, Tipo.CARGO.name())
				.setMaxResults(1)
				.uniqueResult();
				if(car==null)
					return 0l;
				String ref=car.getReferencia();
				if(StringUtils.isNumeric(ref)){
					Long val=Long.valueOf(ref);
					return val+1;
				}else
					return 0l;
			}
			
		});
	}



	public List<CargoAbono> buscarPagos(final Date fecha) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List<CargoAbono> pagos= session.createQuery("from CargoAbono c where c.fecha=? " +
						"and c.importe<0 " +
						"and c.requisicion is not null " +
						"and c.requisicion.origen=? ")
				.setParameter(0, fecha,Hibernate.DATE)
				.setString(1, Requisicion.GASTOS)
				.list();				
				return pagos;
			}
			
		});
	}



	public List<CargoAbono> buscarEgresos(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery("from CargoAbono c " +
						"where c.fecha between ?  and ? " +
						"  and c.importe<=0 " +
						"  and c.formaDePago=?")
				.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
				.setString(2, FormaDePago.CHEQUE.name())
				.list();
				
			}
			
		});
	}
	
	public CargoAbono buscarAbonoImportado(final Object id){
		String ss=(String)id;
		String hql="from CargoAbono ca where ca.comentario=?";
		List<CargoAbono> l=getHibernateTemplate().find(hql, ss);
		return l.isEmpty()?null:l.get(0);
	}
	
	private void registrarBitacora(final CargoAbono bean){
		Date time=new Date();
		
		String user=KernellSecurity.instance().getCurrentUserName();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getUserLog().setModificado(time);
		bean.getUserLog().setUpdateUser(user);
		//bean.getAddresLog().setUpdatedIp(ip);
		//bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getUserLog().setCreado(time);
			bean.getUserLog().setCreateUser(user);
			//bean.getAddresLog().setCreatedIp(ip);
			///bean.getAddresLog().setCreatedMac(mac);
		}
	}

}
