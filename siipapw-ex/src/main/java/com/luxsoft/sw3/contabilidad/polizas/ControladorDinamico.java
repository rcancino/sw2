package com.luxsoft.sw3.contabilidad.polizas;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.PolizasManager;
import com.luxsoft.sw3.contabilidad.ui.form.PolizaDetForm;
import com.luxsoft.utils.LoggerHelper;

public class ControladorDinamico  implements IControladorDePoliza,InitializingBean,BeanNameAware{
	
	
	
	protected Logger logger=LoggerHelper.getLogger();
	
	protected ModelMap model;
	
	private  String clase;
	
	String beanName;
	
	private List<IProcesador> procesadores=new ArrayList<IProcesador>();
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.contabilidad.polizas.IControladorDePoliza#generar(com.luxsoft.siipap.model.Periodo)
	 */
	public List<Poliza> generar(Periodo periodo) {
		List<Poliza> polizas=new ArrayList<Poliza>();
		List<Date> dias=periodo.getListaDeDias();
		for(Date dia:dias){
			polizas.addAll(generar(dia));
		}
		return polizas;
	}

	/**
	 * Prepara una poliza por cada referencia y delega al metodo generar 
	 * la generacion de cada poliza
	 * 
	 */
	public List<Poliza> generar(Date fecha) {
		List<Poliza> polizas=new ArrayList<Poliza>();
		for(String referencia:generarReferencias(fecha)){
			
			model=new ModelMap();
			model.addAttribute("fecha",fecha);
			model.addAttribute("referencia",referencia);
			model.addAttribute("jdbcTemplate",getJdbcTemplate());
			model.addAttribute("hibernateTemplate",getHibernateTemplate());
			
			cargar(model);
			Poliza poliza=generar(fecha,referencia);
			poliza.actualizar();
			polizas.add(poliza);
		}
		return polizas;
	}

	/**
	 * Delega a una lista de IProcesadorDePoliza el procesamiento de la poliza pasando como parametro 
	 * el modelo
	 * 
	 * @see com.luxsoft.sw3.contabilidad.polizas.IControladorDePoliza#generar(java.util.Date, java.lang.String)
	 */
	public Poliza generar(Date fecha, String referencia) {
		Poliza poliza=new Poliza();
		poliza.setClase(getClase());
		poliza.setReferencia(referencia);
		poliza.setFecha(fecha);
		poliza.setDescripcion(getClase()+ " - "+referencia);
		procesar(poliza,model);
		return poliza;
	}


	/**
	 * Regresa una lista de las referencias. Esta implementacion solo regresa una referencia que es
	 * la misma fecha en formato dd/MM/yyyy
	 * Es decir esta implementacion solo procesa una poliza por dia
	 * 
	 */
	public List<String> generarReferencias(Date fecha) {
		return Arrays.asList(new SimpleDateFormat("dd/MM/yyyy").format(fecha)); 
	}



	/**
	 * Template para carga el modelo de lo poliza con la informacion que pudiera ser requerida durante el procesamiento
	 * de la poliza. La idea es cargar la mayor cantidad de datos que pudieran ser ocupados en 
	 * varias ocaciones durante el procesamiento de la poliza. Normalmente util para las lecturas de informacion 
	 * a la base de datos
	 * 
	 */
	public void cargar(ModelMap model) {}


	/**
	 * Delega a las instancias de {@link IProcesador} el procesamiento de la poliza
	 * 
	 */
	public void procesar(Poliza poliza, ModelMap model) {
		
		for(IProcesador p:getProcesadores()){
			p.procesar(poliza, model);
		}
		
	}
	
	/**
	 * Re procesa las partidas de la poliza. Si el parametro total es verdadero, se recarga e modelo
	 * antes de procesar. Esto ultimo normalmente implica una lectura nueva de la base de datos
	 * mientras que un reprocesamiento sin recargar solo re genera la poliza segun las reglas impuestas
	 * por los procesadores
	 * 
	 * 
	 * @param poliza
	 * @param total
	 */
	public void recargar(Poliza poliza,boolean total){
		poliza.getPartidas().clear();
		poliza.actualizar();
		poliza.clean();
		if(total){
			logger.info("Inicializando model");
			model.clear();
			model=new ModelMap();
			cargar(model);
			procesar(poliza, model);
		}else
			procesar(poliza, model);
	}
	
	public Poliza cuadrar(final Poliza bean){
		/*
		if(bean.getFolio()==null || bean.getId()==null){
			MessageUtils.showMessage("Debe salvar primero la poliza antes de generar Otros productos/gastos", "Polizas");
			return null;
		}
		
		Poliza source=ServiceLocator2.getPolizasManager().getPolizaDao().get(bean.getId());
		
		BigDecimal cuadre=source.getCuadre();
		
		//Verificamos q la instancia en la base requiera cuadre
		if(cuadre.doubleValue()==0)
			return source;
		
		if(cuadre.doubleValue()>0){
			PolizaDetFactory.generarPolizaDet(source, "702", "OING01", false, cuadre, "AJUSTE AUTOMATICO", "", "OFICINAS", "OTROS");
		}else{
			PolizaDetFactory.generarPolizaDet(source, "704", "OGST01", true, cuadre.abs(), "AJUSTE AUTOMATICO", "", "OFICINAS", "OTROS");
		}
		source.actualizar();
		return getPolizaManager().salvarPoliza(source);
			*/
		return null;
		
	}

	/**
	 * @see BeanNameAware
	 * @param name
	 */
	public void setBeanName(String name) {
		this.beanName=name;		
	}

	/**
	 * 
	 * @see InitializingBean
	 * @throws Exception
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(getClase(),"Requiere definir la propiedad clase para este controlador :"+beanName);
		
	}

	public ModelMap getModel() {
		return model;
	}
	

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}
	
	
	public List<IProcesador> getProcesadores() {
		return procesadores;
	}

	public void setProcesadores(List<IProcesador> procesadores) {
		this.procesadores = procesadores;
	}

	/********* Implementacion de Presistencia ****/
	public Poliza salvar(Poliza poliza1){
		/*
		if(poliza1.getCuadre().abs().doubleValue()>0){
			MessageUtils.showMessage("La poliza no cuadra: "+poliza1.getCuadre(), "Salvando poliza");
			return null;
		}*/
		Poliza existente=existe(poliza1);
		if(existente!=null){
			if(MessageUtils.showConfirmationMessage("Ya existe la póliza para la referencia: "+existente.getReferencia()
					+ " y clase: "+existente.getClase()+ ", desea actualizarla", "Acutalizar poliza")){
				poliza1.setFolio(existente.getFolio());
				getPolizaManager().getPolizaDao().remove(existente.getId());
				poliza1=getPolizaManager().salvarPoliza(poliza1);
				return poliza1;
			}
			return poliza1;
		}else{
			poliza1=getPolizaManager().salvarPoliza(poliza1);;
			return poliza1;
		}
	}
	
	
	public Poliza existe(Poliza poliza){
		String hql="from Poliza p where p.clase=? and p.referencia=?";
		Assert.hasLength(clase,"Controlador de poliza mal parametrizado... sin clase");
		Assert.hasLength(poliza.getReferencia(),"Controlador de poliza mal parametrizado... sin referencia");
		Object values[]={poliza.getClase(),poliza.getReferencia()};
		List<Poliza> res=getHibernateTemplate().find(hql, values);
		return res.isEmpty()?null:getPolizaManager().getPolizaDao().get(res.get(0).getId());
	}
	
	public Poliza existe(String clase, String referencia){		
		//System.out.println("Localizando poliza Clase:"+clase+"  Ref: "+referencia);
		Assert.hasLength(clase,"Controlador de poliza mal parametrizado... sin clase");
		Assert.hasLength(referencia,"Controlador de poliza mal parametrizado... sin referencia");
		
		String hql="from Poliza p where p.clase=? and p.referencia=?";
		Object values[]={clase,referencia};
		List<Poliza> res=getHibernateTemplate().find(hql, values);
		return res.isEmpty()?null:getPolizaManager().getPolizaDao().get(res.get(0).getId());
	}
	
	/**
	 * Elimina la poliza y se pierde el consecutivo
	 * 
	 * @param poliza
	 * @return
	 */
	public boolean eliminarPoliza(Poliza poliza) {
		getPolizaManager().getPolizaDao().remove(poliza.getId());
		return true;
	}
	
	public JdbcTemplate getJdbcTemplate(){
		return ServiceLocator2.getJdbcTemplate();
	}
	
	public HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
	
	public PolizasManager getPolizaManager(){
		return ServiceLocator2.getPolizasManager();
	}
	
	public CuentaContable getCuenta(String clave){
		return ServiceLocator2.getCuentasContablesManager().buscarPorClave(clave);
	}
	

}
