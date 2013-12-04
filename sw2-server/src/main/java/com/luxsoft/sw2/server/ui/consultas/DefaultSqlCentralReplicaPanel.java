package com.luxsoft.sw2.server.ui.consultas;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTextField;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.SQLBrowserPanel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.replica.EntityLog;

/**
 * Abstract class para simplificar la creacion de paneles para el mantenimiento de la replica de entidades
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DefaultSqlCentralReplicaPanel extends SQLBrowserPanel implements InitializingBean{
	
	protected Map<String, String> filters=new HashMap<String, String>();
	
	public DefaultSqlCentralReplicaPanel() {
		super();
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-2);
	}
	
	@Override
	protected void initGlazedList() {
		for(Map.Entry<String, String> entry:filters.entrySet()){
			addEditor(entry);
		}
		super.initGlazedList();
	}
	
	
	public TextComponentMatcherEditor addEditor(final Map.Entry<String, String> entry){
		final JTextField tf=new JTextField(5);
		tf.setName(entry.getKey());
		final TextFilterator<Map<String, Object>> filterator=new TextFilterator<Map<String,Object>>() {
			public void getFilterStrings(List<String> baseList,Map<String, Object> element) {
				String add=ObjectUtils.toString(element.get(entry.getValue()));
				baseList.add(add);
			}
		};
		final TextComponentMatcherEditor editor=new TextComponentMatcherEditor(tf,filterator);
		matcherEditors.add(editor);
		textEditors.put(entry.getKey(), tf);
		return editor;
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
			new SqlParameterValue(Types.DATE,periodo.getFechaInicial())
			,new SqlParameterValue(Types.DATE,periodo.getFechaFinal())
				
		};
	}
	
	private String customSql;

	@Override
	protected List findData() {
		if(customSql!=null)
			return ServiceLocator2.getJdbcTemplate().queryForList(customSql);
		return ServiceLocator2.getJdbcTemplate().queryForList(getSql(), getDefaultParameters());
	}


	public Map<String, String> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, String> filters) {
		this.filters = filters;
	}

	public void replicar(){
		if(!getSelected().isEmpty()){
			if(MessageUtils.showConfirmationMessage("Replicar "+getSelected().size()+"  entidades", "Replicación")){
				getHibernateTemplate().execute(new HibernateCallback() {						
					public Object doInHibernate(Session session) throws HibernateException,SQLException {						
						for(int i=0;i<getSelected().size();i++){
							Object o=getSelected().get(i);
							Map<String,Object> row=(Map<String,Object>)o;
							final Serializable id=(Serializable)row.get(getPk());
							Object bean=session.load(entity, id);
							EntityLog log=generarLog(bean);
							doReplicar(log);
							if(i%20==0){
								session.flush();
								session.clear();
							}
						}
						return null;
					}
				});		
			}
		}
	}
	
	
	protected EntityLog generarLog(Object bean){
		try {
			Serializable id=(Serializable)PropertyUtils.getProperty(bean, "id");
			Sucursal sucursal=(Sucursal)PropertyUtils.getProperty(bean, "sucursal");
			EntityLog log=new EntityLog(bean,id,sucursal.getNombre(),EntityLog.Tipo.CAMBIO);
			return log;
		} catch (Exception e) {
			throw new RuntimeException("No se pudo generar EntityLog para la entidad: "+bean+" Causa: "+ExceptionUtils.getRootCauseMessage(e));
		}
		
	}
	
	protected void doReplicar(EntityLog log){
		ServiceLocator2.getReplicaMessageCreator().enviar(log);
	}
	
	
	public void replicaBatch(){
		
	}
	
	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(pk," Debe definir la propiedad primary key pk");
		Assert.notNull(entity," Debe definir la entidad representada por los registros sql");
	}
	
	private String pk;
	private Class entity;


	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}
	
	
	
	

	public Class getEntity() {
		return entity;
	}

	public void setEntity(Class entity) {
		this.entity = entity;
	}

	public JdbcTemplate getJdbcTemplate(){
		return ServiceLocator2.getJdbcTemplate();
	}
	
	public HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}

	public String getCustomSql() {
		return customSql;
	}

	public void setCustomSql(String customSql) {
		this.customSql = customSql;
	}
	
	
	
}
