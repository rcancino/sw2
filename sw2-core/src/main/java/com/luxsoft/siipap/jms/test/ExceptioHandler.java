package com.luxsoft.siipap.jms.test;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

public class ExceptioHandler implements ExceptionListener{

	public void onException(JMSException exception) {
		System.out.println("JMS error: "+exception.getMessage());
	}

}
