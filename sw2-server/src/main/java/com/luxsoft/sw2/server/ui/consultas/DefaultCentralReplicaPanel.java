package com.luxsoft.sw2.server.ui.consultas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.ClassUtils;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Abstract class para simplificar la creacion de paneles para el mantenimiento de la replica de entidades
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DefaultCentralReplicaPanel<E> extends FilteredBrowserPanel {
	
	protected Map<String, String[]> textFiltes=new HashMap<String, String[]>();
	
	public DefaultCentralReplicaPanel(Class beanClazz) {
		super(beanClazz);
		setTitle(ClassUtils.getShortName(beanClazz));
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-2);
	}
	
	@Override
	protected void initGlazedList() {
		for(Map.Entry<String, String[]> entry:textFiltes.entrySet()){
			installTextComponentMatcherEditor(entry.getKey(), entry.getValue());
		}
		super.initGlazedList();
	}
	
	
	public void open(){
		load();
	}
	
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()				
				,addAction(null,"replicar","Replicar")
				,addAction(null,"replicaBatch","Replica batch")
				};
		return actions;
	}
	
	
	protected Object[] getDefaultParameters(){
		return new Object[]{				
				periodo.getFechaInicial()
				,periodo.getFechaFinal()
		};
	}
	
	protected String hibernateQuery="from @CLASS s where  date(s.fecha) between ? and ?";
	
	public String getHibernateQuery() {
		return hibernateQuery;
	}

	public void setHibernateQuery(String hibernateQuery) {
		this.hibernateQuery = hibernateQuery;
	}

	@Override
	protected List findData() {
		String hql=getHibernateQuery();
		hql=hql.replaceAll("@CLASS", ClassUtils.getShortName(beanClazz));
		return getHibernateTemplate().find(hql,getDefaultParameters());
	}
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
	
	
	
	

	public Map<String, String[]> getTextFiltes() {
		return textFiltes;
	}

	public void setTextFiltes(Map<String, String[]> textFiltes) {
		this.textFiltes = textFiltes;
	}

	public void replicar(){
		List togo=new ArrayList();
		if(!getSelected().isEmpty()){
			togo.addAll(getSelected());
			if(MessageUtils.showConfirmationMessage("Replicar "+togo.size()+ " entidades ", "Replica de entidades")){				
				//getManager().replicar(togo);
			}
		}
	}
	
	/**
	 * Template method para preparar el bean antes de la replica
	 * 
	 * @param bean
	 * @return
	 */
	public Object prepararReplica(Object bean){
		return bean;
	}
	
	public void replicaBatch(){
		List togo=new ArrayList();
		if(!getSelected().isEmpty()){
			togo.addAll(getSelected());
		}else
			togo.addAll(getSource());
		if(MessageUtils.showConfirmationMessage("Replica Batch de: "+togo.size()+ " entidades ", "Replica de entidades")){
			//getManager().replicaBatch(togo);
		}
		
	}
	
	
}
