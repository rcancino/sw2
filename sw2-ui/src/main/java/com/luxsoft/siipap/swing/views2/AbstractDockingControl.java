package com.luxsoft.siipap.swing.views2;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.luxsoft.siipap.swing.controls.ViewControl;
import com.luxsoft.siipap.swing.views2.DockingConstants;

public abstract class AbstractDockingControl implements ViewControl
	,BeanNameAware,Ordered, AncestorListener,ResourceLoaderAware{

	private String name;
	private String title;
	private String iconPath;
	private int order;
	protected Logger logger=Logger.getLogger(getClass());

	public void setBeanName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}	
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
	

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}



	protected JComponent content;
	

	public JComponent getControl() {
		if(content==null){
			content=buildContent();
			content.putClientProperty(DockingConstants.DOCKING_ID_KEY, content.hashCode());
			content.putClientProperty(DockingConstants.DOCKING_TITLE_KEY, getTitle());
			Icon icon=getIcon(getIconPath());
			content.putClientProperty(DockingConstants.DOCKING_ICON_KEY, icon);
			content.addAncestorListener(this);
		}
		return content;
	}
	
	private ResourceLoader resourceLoader;
	
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader=resourceLoader;
		
	}
	
	public Icon getIcon(String location){
		if(StringUtils.isBlank(location))
			return null;
		Resource r=resourceLoader.getResource(location);
		try {
			ImageIcon icon=new ImageIcon(r.getURL());
			return icon;
		} catch (IOException e) {			
			logger.error("Error: "+e.getMessage());
			return null;
		}
	}

	public abstract JComponent buildContent();
	
	protected void open(){
		logger.info("Panel opened:" +getName());
	}
	
	protected void closed(){
		logger.info("Panel closed:" +getName());
	}
	
	public void ancestorAdded(AncestorEvent event) {
		open();
	}

	public void ancestorMoved(AncestorEvent event) {}

	public void ancestorRemoved(AncestorEvent event) {
		closed();
	}
}
