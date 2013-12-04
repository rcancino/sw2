package com.luxsoft.siipap.swing;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import com.jgoodies.uif.util.SystemUtils;

/**
 * Pruebas genericas sobre cualquier libreria, basicamente solo sirve para en el metodo main
 * probar algunas cosas relacionadas con librerias de terceros
 * 
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class PruebaGenerica {
	
	
	public static void main(String[] args) {
		List frames=new ArrayList();
		int buff=1;
		NotificationListener myListener=new NotificationListener(){

			public void handleNotification(Notification notification, Object handback) {				
				System.out.println(notification.getMessage());
				System.out.println(notification.getType());
				System.out.println(notification.getSource());
			}
			
		};
		final MemoryMXBean mbean=ManagementFactory.getMemoryMXBean();
		NotificationEmitter emmiter=(NotificationEmitter)mbean;
		emmiter.addNotificationListener(myListener, null, null);
		while(true){
			frames.add("Ste");
			
			//MemoryMXBean mbean=ManagementFactory.getMemoryMXBean();
			
			
			//System.out.println("Instancias: "+buff++);
			
			while(buff++%10000==0){
				
				System.out.println("Limpiando buffer");
				mbean.gc();								
				SystemUtils.sleep(2000);
				frames.clear();
				long commited=mbean.getHeapMemoryUsage().getCommitted()/1000;
				long init=mbean.getHeapMemoryUsage().getInit()/1000;
				long max=mbean.getHeapMemoryUsage().getMax()/1000;
				long used=mbean.getHeapMemoryUsage().getUsed()/1000;
				System.out.println("Commited: "+commited+" \tinit:"+init+"\tmax :"+max+"\tused: "+used);
				
			}
		}
		
		
	}

}
