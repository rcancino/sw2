package com.luxsoft.sw3.services;

import com.luxsoft.siipap.inventarios.model.Transformacion;


public interface TransformacionesManager {
	
	public Transformacion get(String id);
	
	public Transformacion save(final Transformacion t);
	
	public void remove(String id);

}
