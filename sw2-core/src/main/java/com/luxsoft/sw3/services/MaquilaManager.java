package com.luxsoft.sw3.services;


import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.OrdenDeCorte;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorte;
import com.luxsoft.sw3.maquila.model.SalidaDeBobinas;

/**
 * Service facade para las operaciones de Maquila
 * 
 * @author Ruben Cancino
 *
 */
public interface MaquilaManager {
	
	public EntradaDeMaterial getEntrada(final Long id);
	
	public EntradaDeMaterial salvarEntrada(final EntradaDeMaterial entrada);
	
	public void eliminarEntrada(final EntradaDeMaterial entrada);
	
	public void actualiarCostos(final EntradaDeMaterialDet entrada);

	public OrdenDeCorte getOrden(final Long id);
	
	public OrdenDeCorte salvarOrden(final OrdenDeCorte orden);
	
	public void eliminarOrden(final OrdenDeCorte orden);
	
	public RecepcionDeCorte getRecepcionDeCorte(final Long id);
	
	public RecepcionDeCorte salvarRecepcionDeCorte(final RecepcionDeCorte recepcion);
	
	public void eliminarRecepcionDeCorte(final RecepcionDeCorte recepcion);
	
	
	public SalidaDeBobinas getSalidaDeBobina(final Long id);
	
	public SalidaDeBobinas salvarSalidaDeBobina(final SalidaDeBobinas salida);
	
	public void eliminarSalidaDeBobina(final SalidaDeBobinas salida);
	
	
	// Analisis de Materia prima
	
	public AnalisisDeMaterial getAnalisis(final Long id);
	
	public AnalisisDeMaterial salvarAnalisis(final AnalisisDeMaterial a);
	
	public void eliminarAnalisis(final AnalisisDeMaterial a);

	// Analisis de flete
	
	public AnalisisDeFlete getAnalisisDeFlete(final Long id);
	
	public AnalisisDeFlete salvarAnalisisDeFlete(final AnalisisDeFlete a);
	
	public void eliminarAnalisisDeFlete(final AnalisisDeFlete a);
	
	public AnalisisDeFlete generarCuentaPorPagar(final AnalisisDeFlete a);
	
	// 	Analisis de Hojeo
	
	public AnalisisDeHojeo getAnalisisDeHojeo(final Long id);
	
	public AnalisisDeHojeo salvarAnalisisDeHojeo(final AnalisisDeHojeo a);
	
	public void eliminarAnalisisDeHojeo(final AnalisisDeHojeo a);

	public AnalisisDeHojeo generarCuentaPorPagar(AnalisisDeHojeo a);
	
	public AnalisisDeMaterial generarCuentaPorPagar(AnalisisDeMaterial a);
	
}
