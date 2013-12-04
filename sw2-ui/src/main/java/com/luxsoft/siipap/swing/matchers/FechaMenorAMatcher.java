package com.luxsoft.siipap.swing.matchers;

import java.util.Date;


public class FechaMenorAMatcher extends FechaMayorAMatcher{

	@Override
	protected boolean comparaFecha(Date fecha, Date fechaFromBean) {
		return fecha.compareTo(fechaFromBean)>=0;
	}
	
}