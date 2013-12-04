package com.luxsoft.luxor.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import com.jgoodies.binding.PresentationModel;
import com.luxsoft.siipap.ventas.model.Venta;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class Bean implements MethodInterceptor{
	
	protected transient PropertyChangeSupport support;
	
	Logger logger;
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}
	 
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);		
	}
	
	public void firePropertyChange(String propertyName, Object old, Object value){
		support.firePropertyChange(propertyName, old, value);
	}

	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {
		Object retValFromSuper=null;
		try{
			if(!Modifier.isAbstract(method.getModifiers())){
				retValFromSuper=proxy.invokeSuper(obj, args);
			}
		}finally{
			String name=method.getName();			
			if("addPropertyChangeListener".equals(name)){
				addPropertyChangeListener((PropertyChangeListener)args[0]);
			}else if("removePropertyChangeListener".equals(name)){
				removePropertyChangeListener((PropertyChangeListener)args[0]);
			}
			if(name.startsWith("set") && args.length==1 && method.getReturnType()==Void.TYPE){
				char propName[] = name.substring("set".length()).toCharArray();
				propName[0] = Character.toLowerCase( propName[0] );
				firePropertyChange( new String( propName ) , null , args[0]);
			}
		}
		return retValFromSuper;
	}
	
	public static Object proxy(Class clazz){
		try {
			Bean interceptor=new Bean();
			Enhancer e=new Enhancer();
			e.setSuperclass(clazz);
			
			e.setInterfaces(new Class[]{BasicJavaBean.class});
			e.setCallback(interceptor);
			Object bean=e.create();
			interceptor.support=new PropertyChangeSupport(bean);
			interceptor.logger=Logger.getLogger(clazz);
			return bean;
			
		} catch (Throwable e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}
	}
	
	public static void normalizar(Object proxy,Object target,String[] exlude){
		BeanUtils.copyProperties(proxy, target,exlude);
	}
	
	public static void main(String[] args) {
		Venta proxy=(Venta)proxy(Venta.class);
		System.out.println("Proxy class: "+proxy.getClass().getName());
		PresentationModel model=new PresentationModel(proxy);
		BasicJavaBean bj=(BasicJavaBean)proxy;
		bj.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("Cambio propiedad: "
						+evt.getPropertyName()+ " Valor:"
						+evt.getNewValue());				
			}			
		});
		proxy.setClave("Clie clave");
		System.out.println(proxy);
	}

}
