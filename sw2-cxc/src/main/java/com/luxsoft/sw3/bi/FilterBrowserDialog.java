package com.luxsoft.sw3.bi;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.jdesktop.swingx.VerticalLayout;
import org.springframework.util.Assert;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;

public class FilterBrowserDialog extends SXAbstractDialog{
	
	private  FilteredBrowserPanel browser;
	
	public FilterBrowserDialog(FilteredBrowserPanel browser) {
		super(browser.getTitle());
		this.browser = browser;
	}
	
	public FilterBrowserDialog(String title, FilteredBrowserPanel browser) {
		super(title);
		this.browser = browser;
	}

	public FilterBrowserDialog(Dialog owner) {
		super(owner);
	}

	@Override
	protected JComponent buildContent() {
		Assert.notNull(getBrowser(),"No se tiene asignado un browser");
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(getBrowser().getControl(),BorderLayout.CENTER);
		
		JPanel leftPanel=new JPanel(new VerticalLayout());
		leftPanel.add(getBrowser().getFilterPanel());
		leftPanel.add(new JSeparator());
		JComponent processPane=createProcessPanel();
		if(processPane!=null)
			leftPanel.add(processPane);
		//final JComponent totalesPanel=getTotalesPanel();
		//if(totalesPanel!=null)
			//leftPanel.add(totalesPanel);
		panel.add(leftPanel,BorderLayout.WEST);
		
		ToolBarBuilder toolbar=new ToolBarBuilder();
		for(Action a:getBrowser().getActions()){
			toolbar.add(a);
			toolbar.add(getBrowser().getPeriodoLabel());
		}
		
		panel.add(toolbar.getToolBar(),BorderLayout.NORTH);
		panel.setPreferredSize(getDimension());
		return panel;
	}
	
	private JComponent createProcessPanel(){
		if(browser.getProccessActions()!=null){
			if(!browser.getProccessActions().isEmpty()){
				final FormLayout layout=new FormLayout("l:p","");
				DefaultFormBuilder builder=new DefaultFormBuilder(layout);
				builder.appendSeparator("Reportes");
				builder.getPanel().setOpaque(false);
				for(Object o:browser.getProccessActions()){
					Action a=(Action)o;
					//a.putValue(Action.SMALL_ICON,CommandUtils.getIconFromResource("images2/overlays.png"));
					builder.append(new ActionLabel(a));
				}
				return builder.getPanel();
			}
		}
		return null;
	}
	
	
	
	/**
	 * Template method para fijar el tamaño del dialog
	 * @return
	 */
	public Dimension getDimension(){
		return new Dimension(850,650);
	}

	public FilteredBrowserPanel getBrowser() {
		return browser;
	}

	public void setBrowser(FilteredBrowserPanel browser) {
		this.browser = browser;
	}
	
	
	public Object getValueFromBrowser(String property){
		try {
			return PropertyUtils.getProperty(browser, property);
		} catch (Exception e) {				
			e.printStackTrace();
			return null;
		} 
	}
	/*
	public JComponent getTotalesPanel(){
		Object res=getValueFromBrowser("totalesPanel");
		return (JComponent)res;
	}*/

	@Override
	protected void onWindowOpened() {
		//System.out.println("Abriendo");
		getBrowser().open();
	}

	@Override
	public void close() {
		getBrowser().close();
		super.close();
	}
	

}
