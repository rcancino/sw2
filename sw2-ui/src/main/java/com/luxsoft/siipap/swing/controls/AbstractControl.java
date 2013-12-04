package com.luxsoft.siipap.swing.controls;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;


public abstract class AbstractControl implements ViewControl{
	
	
    private JComponent panel;
    protected Logger logger=Logger.getLogger(getClass());

    
    
    public synchronized JComponent getControl() {
        if (panel == null) {
            panel = buildContent();
        }
        return panel;
    }
    
    public Icon getIconFromResource(String path){
		try {
			ClassLoader cl=getClass().getClassLoader();
			URL url=cl.getResource(path);
			Icon icon=new ImageIcon(url);
			return icon;
		} catch (Exception e) {
			logger.info("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			System.out.println("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}
    
    public BufferedImage getImageFromResource(String path){
		try {
			ClassLoader cl=getClass().getClassLoader();
			URL url=cl.getResource(path);
			return ImageIO.read(url);
		} catch (Exception e) {
			logger.info("No pudo cargar image: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}



    protected abstract JComponent buildContent();
    
    /**
     * Verifica si el control ya se ha inicializado
     * 
     * @return
     */
    protected boolean isInitialized(){
    	return panel!=null;
    }

}
