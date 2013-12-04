package com.luxsoft.sw2.replica;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.annotations.common.AssertionFailure;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.thoughtworks.xstream.XStream;

public class PruebasDeSerializacion {
	
	
	public  static void test(){
		final XStream xstream=new XStream();
		String xml=(String)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				Object entity=session.load(Abono.class, "8a8a8190-36696788-0136-69f0a408-002a");
				entity=((HibernateProxy)entity).getHibernateLazyInitializer().getImplementation();
				
				return xstream.toXML(entity);
			}
		});
		System.out.println(xml);
		Abono abono= (Abono)xstream.fromXML(xml);
		//abono.initPropSupport();
		abono.setReplicado(new Date());
		abono.setComentario("REPLICADO ...2");
		System.out.println("Abono: "+abono);
		try {
			ServiceLocator2.getHibernateTemplate().replicate(abono, ReplicationMode.OVERWRITE);
			//ServiceLocator2.getHibernateTemplate().saveOrUpdate(abono);
		} catch (AssertionFailure e) {
			System.out.println("Warn: "+e);
		}
		
	}
	
	public static void main(String[] args) {
		test();
	}

}
