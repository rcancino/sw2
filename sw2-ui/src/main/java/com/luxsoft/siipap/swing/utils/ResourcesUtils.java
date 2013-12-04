/*
 * Copyright (c) 2002-2005 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * This software is the proprietary information of Karsten Lentzsch.  
 * Use is subject to license terms.
 *
 */
 
package com.luxsoft.siipap.swing.utils;

import java.awt.Image;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.swing.Application;





public final class ResourcesUtils {
	
    private ResourcesUtils() {
        // Overrides default constructor; prevents instantiation.
    }
    
    
	// IDs ********************************************************************
	
	public static final String DESCRIPTION_NAME_ID	    = "description.name";
	public static final String DESCRIPTION_ICON_ID	    = "description.icon";
	
	public static final String APPLICATION_IMAGE		= "application.image";
	public static final String APPLICATION_LOGO			= "application.logo.image";
	public static final String HELP_CONTENTS_ICON_ID	= "helpContents.icon";
	public static final String ARROW_ICON_ID			= "arrow.icon";
	
	
	
	// Resources **************************************************************
	
	public static final String DESCRIPTION_NAME 	= getString(DESCRIPTION_NAME_ID);
	public static final Icon   DESCRIPTION_ICON 	= getIcon  (DESCRIPTION_ICON_ID);
	
	private static Logger logger=Logger.getLogger(ResourcesUtils.class);
	

	// Helper Code ************************************************************
	
	private static Icon getIcon(String id) {
        if(Application.isLoaded()){
        	return Application.instance().getIcon(id);
        }
        return null;
    }


    private static String getString(String id) {
    	if(Application.isLoaded()){
        	return Application.instance().getMessage(id,id.toUpperCase());
        }
        return null;
    }
    
    public static Icon getIconFromResource(String path){
		try {
			ClassLoader cl=ResourcesUtils.class.getClassLoader();
			URL url=cl.getResource(path);
			Icon icon=new ImageIcon(url);
			return icon;
		} catch (Exception e) {
			logger.info("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}
    
    public static Image getImageFromResource(String path){
		try {
			ClassLoader cl=ResourcesUtils.class.getClassLoader();
			URL url=cl.getResource(path);			
			return ImageIO.read(url);
		} catch (Exception e) {
			logger.info("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}
	
}
