package com.luxsoft.sw3.contabilidad.polizas;

import java.util.Date;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.contabilidad.model.Poliza;

public interface IControladorDePoliza {
	
	/**
	 * Genera una lista de las polizasa requeidas para una fecha 
	 * Las implementaciones deben generar una poliza por cada referencia 
	 * que el metodo generarReferencias
	 *  
	 * @param fecha
	 * @return
	 */
	public  List<Poliza> generar(Date fecha);
	
	/**
	 * Genera una lista de las polizasa requeidas para un periodo 
	 * 
	 * 
	 * @param periodo
	 * @return
	 */
	public  List<Poliza> generar(Periodo periodo);
	
	/**
	 * Inicializa una poliza con los datos requeridos para el caso de uso 
	 * es decir unicamente se encarga de preparar la entidad Poliza sin importar
	 * su contenido
	 *  Las implementaciones deben preparar la poliza y procesarla mediante el metodo procesar
	 *  para generar el contenido de la poliza
	 *  
	 * @param fecha
	 * @param referencia
	 * @return
	 */
	public  Poliza generar(Date fecha,String referencia);
	
	/**
	 * Genera una lista de las referencias que se requieren para cada poliza a generar 
	 * Este metodo define cuantas polizas se generaran por dia para el caso especifico
	 * 
	 * @param fecha
	 * @return
	 */
	public List<String> generarReferencias(Date fecha);
	
	
	
	/**
	 * Template para carga el modelo de lo poliza con la informacion que pudiera ser requerida durante el procesamiento
	 * de la poliza. La idea es cargar la mayor cantidad de datos que pudieran ser ocupados en 
	 * varias ocaciones durante el procesamiento de la poliza
	 * 
	 * @param model
	 */
	public void cargar(ModelMap model);
	
	/**
	 * Procesa la poliza para generar el contenido de la misma segun sea el caso
	 * La intencion de este metodo es centralizar la logica para generar el contenido de la poliza
	 * Este metodo se detona cuando la poliza ya esta inicializada y el modelo cargado
	 * 
	 * @param poliza
	 * @param model
	 */
	public void procesar(Poliza poliza,ModelMap model);

}
