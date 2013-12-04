package com.luxsoft.siipap.pos.facturacion;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

public class VelocityTest {
	
	public static void main(String[] args) throws Exception {
		Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, VelocityTest.class);
		Velocity.init();
		Template template=Velocity.getTemplate("templates/pedidoHeader.vm");
	}

}
