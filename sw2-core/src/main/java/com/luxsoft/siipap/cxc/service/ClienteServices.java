package com.luxsoft.siipap.cxc.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.luxsoft.siipap.cxc.model.CXCUtils;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.replica.ExportUtils;
import com.luxsoft.siipap.replica.ExportadorDeClientes;
import com.luxsoft.siipap.replica.ExportadorDeSaldoDeCliente;
import com.luxsoft.siipap.replica.ReplicaExporter.Tipo;
import com.luxsoft.siipap.service.ServiceLocator2;

import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;

public class ClienteServices extends HibernateDaoSupport{
	
	Logger logger=Logger.getLogger(getClass());
	
	public boolean permitirExportacion=true;
	
	
	public Object[] calcularSaldoyAtraso( Cliente c){
		
		final String clave=c.getClave();

		Object[] datos=(Object[])getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				//Cliente c=(Cliente)session.load(Cliente.class, id);
				logger.info("Calculando saldo para:" +clave);
				
				List<Cargo> cargos=session.createQuery("from Cargo c " +
						" where c.clave=? " +
						"  and c.fecha>?" +
						"  and (c.total-c.aplicado)!=0" +
						"  and c.origen=\'CRE\'")
						.setString(0, clave)
						.setParameter(1, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
						.list();
				BigDecimal saldoCargos=BigDecimal.ZERO;
				BigDecimal moratorios=BigDecimal.ZERO;
				int atrasoMaximo=0;
				for(Cargo ca:cargos){
					saldoCargos=saldoCargos.add(ca.getSaldoCalculado());
					moratorios=moratorios.add(getMoratorios(ca));
					if(ca.getAtraso()>atrasoMaximo)
						atrasoMaximo=ca.getAtraso();
				}
				
				
				
				List<NotaDeCredito> disponibles=session.createQuery("from NotaDeCredito n" +
						" where n.clave=? " +
						"   and n.fecha>?" +
						"   and (n.total-n.diferencia-n.aplicado)>0" +
						"   and n.origen=\'CRE\'")
						.setString(0, clave)
						.setParameter(1, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
						.list();
				
				BigDecimal disponible=BigDecimal.ZERO;
				for(NotaDeCredito nota:disponibles){
					disponible=disponible.add(nota.getDisponibleCalculado());
				}
				
				
				BigDecimal saldo=saldoCargos.subtract(disponible).add(moratorios);
				
				logger.info("Saldo de Facturas: "+saldoCargos);
				logger.info("Moratorios: "+moratorios);
				logger.info("Disponible  acreditable: "+disponible);
				logger.info("Saldo general:" +saldo);
				logger.info("Atraso maximo:" +atrasoMaximo);
				
				//c.getCredito().setSaldo(saldo);
				//c.getCredito().setAtrasoMaximo(atrasoMaximo);
				//c.getLog().setModificado(new Date());
				return new Object[]{saldo,atrasoMaximo};
				//return saldo;
			}
		});
		
		return datos;
	}
	
	/**
	 * Regresa el saldo general de cliente
	 * 
	 * @param c
	 * @return
	 */
	public BigDecimal getSaldo(final Cliente c){

		return (BigDecimal)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				logger.info("Calculando saldo para:" +c.getNombreRazon());
				
				List<Cargo> cargos=session.createQuery("from Cargo c " +
						" where c.clave=? " +
						"  and c.fecha>?" +
						"  and (c.total-c.aplicado)!=0" +
						"  and c.origen=\'CRE\'")
						.setString(0, c.getClave())
						.setParameter(1, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
						.list();
				BigDecimal saldoCargos=BigDecimal.ZERO;
				BigDecimal moratorios=BigDecimal.ZERO;
				for(Cargo ca:cargos){
					saldoCargos=saldoCargos.add(ca.getSaldoCalculado());
					moratorios=moratorios.add(getMoratorios(ca));
				}
				
				
				
				List<NotaDeCredito> disponibles=session.createQuery("from NotaDeCredito n" +
						" where n.clave=? " +
						"   and n.fecha>?" +
						"   and (n.total-n.diferencia-n.aplicado)>0" +
						"   and n.origen=\'CRE\'")
						.setString(0, c.getClave())
						.setParameter(1, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
						.list();
				
				BigDecimal disponible=BigDecimal.ZERO;
				for(NotaDeCredito nota:disponibles){
					disponible=disponible.add(nota.getDisponibleCalculado());
				}
				
				
				BigDecimal saldo=saldoCargos.subtract(disponible).add(moratorios);
				if(logger.isDebugEnabled()){
					
				}
				logger.info("Saldo de Facturas: "+saldoCargos);
				logger.info("Moratorios: "+moratorios);
				logger.info("Disponible  acreditable: "+disponible);
				logger.info("Saldo general:" +saldo);
				return saldo;
			}
		});
		
	}
	
	private BigDecimal getMoratorios(Cargo cargo){
		double pena=1.0;
		int atraso=cargo.getAtraso();
		if(atraso<=0 || (cargo.getCargosAplicados().doubleValue()>0))
			return BigDecimal.ZERO;
		int semanas=atraso/7;
		double car=( ((double)semanas)*pena)/100;
		CantidadMonetaria imp=new CantidadMonetaria(cargo.getTotal().subtract(cargo.getDevoluciones()),cargo.getMoneda());
		return imp.multiply(car).amount();
	}
	
	public BigDecimal getSaldo(final String clave){

		return (BigDecimal)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				logger.info("Calculando saldo para:" +clave);
				
				List<Cargo> cargos=session.createQuery("from Cargo c " +
						" where c.clave=? " +
						"  and c.fecha>?" +
						"  and (c.total-c.aplicado)!=0" +
						"  and c.origen=\'CRE\'")
						.setString(0, clave)
						.setParameter(1, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
						.list();
				BigDecimal saldoCargos=BigDecimal.ZERO;
				for(Cargo ca:cargos){
					saldoCargos=saldoCargos.add(ca.getSaldoCalculado());
				}
				
				
				
				List<NotaDeCredito> disponibles=session.createQuery("from NotaDeCredito n" +
						" where n.clave=? " +
						"   and n.fecha>?" +
						"   and (n.total-n.aplicado)>5" +
						"   and n.origen=\'CRE\'")
						.setString(0, clave)
						.setParameter(1, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
						.list();
				
				BigDecimal disponible=BigDecimal.ZERO;
				for(NotaDeCredito nota:disponibles){
					disponible=disponible.add(nota.getDisponibleCalculado());
				}
				
				
				BigDecimal saldo=saldoCargos.subtract(disponible);
				if(logger.isDebugEnabled()){
					logger.debug("Saldo de Facturas: "+saldoCargos);
					logger.debug("Disponible  acreditable: "+disponible);
					logger.info("Saldo general:" +saldo);
				}
				
				return saldo;
			}
		});
		
	}
	
	
	
	/**
	 * Exporta el saldo del cliente a SIIPAP (Clipper)
	 * @deprecated Ya no se requiere 
	 * @param c
	 */
	public String exportarSaldo(final Cliente c){
		return exportarSaldo(c, getSaldo(c));
	}
	
	/**
	 * Exporta el saldo del cliente a SIIPAP (Clipper)
	 * @deprecated Ya no se requiere, era para siipap dbf
	 * @param c
	 */
	public String exportarSaldo(final Cliente c,BigDecimal saldo){
		/*
		if(isPermitirExportacion()){
			final ExportadorDeSaldoDeCliente exp=new ExportadorDeSaldoDeCliente();
			String data=exp.export(c, Tipo.C,saldo);
			String res=ExportUtils.toDORFile(data);
			return res;
		}
		return null;
		*/
		return null;
	}
	
	public String exportarCliente(final Cliente c){
		if(isPermitirExportacion()){
			/*
			final ExportadorDeClientes exp=new ExportadorDeClientes();
			String data=exp.export(c, Tipo.C);
			String res=ExportUtils.toDORFile(data);
			return res;
			*/
		}
		return null;
	}
	
	/**
	 * Actualiza el saldo de los clientes de credito usando el SP: SALDO_CTE_CRED
	 * 
	 */
	public void actualizarSaldoDeClientesCredito(){
		List<Cliente> clientes=ServiceLocator2.getClienteManager().buscarClientesCredito();
		for(Cliente c:clientes){
			System.out.println("Actualizando saldo para: "+c.getClave());
			BigDecimal saldoAnterior=c.getCredito().getSaldo();
			Map<String,Object> inParams=new HashMap<String, Object>();
			inParams.put("CLAVES", c.getClave());
			
			JdbcTemplate template=ServiceLocator2.getJdbcTemplate();
			StoredProcedure sp=new StoredProcedure(template,"SALDO_CTE_CRED"){
				
			};
			sp.declareParameter(new SqlParameter("CLAVES",Types.VARCHAR));
			sp.execute(inParams);
			c=ServiceLocator2.getClienteManager().get(c.getId());
			System.out.println("Saldo actualizado: "+c.getClave()+ "Anterior: "+saldoAnterior+" Nuevo: "+c.getCredito().getSaldo());
		}
	}
	
	/**
	 * Actualiza mediante un SP el saldo del cliente
	 * @param c
	 */
	public Cliente actualizarSaldoDeCliente(Cliente c){		
		System.out.println("Actualizando saldo para: "+c.getClave());
		BigDecimal saldoAnterior=c.getCredito().getSaldo();
		Map<String,Object> inParams=new HashMap<String, Object>();
		inParams.put("CLAVES", c.getClave());
		
		JdbcTemplate template=ServiceLocator2.getJdbcTemplate();
		StoredProcedure sp=new StoredProcedure(template,"SALDO_CTE_CRED"){
			
		};
		sp.declareParameter(new SqlParameter("CLAVES",Types.VARCHAR));
		sp.execute(inParams);
		c=ServiceLocator2.getClienteManager().get(c.getId());
		System.out.println("Saldo actualizado: "+c.getClave()+ "Anterior: "+saldoAnterior+" Nuevo: "+c.getCredito().getSaldo());
		return c;
	}
	
	public void actualizarAtrasoMaximo(){
		List<Cliente> clientes=ServiceLocator2.getClienteManager().buscarClientesCredito();
		for(Cliente c:clientes){
			String sql=SQLUtils.loadSQLQueryFromResource("sql/Clientes_credito_atraso_max.sql");
			List<Integer> res=ServiceLocator2.getJdbcTemplate().queryForList(sql,new Object[]{c.getClave()},Integer.class);
			int atraso=0;
			if(!res.isEmpty()){
				atraso=res.get(0);
				if(atraso<0)
					atraso=0;
			}
			if(c.getCredito().getAtrasoMaximo()!=atraso){
				c.getCredito().setAtrasoMaximo(atraso);
				//c.getComentarios().put("", value)
				ServiceLocator2.getClienteManager().save(c);
				System.out.println("Atraso maximo actualizado: "+c.getClave()+ " atraso:: "+atraso);
			}
		}
	}
	
	public void actualizarSaldoEnChequeDevueltos(Cliente cliente){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(cliente.getClave());
		String sql2=SQLUtils.loadSQLQueryFromResource("sql/Clientes_saldo_cheques_devueltos.sql");
		List<BigDecimal> res=ServiceLocator2.getJdbcTemplate().queryForList(sql2,new Object[]{c.getClave()},BigDecimal.class);
		BigDecimal saldo=BigDecimal.ZERO;
		if(!res.isEmpty()){
			saldo=res.get(0);
		}
		if(!c.getChequesDevueltos().equals(saldo)){
			c.setChequesDevueltos(saldo);
			c.getLog().setModificado(new Date());
			ServiceLocator2.getClienteManager().save(c);
			//System.out.println("Saldo en cheques devueltos: "+c.getClave()+ " saldo: "+saldo);
		}
	}
	
	public void actualizarSaldoEnChequeDevueltos(){
		String sql="select distinct clave from sx_ventas where origen='CHE'";
		List<String> claves=ServiceLocator2.getJdbcTemplate().queryForList(sql,String.class);
		for(String clave:claves){
			Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
			String sql2=SQLUtils.loadSQLQueryFromResource("sql/Clientes_saldo_cheques_devueltos.sql");
			List<BigDecimal> res=ServiceLocator2.getJdbcTemplate().queryForList(sql2,new Object[]{c.getClave()},BigDecimal.class);
			BigDecimal saldo=BigDecimal.ZERO;
			if(!res.isEmpty()){
				saldo=res.get(0);
			}
			if(!c.getChequesDevueltos().equals(saldo)){
				c.setChequesDevueltos(saldo);
				ServiceLocator2.getClienteManager().save(c);
				System.out.println("Saldo en cheques devueltos: "+c.getClave()+ " saldo: "+saldo);
			}
		}
	}
	
	public void actualizarSaldoEnJuridico(){
		String sql="SELECT DISTINCT clave FROM sx_juridico";
		List<String> claves=ServiceLocator2.getJdbcTemplate().queryForList(sql,String.class);
		for(String clave:claves){
			Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
			String sql2=SQLUtils.loadSQLQueryFromResource("sql/Clientes_saldo_juridico.sql");
			List<BigDecimal> res=ServiceLocator2.getJdbcTemplate().queryForList(sql2,new Object[]{c.getClave()},BigDecimal.class);
			BigDecimal saldo=BigDecimal.ZERO;
			if(!res.isEmpty()){
				saldo=res.get(0);
			}
			c.setJuridico(saldo.doubleValue()>0);
			ServiceLocator2.getClienteManager().save(c);
			System.out.println("Saldo en juridico devueltos: "+c.getClave()+ " saldo: "+saldo);
		}
	}

	public boolean isPermitirExportacion() {
		return permitirExportacion;
	}

	public void setPermitirExportacion(boolean permitirExportacion) {
		this.permitirExportacion = permitirExportacion;
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		//ServiceLocator2.getClienteServices().actualizarAtrasoMaximo();
		//ServiceLocator2.getClienteServices().actualizarSaldoEnChequeDevueltos();
		//ServiceLocator2.getClienteServices().actualizarSaldoEnJuridico();
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave("E010022");
		ServiceLocator2.getClienteServices().calcularSaldoyAtraso(c);
		//System.out.println();
		
	}
	
}
