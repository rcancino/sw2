package com.luxsoft.sw2.replica.parches;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;




import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.transformer.AbstractTransformer;

/**
 * Genera una carga batch de todo el catalogo de articulos
 * 
 * @author Ruben Cancino
 *
 */
public class ReplicadorBatchDeProductos {
	
	public ReplicadorBatchDeProductos replicar(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<Producto> res=session.createQuery("from Producto p")
				//.setMaxResults(100)
				.list();
				try {
					int count=0;
					File target=new File("/basura/ProductosBatchJSON.txt");
					PrintWriter bw=new PrintWriter(target);
					JSONSerializer s=new JSONSerializer()
					.exclude("linea.corte","clase.corte","proveedor.direccion")
					.transform(new CurrencyTransformer(), Currency.class);
					
					for(Producto p:res){
						String sp=s.serialize(p);
						bw.println(sp);
						count++;
						if(count%20==0){
							session.flush();
							session.clear();
						}
					}
					bw.flush();
					bw.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		return this;
	}
	
	public ReplicadorBatchDeProductos importar(){
		try {
			File target=new File("/basura/ProductosBatchJSON.txt");
			FileInputStream in=new FileInputStream(target);
			InputStreamReader input=new InputStreamReader(in, "UTF-8");
			BufferedReader reader=new BufferedReader(input);
			JSONDeserializer sr=new JSONDeserializer<Producto>()
					.use(Currency.class, new CurrencyTransformer())					
					.use(BigDecimal.class,new ImportesMonetariosTranformers());
			
			String line=reader.readLine();
			while(line!=null){
				Producto p=(Producto)sr.deserialize(line);
				//System.out.println(p);
				try {
					ServiceLocator2.getHibernateTemplate().replicate(p, ReplicationMode.OVERWRITE);
				} catch (Exception e) {
					String msg=ExceptionUtils.getRootCauseMessage(e);
					System.out.println("Error importando producto: "+p+ " \n \tCausa: "+msg);
				}
				
				line=reader.readLine();
			}
			/*
			Collection<Producto> productos=new JSONDeserializer<List<Producto>>()
					.use(null, ArrayList.class)
					.use(Currency.class, new CurrencyTransformer())					
					.use(BigDecimal.class,new ImportesMonetariosTranformers())
					.deserialize(input);
			for(Producto p:productos){
				System.out.println(p);
				System.out.println(" Id: "+p.getId());
			}*/
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
		DBUtils.whereWeAre();
		new ReplicadorBatchDeProductos()
		//.replicar()
		.importar()
		;
	}
	

}
