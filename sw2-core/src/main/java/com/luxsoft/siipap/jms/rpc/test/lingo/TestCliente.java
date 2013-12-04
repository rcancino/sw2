package com.luxsoft.siipap.jms.rpc.test.lingo;

import java.math.BigDecimal;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

public class TestCliente {
	
	public static void main(String[] args) {
		ApplicationContext ctx=new ClassPathXmlApplicationContext("jms-rpc-cliente-testcontext.xml",TestCliente.class);
		AutorizacionesCxC aut=(AutorizacionesCxC)ctx.getBean("autorizacionesService");
		PagoConEfectivo pago=new PagoConEfectivo();
		pago.setTotal(BigDecimal.valueOf(23000));
		pago.setCliente(new Cliente("TEST","CLIENTE TEST"));		
		pago.setComentario("PRUEBA DE JMS OVER LINGO");
		aut.autorizarAbonoParaAplicar(pago);
		//System.out.println(pago);		System.out.println(pago.getComentario());
		
	}

}
