package com.luxsoft.siipap.swing.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.springframework.util.StringUtils;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.UIFactory;
import com.luxsoft.siipap.swing.ResourceLocator;

public class Header { 
	
	private String titulo="App sin Titulo";
	private String descripcion="App sin descripcion";
	private String iconPath="App sin icono";
	private int tituloSize=7;
	private float descSize=12f;
	private CustomHeader header;
	private ResourceLocator resourceLocator;
	private int descRows=2;	


	public Header() {		
	}
	
	

	public Header(String titulo, String descripcion) {
		super();
		this.titulo = titulo;
		this.descripcion = descripcion;
	}



	public String getDescripcion() {
		return descripcion;
	}



	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
		if(header!=null)
			getHeader().setDescription(descripcion);
	}



	public String getIconPath() {
		return iconPath;
	}
	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}


	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
		if(header!=null)
			getHeader().setTitle(titulo);
	}
	
	

	public float getDescSize() {
		return descSize;
	}

	public void setDescSize(float descSize) {
		this.descSize = descSize;		
	}

	public int getTituloSize() {
		return tituloSize;
	}

	public void setTituloSize(int tituloSize) {
		this.tituloSize = tituloSize;
	}
	
	public CustomHeader getHeader(){
		if(header==null){
			header=new CustomHeader(getTitulo(),getDescripcion(),getIcon());
		}
		return header;
	}
	
	protected Icon getIcon(){
		if(getResourceLocator()!=null && StringUtils.hasText(getIconPath())){
			final URL url=getResourceLocator().getURL(getIconPath());
			if(url!=null){
				try {
					final BufferedImage bi=ImageIO.read(url);
					final Image im2=bi.getScaledInstance(-40, 50,Image.SCALE_SMOOTH);
					return new ImageIcon(im2);
				} catch (Exception e) {			}
				
			}			
		}
		return null;
	}
	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}



	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	public int getDescRows() {
		return descRows;
	}
	public void setDescRows(int descRows) {
		this.descRows = descRows;
	}
	
	public void onLoad(){
		
	}
	
	public  class CustomHeader extends HeaderPanel {

		public CustomHeader(String title, String description, Icon icon,
				ConstantSize minimumWidth, ConstantSize minimumHeight) {
			super(title, description, icon, minimumWidth, minimumHeight);
		}

		public CustomHeader(String title, String description, Icon icon,
				JComponent backgroundComponent, ConstantSize minimumWidth,
				ConstantSize minimumHeight) {
			super(title, description, icon, backgroundComponent, minimumWidth,
					minimumHeight);
		}

		public CustomHeader(String title, String description, Icon icon) {
			super(title, description, icon);
		}

		public CustomHeader(String title, String description) {
			super(title, description);
		}

		protected JLabel createTitleLabel() {
			return UIFactory.createBoldLabel("", getTituloSize(), UIManager
					.getColor("TitledBorder.titleColor"));
		}

		protected JTextComponent createDescriptionArea() {
			JTextArea area = UIFactory.createWrappedMultilineLabel("");
			area.setLineWrap(false);
			Font f = area.getFont();
			area.setFont(f.deriveFont(Font.BOLD, getDescSize()));
			area.setForeground(Color.DARK_GRAY.brighter());			
			area.setRows(getDescRows());
			return area;
		}
		@Override
		public void addNotify() {
			super.addNotify();
			onLoad();
		}
		

	}
	

}
