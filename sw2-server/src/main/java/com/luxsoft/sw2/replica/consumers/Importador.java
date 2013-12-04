package com.luxsoft.sw2.replica.consumers;

import com.luxsoft.sw3.replica.EntityLog;

public interface Importador {
	
	public void importar(final EntityLog log);

}
