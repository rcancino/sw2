package com.luxsoft.siipap.cxc.parches;

import com.luxsoft.siipap.cxc.model.Esquema;
import com.luxsoft.siipap.cxc.model.EsquemaPorTarjeta;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.service.ServiceLocator2;

public class ActualizarEsquemasDeTarjetas {
	
	
	public static void salvarEsquemas(){
		Esquema m3=new Esquema("3 MESES SIN INTERESES");
		Esquema m6=new Esquema("6 MESES SIN INTERESES");
		ServiceLocator2.getUniversalDao().save(m3);
		ServiceLocator2.getUniversalDao().save(m6);
	}
	
	public static void salvarPromiciones(int tarjetaId){
		
		Esquema e1=(Esquema)ServiceLocator2.getHibernateTemplate().get(Esquema.class, new Long(1));
		Esquema e2=(Esquema)ServiceLocator2.getHibernateTemplate().get(Esquema.class, new Long(2));
		
		final EsquemaPorTarjeta es1=new EsquemaPorTarjeta();
		es1.setComisionBancaria(3.2d);
		es1.setComisionVenta(10d);
		es1.setEsquema(e1);
		
		final EsquemaPorTarjeta es2=new EsquemaPorTarjeta();
		es2.setComisionBancaria(5.2d);
		es2.setComisionVenta(10d);
		es2.setEsquema(e2);
		
		Tarjeta tarjeta=(Tarjeta)ServiceLocator2.getHibernateTemplate().get(Tarjeta.class, new Long(tarjetaId));
		tarjeta.getEsquemas().add(es1);
		tarjeta.getEsquemas().add(es2);
		
		ServiceLocator2.getUniversalDao().save(tarjeta);
		
	}
	
	public static void main(String[] args) {
		//salvarEsquemas();
		salvarPromiciones(28);
	}

}
