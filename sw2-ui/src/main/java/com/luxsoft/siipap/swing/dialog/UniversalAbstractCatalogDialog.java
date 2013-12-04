package com.luxsoft.siipap.swing.dialog;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.BeanWrapperImpl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Implementacion de {@link AbstractCatalogDialog} que utiliza UniversalDao para
 * como medio de acceso a la base de datis
 * 
 * 
 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public   class UniversalAbstractCatalogDialog<T> extends AbstractCatalogDialog<T> {
	
	private TableFormat<T> tableFormat;

	public UniversalAbstractCatalogDialog(Class<T> baseClass, EventList<T> source, String title, String header, String headerDesc) {
		super(baseClass, source, title, header, headerDesc);
	}

	public UniversalAbstractCatalogDialog(Class<T> baseClass, EventList<T> source, String title) {
		super(baseClass, source, title);		
	}

	public UniversalAbstractCatalogDialog(Class<T> baseClass, String title) {
		super(baseClass, title);
	}

	@Override
	protected List<T> getData() {
		return getDao().getAll(getBaseClass());
	}
	
	@Override
	protected TableFormat<T> getTableFormat() {		
		return tableFormat;
	}
	public void setTableFormat(TableFormat<T> tableFormat) {
		this.tableFormat = tableFormat;
	}

	private UniversalDao getDao(){
		return ServiceLocator2.getUniversalDao();
	}
	
	protected T save(T bean){
		return (T) ServiceLocator2.getUniversalDao().save(bean);
	}

	@Override
	protected boolean doDelete(T bean) {
		BeanWrapperImpl wrapper=new BeanWrapperImpl(bean);
		ServiceLocator2.getUniversalDao().remove(getBaseClass(), (Serializable)wrapper.getPropertyValue("id"));
		return true;
	}
	
	
	
	

}
