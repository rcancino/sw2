package com.luxsoft.siipap.swing;

import java.awt.Image;

import javax.swing.Icon;

/**
 * JavaBean tha implements  VisualElement for use as a delegation object
 *  
 * @author Ruben Cancino
 *
 */
public class VisualElementSupport implements VisualElement{
	
	private String label="NA";
	private String tooltip;
	private String description="NA";
	private Icon icon;
	private Image image;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Icon getIcon() {
		return icon;
	}
	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getTooltip() {
		return tooltip;
	}
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}	
	
}
