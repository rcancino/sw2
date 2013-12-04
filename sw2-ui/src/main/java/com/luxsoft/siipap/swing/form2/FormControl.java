package com.luxsoft.siipap.swing.form2;

import javax.swing.JComponent;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;



public class FormControl {
	
	private transient JComponent control;
	private String labelKey;
	private String property;
	private String label;
	
	
	public FormControl(JComponent control, String property) {
		this(property,control,property);
	}

	public FormControl(String property,JComponent control, String label) {
		this.labelKey = label;
		this.control = control;
		control.setName(property);
		this.property = property;
		this.label=labelKey;
	}
	
	public JComponent getControl() {
		return control;
	}
	public void setControl(JComponent control) {
		this.control = control;
	}
	
	public String getLabelKey() {
		return labelKey;
	}
	public void setLabelKey(String label) {
		this.labelKey = label;
	}
	
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE,false);
	}
	
	
}
