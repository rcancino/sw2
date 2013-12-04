package com.luxsoft.siipap.swing;

import java.awt.Image;

import javax.swing.Icon;

public interface VisualElement {
	
	
	public String getLabel();
	public void setLabel(String label);
	
	public String getTooltip();
	public void setTooltip(String tooltip);
	
	public String getDescription();
	public void setDescription(String description);
	
	public Icon getIcon();
	public void setIcon(Icon icon);
	
	public Image getImage();
	public void setImage(Image image);
	
	public enum Type{
		
		LABEL,
		TOOLTIP,
		ICON,
		DESCRIPTION,
		IMAGE;
	}

}
