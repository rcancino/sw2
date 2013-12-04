package com.luxsoft.siipap.jms.rpc.test.spring;

import java.math.BigDecimal;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.jms.rpc.test.lingo.AutorizacionesCxC;
import com.luxsoft.siipap.model.core.Cliente;

public class ClientTest {
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext(
				"swx-jms-rpc-cliente-context.xml"
				,ClientTest.class
				);
		AutorizacionesCxC aut=(AutorizacionesCxC)ctx.getBean("autorizacionesManager");
		PagoConEfectivo pago=new PagoConEfectivo();
		pago.setTotal(BigDecimal.valueOf(23000));
		pago.setCliente(new Cliente("TEST","CLIENTE TEST"));		
		pago.setComentario("PRUEBA DE JMS OVER LINGO");
		aut.autorizarAbonoParaAplicar(pago);
		
		
	}

}
