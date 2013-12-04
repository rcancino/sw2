package com.luxsoft.sw2.replica.producers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.OutputHandler;
import flexjson.WriterOutputHandler;
import flexjson.transformer.AbstractTransformer;

/**
 * Replica batch para ventas
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicadorBatchDeVentas {
	
	
	
	
	public ReplicadorBatchDeVentas replicar(final Date fecha){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<Venta> res=session.createQuery("from Venta v where v.fecha =?")
				.setParameter(0, fecha,Hibernate.DATE)
				.setMaxResults(1)
				.list();
				try {
					StringBuffer buffer=new StringBuffer();
					new JSONSerializer()
					.transform(new CurrencyTransformer(), Currency.class)
					.prettyPrint(true)
					.include("sucursal.id","cliente.id","cliente.clave","vendedor.id","cobrador.id","partidas"
							,"partidas.producto.id","partidas.producto.clave","partidas.producto.descripcion","partidas.producto.nacional","partidas.producto.unidad","partidas.producto.kilos"
							,"partidas.sucursal.id")
					.exclude("direccion","sucursal.*","cliente.*","pedido","vendedor.*","cobrador.*","partidas.producto.*","partidas.sucursal.*")
					.exclude("*.class")
					.serialize(res,buffer);
					
					File target=new File("/basura/VentasJSON.txt");
					FileOutputStream out=new FileOutputStream(target);
					OutputStreamWriter writer=new OutputStreamWriter(out, "UTF-8");
					writer.write(buffer.toString());
					writer.flush();
					writer.close();
					out.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		return this;
	}
	
	public ReplicadorBatchDeVentas importar(){
		try {
			File target=new File("/basura/VentasJSON.txt");
			FileInputStream in=new FileInputStream(target);
			InputStreamReader input=new InputStreamReader(in, "UTF-8");
			Collection<Venta> ventas=new JSONDeserializer<List<Venta>>()
					.use(null, ArrayList.class)
					.use("values",Venta.class)
					.use("partidas",ArrayList.class)
					.use(Currency.class, new CurrencyTransformer())
					.use(BigDecimal.class,new ImportesMonetariosTranformers())
					.deserialize(input);
			for(Venta v:ventas){
				System.out.println(v);
				System.out.println(" Id: "+v.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
		
	}
	
	public static class CurrencyTransformer extends AbstractTransformer implements ObjectFactory{		
		public void transform(Object object) {
			Currency c=(Currency)object;
			getContext().writeQuoted(c.getCurrencyCode());			
		}
		public Object instantiate(ObjectBinder context, Object value,Type targetType, Class targetClass) {
			return Currency.getInstance((String)value);
		}
	}
	
	public static class ImportesMonetariosTranformers  implements ObjectFactory{
		
		public Object instantiate(ObjectBinder context, Object value,Type targetType, Class targetClass) {
			return new BigDecimal( value.toString() );
		}
	}
	
	public static void main(String[] args) {
		
		new ReplicadorBatchDeVentas()
		.replicar(new Date())
		.importar()
		;
	}

}
