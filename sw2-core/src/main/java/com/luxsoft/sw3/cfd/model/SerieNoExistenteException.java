package com.luxsoft.sw3.cfd.model;

import java.text.MessageFormat;

public class SerieNoExistenteException extends CFDException{

	public SerieNoExistenteException(String serie) {
		super(MessageFormat.format(
				"La serie de folios  {0} no está registrada en el sistema por lo que no se pueden" +
				" generar comprobantes fiscales digitales en el sistema" +
				" . Registre priemero la serie antes de solicitar folios",serie));
	}

}
