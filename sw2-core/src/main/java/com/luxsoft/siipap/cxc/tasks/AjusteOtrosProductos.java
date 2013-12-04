package com.luxsoft.siipap.cxc.tasks;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.Cuenta;


/**
 * Llena la columna de DISPONILBE en un abono 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AjusteOtrosProductos {
	
	private HibernateTemplate template;
	
	public void execute(final Periodo periodo,final BigDecimal minimo){
		final Cuenta cta=(Cuenta)template.find("from Cuenta c").get(0);
		template.execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from Abono a where a.fecha between ? and ? " +
						" and a.total-a.aplicado between 0.01 and ? ";
				ScrollableResults rs=session.createQuery(hql)
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
				.setBigDecimal(2, minimo)
				.scroll();
				int buff=0;
				while(rs.next()){
					try {
						Abono a=(Abono)rs.get()[0];
						System.out.println("Ajustando :"+a);
						a.setDiferencia(a.getDisponibleCalculado());
						a.setDirefenciaFecha(new Date());
						if(a instanceof PagoConDeposito){
							PagoConDeposito deposito=(PagoConDeposito)a;
							if(deposito.getFechaDeposito()==null)
								deposito.setFechaDeposito(deposito.getFecha());
							if(StringUtils.isBlank(deposito.getReferenciaBancaria()))
								deposito.setReferenciaBancaria("ND");
							if(StringUtils.isBlank(deposito.getBanco()))
								deposito.setBanco("ND");
							if(deposito.getCuenta()==null){
								deposito.setCuenta(cta);
							}
						}
						session.flush();
						session.clear();
						buff++;
						if(buff%20==0){
							
						}
					} catch (Exception e) {
						System.out.println(ExceptionUtils.getRootCauseMessage(e));
					}
					
				}
				return null;
			}			
		});
	}
	
	public void ajustePorAbono(final String abonoId){
		final Cuenta cta=(Cuenta)template.find("from Cuenta c").get(0);
		template.execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from Abono a where a.id=?";
				Abono a=(Abono)session.get(Abono.class, abonoId);
				System.out.println("Ajustando :"+a);
				System.out.println("Diferencaia: "+a.getDisponibleCalculado());
				a.setDiferencia(a.getDisponibleCalculado());
				a.setDirefenciaFecha(new Date());
				if(a instanceof PagoConDeposito){
					PagoConDeposito deposito=(PagoConDeposito)a;
					if(deposito.getFechaDeposito()==null)
						deposito.setFechaDeposito(deposito.getFecha());
					if(StringUtils.isBlank(deposito.getReferenciaBancaria()))
						deposito.setReferenciaBancaria("ND");
					if(StringUtils.isBlank(deposito.getBanco()))
						deposito.setBanco("ND");
					if(deposito.getCuenta()==null){
						deposito.setCuenta(cta);
					}
				}
				//session.flush();
				//session.clear();
				return null;
			}			
		});
	}

	public HibernateTemplate getTemplate() {
		return template;
	}

	public void setTemplate(HibernateTemplate template) {
		this.template = template;
	}
	
	public static void main(String[] args) {
		AjusteOtrosProductos task=new AjusteOtrosProductos();
		task.setTemplate(com.luxsoft.siipap.service.ServiceLocator2.getHibernateTemplate());
		//task.setTemplate(Services.getInstance().getHibernateTemplate());
		task.execute(new Periodo("01/01/2010","06/01/2010"), BigDecimal.valueOf(10.0));
		//task.ajustePorAbono("8a8a8198-25bd12c4-0125-bd200b78-001d");
	}

}
