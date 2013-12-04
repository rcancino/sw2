package com.luxsoft.siipap.cxc.service.script;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Prueba para el uso de scripting en java
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ScriptingServiceTest {
	
	public void test() throws IOException{
		ApplicationContext ctx=new ClassPathXmlApplicationContext(
				new String[]{"classpath:com/luxsoft/siipap/cxc/service/script/applicationContext-scripting.xml"}
				//,ServiceLocator2.instance().getContext()
				);
		Coconut c=(Coconut)ctx.getBean("coconut");		
		c.drinkThemBothUp();
	}
	
	public static void main(String[] args) throws IOException {
		JFrame app=new JFrame("Scripting test");
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JButton test=new JButton("TEST");
		final ScriptingServiceTest st=new ScriptingServiceTest();
		test.addActionListener(EventHandler.create(ActionListener.class, st, "test"));
		app.getContentPane().add(test);
		app.pack();
		app.setVisible(true);
		
	}

}
