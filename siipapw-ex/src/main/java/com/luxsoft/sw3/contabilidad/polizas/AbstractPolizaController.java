package com.luxsoft.sw3.contabilidad.polizas;

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
import org.springframework.util.Assert;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.PolizasManager;
import com.luxsoft.utils.LoggerHelper;

/**
 * Implementacion basica para facilitar la creacion de controladores para polizas
 * 
 * @author Ruben Cancino Ramos
 *
 */
public abstract class AbstractPolizaController implements InitializingBean,BeanNameAware{
	
	Logger logger=LoggerHelper.getLogger();
	
	
	private  String clase;
	String beanName;
	
	public AbstractPolizaController(){
		
	}

	public Poliza salvar(Poliza poliza1){
		
		Poliza existente=existe(poliza1);
		if(existente!=null){
			if(MessageUtils.showConfirmationMessage("Poliza para el pago: "+existente.getReferencia()+ "Ya existe, desea actualizarla", "Acutalizar poliza")){
				existente.getPartidas().clear();
				Poliza res=generar(existente.getFecha(),existente.getReferencia());
				
				for(PolizaDet det:res.getPartidas()){
					det.setPoliza(existente);
					existente.getPartidas().add(det);
				}
				existente.actualizar();
				existente.clean();
				existente=getPolizaManager().salvarPoliza(existente);
				return existente;
			}
			return poliza1;
		}else{
			return poliza1=getPolizaManager().salvarPoliza(poliza1);
		}
	}
	
	public Poliza existe(Poliza poliza){
		String hql="from Poliza p where p.clase=? and p.referencia=?";
		Assert.hasLength(clase,"Controlador de poliza mal parametrizado... sin clase");
		Assert.hasLength(clase,"Controlador de poliza mal parametrizado... sin referencia");
		Object values[]={poliza.getClase(),poliza.getReferencia()};
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
	
	protected List<String> generarReferencias(Date fecha){
		return Arrays.asList(new SimpleDateFormat("dd/MM/yyyy").format(fecha)); 
	}

	/**
	 * Corre el proceso para generar n polizas segun el contexto definido
	 * 
	 * @return Lista de las polizas generadas
	 */
	public  List<Poliza> generar(Date fecha) {
		List<Poliza> polizas=new ArrayList<Poliza>();
		for(String referencia:generarReferencias(fecha)){
			Poliza poliza=generar(fecha,referencia);
			poliza.actualizar();
			polizas.add(poliza);
		}
		return polizas;
	}
	
	/**
	 * Genera una poliza a partir de una fecha y un dato de referencia
	 * 
	 * @param fecha
	 * @param referencia
	 * @return
	 */
	public  Poliza generar(Date fecha,String referencia){
		Poliza poliza=new Poliza();
		poliza.setClase(getClase());
		poliza.setReferencia(referencia);
		poliza.setFecha(fecha);
		procesar(poliza);
		return poliza;
	}
	
	protected abstract void procesar(Poliza poliza);
	
	

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
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

	public void setBeanName(String name) {
		this.beanName=name;		
	}

	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(getClase(),"Requiere definir la propiedad clase para este controlador :"+beanName);
		
	}

		
}


