package com.luxsoft.siipap.pos.pruebas;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjetaDet;

public class PruebasGenericas {
	
	
	public static void pruebaSimpleDataBase(){
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://ser-ofi-d/produccion");
		dataSource.setUsername("root");
		dataSource.setPassword("sys");
		JdbcTemplate template=new JdbcTemplate(dataSource);
		System.out.println("Rows: "+template.queryForInt("select count(*) from sx_productos"));
	}
	
	public static void cargarContexto(){
		String path="spring/applicationContext-db.xml";
		ApplicationContext ctx=new ClassPathXmlApplicationContext(path) ;
		System.out.println(ctx.getDisplayName());
		System.out.println(ctx.getBeanDefinitionNames());
		JdbcTemplate template=(JdbcTemplate)ctx.getBean("jdbcTemplate");
		System.out.println("Rows: "+template.queryForInt("select count(*) from sx_productos"));
	}
	
	public static void probarHibernate(){
		String path="spring/applicationContext-db.xml";
		ApplicationContext ctx=new ClassPathXmlApplicationContext(path) ;
		HibernateTemplate template=(HibernateTemplate)ctx.getBean("hibernateTemplate");
		System.out.println("Rows: "+template.find("from Banco p").size());
	}
	
	public static void probarRedondeo(){
		CantidadMonetaria base=CantidadMonetaria.pesos(4474.81);
		double factor=1.46d;
		CantidadMonetaria res=base.multiply(factor).divide(100);
		System.out.println("Resultado: "+res);
		CorteDeTarjeta corte=new CorteDeTarjeta();
		corte.setTipoDeTarjeta(CorteDeTarjeta.TIPOS_DE_TARJETAS[0]);
		
		CantidadMonetaria comisionDebito=CantidadMonetaria.pesos(0);
		CantidadMonetaria comisionCredito=CantidadMonetaria.pesos(0);
		if(corte.getTipoDeTarjeta().equals(CorteDeTarjeta.TIPOS_DE_TARJETAS[0])){
			
			for(CorteDeTarjetaDet det:corte.getPartidas()){
				double comision=det.getPago().getComisionBancaria();
				CantidadMonetaria imp=det.getPago().getTotalCM().multiply(comision/-100);
				if(det.getPago().getTarjeta().isDebito()){
					comisionDebito=comisionDebito.add(imp);
				}else{
					comisionCredito=comisionCredito.add(imp);
				}
					
			}
		}
		System.out.println("DEBITO: "+comisionDebito);
		
	}
	
	public static void main(String[] args) {
		//cargarContexto();
		//int res=ServiceLocator2.getJdbcTemplate().queryForInt("select count(*) from sx_productos");
		//System.out.println(res);
		//probarHibernate();
		probarRedondeo();
	}

}
