package com.luxsoft.siipap.swing.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.swing.Application;

/**
 * Clase de utilerias para objetos de tipo accion
 * 
 * 
 * @author Ruben Cancino
 *
 */
public final class ActionUtils {
	
	private static Logger logger=Logger.getLogger(ActionUtils.class);
	
	public static Action getNotFoundAction(final String id,final String msg){
		AbstractAction a=new AbstractAction(id){

			public void actionPerformed(ActionEvent e) {
				Component source=null;
				if(Application.isLoaded()){
					source=Application.instance().getMainFrame();
				}
				
				JOptionPane.showMessageDialog(source, msg,"Acción no registrada",JOptionPane.ERROR_MESSAGE);
				
			}
			
		};
		a.putValue(Action.SMALL_ICON, getActionIcon("images/alert/errorwarning_tab.gif"));
		return a;
	}
	
	public static Icon getActionIcon(String path){
		try {
			ClassLoader cl=ActionUtils.class.getClassLoader();
			URL url=cl.getResource(path);
			Icon icon=new ImageIcon(url);
			return icon;
		} catch (Exception e) {
			String msg=MessageFormat.format("No se localizo el icono en la ruta: {o}", path);
			logger.info(msg);
			return null;
		}		
	}

}
