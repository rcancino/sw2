package com.luxsoft.siipap.swing.form2;

import java.awt.BorderLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.ClassUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.view.ValidationComponentUtils;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;

/**
 * Mantenimiento al catalogo de clientes
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractForm extends SXAbstractDialog{
	
	
	
	private Map<String, JComponent> components=new HashMap<String, JComponent>();
	protected final IFormModel model;
	private BindingFactory factory;
 
	public AbstractForm(final IFormModel model) {
		super("");
		this.model=model;
		model.getValidationModel().addPropertyChangeListener(ValidationResultModel.PROPERTYNAME_RESULT,new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				updateComponentTreeMandatoryAndSeverity();
			}			
		});
	}
	
	protected JComponent buildContentPane() {
		JComponent content=super.buildContentPane();
		afterContentCreated(content);
		return content;
	}

	@Override
	protected  JComponent buildContent() {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildMainPanel(),BorderLayout.CENTER);
		if(model.isReadOnly()){
			panel.add(buildButtonBarWithClose(),BorderLayout.SOUTH);			
		}else
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}
	
	/**
	 * Hook para hacer algo posterior al la creacion del panel principal 
	 */
	protected void afterContentCreated(JComponent content){
		
	}
	
	/**
	 * Creea el panel principal. Normalmente no requiere ser modificado
	 * 
	 * @return
	 */
	protected JComponent buildMainPanel(){
		final FormLayout layout=new FormLayout("p:g","p,2dlu,40dlu");
		final PanelBuilder builder=new PanelBuilder(layout);
		final CellConstraints cc=new CellConstraints();
		builder.add(buildFormPanel(),cc.xy(1, 1));
		if(!model.isReadOnly())
			builder.add(buildValidationPanel(),cc.xy(1,3));
		model.validate();
		updateComponentTreeMandatoryAndSeverity(builder.getPanel());
		return builder.getPanel();
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	protected abstract JComponent buildFormPanel();
	
	protected JComponent getControl(final String property){
		JComponent c=components.get(property);
		if(c==null){
			c=createNewComponent(property);	
			/*System.out.println( "EL VALOR DE " +property  +" ES ....."  +components.get("property"));
			if (property=="cobrador")
					{
				components.put(property,new JTextField("1"));
				
				System.out.println("SI ENTRO AL IF ENTONCES ES OTRA COSA" + components.get("property"));
					}*/
				
			components.put(property, c);
			
		}
		return c;
	}
	
	protected JComponent createNewComponent(final String property){
		JComponent c=createCustomComponent(property);
		if(c==null){
			FormControl fc=getFactory().getFormControl(property, model);
			fc.setLabelKey(getFullPath(model.getBaseBeanClass(), property));
			ValidationComponentUtils.setMessageKey(fc.getControl(), fc.getLabelKey());
			return fc.getControl();
		}else{
			FormControl fc=new FormControl(c,property);	
			fc.setLabelKey(getFullPath(model.getBaseBeanClass(), property));
			ValidationComponentUtils.setMessageKey(fc.getControl(), fc.getLabelKey());
			return c;
		}
	}
	
	protected JComponent addReadOnly(String property){
		JComponent c=getControl(property);
		c.setEnabled(false);
		c.setFocusable(false);
		c.setFont(c.getFont().deriveFont(Font.BOLD));
		if(c instanceof JTextField){
			((JTextField)c).setEditable(false);
			c.setEnabled(true);
		}
		return c;
	}
	
	/**
	 * Regresa un binding en JLabel. Se asume que la propiedad es de solol lectura
	 * y es de tipo String
	 * @param property
	 * @return
	 */
	protected JLabel addAsLabel(String property){
		JLabel label=BasicComponentFactory.createLabel(model.getModel(property));		
		return label;
	}
	
	protected JComponent addMandatory(String property){
		ValidationComponentUtils.setMandatory(getControl(property),true);
		return getControl(property);
	}
	
	protected void setMandatory(String property){
		ValidationComponentUtils.setMandatory(getControl(property),true);
	}
	
	/**
	 * Template method para crear los controles de forma manual
	 * 
	 * @param property
	 * @return
	 */
	protected JComponent createCustomComponent(final String property){
		return null;
	}
	
	
	
	@Override
	protected void onWindowOpened() {
		updateComponentTreeMandatoryAndSeverity();
	}
	
	
	 protected void updateComponentTreeMandatoryAndSeverity(JPanel panel) {
		 ValidationComponentUtils.updateComponentTreeMandatoryAndBlankBackground(panel);
	     ValidationComponentUtils.updateComponentTreeSeverityBackground(panel,model.getValidationModel().getResult());
	     getOKAction().setEnabled(!model.getValidationModel().hasErrors());
	 }
	 
	 protected void updateComponentTreeMandatoryAndSeverity() {
		 ValidationComponentUtils.updateComponentTreeMandatoryAndBlankBackground(getContentPane());
	     ValidationComponentUtils.updateComponentTreeSeverityBackground(getContentPane(),model.getValidationModel().getResult());
	     getOKAction().setEnabled(!model.getValidationModel().hasErrors());
	 }
	 
	 public BindingFactory getFactory(){
			if(factory==null)
				factory=new BindingFactoryImpl();
			return factory;
	}
	 
	protected JComponent buildValidationPanel(){
		JComponent c=ValidationResultViewFactory.createReportList(model.getValidationModel());
		return c;
	}
	
	public IFormModel getModel(){
		return model;
	}
	
	protected Action insertAction;
	protected Action deleteAction;
	protected Action editAction;
	protected Action viewAction;
	
	
	protected Action getInsertAction(){
		if(insertAction==null){
			insertAction=CommandUtils.createInsertAction(this, "insertPartida");
		}
		return insertAction;
	}
	
	protected Action getDeleteAction(){
		if(deleteAction==null){
			deleteAction=CommandUtils.createDeleteAction(this, "deletePartida");
		}
		return deleteAction;
	}
	
	protected Action getEditAction(){
		if(editAction==null){
			editAction=CommandUtils.createEditAction(this, "edit");
		}
		return editAction;
	}
	
	protected Action getViewAction(){
		if(viewAction==null){
			viewAction=CommandUtils.createViewAction(this, "view");
		}
		return viewAction;
	}
	
	
	
	/**
	 * HeaderPanel 
	 * 
	 */
	protected JComponent buildHeader() {
		return null;
	}

	
	public static void showObject(Object bean){
		System.out.println(ToStringBuilder.reflectionToString(bean,ToStringStyle.MULTI_LINE_STYLE,false));
	}
	
	protected String getFullPath(final Class clazz,final String property){
		return ClassUtils.getShortName(clazz)+"."+property;
	}

}
