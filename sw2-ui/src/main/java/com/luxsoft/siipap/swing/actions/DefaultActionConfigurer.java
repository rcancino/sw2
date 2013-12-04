package com.luxsoft.siipap.swing.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import com.luxsoft.siipap.swing.ResourceLocator;



/**
 * Implementacion de ActionConfigurer que delega la ejecu
 * 
 * @author Ruben Cancino
 *
 */
public class DefaultActionConfigurer implements ActionConfigurer{
	
	
	private Logger logger;
	private ResourceLocator resourceLocator;
	
	public DefaultActionConfigurer(){
		logger=Logger.getLogger(this.getClass());
	}


	/**
	 * Constructor principalmente para propositos de Test
	 * @param resourceLocator
	 */
	public DefaultActionConfigurer(ResourceLocator resourceLocator) {		
		this();
		this.resourceLocator = resourceLocator;
	}
	
	protected void configureSmallIcon(Action action, String id){
		String key=id+"."+SMALL_ICON;	
		Icon icon=getIcon(key);		
		action.putValue(Action.SMALL_ICON,icon);
		if(logger.isDebugEnabled()){
			String res=icon!=null?"OK":" NOT FOUND";
			logger.debug("\t Icon for action: "+key+" : "+res);
		}
	}
	
	protected void configureGrayIcon(Action action, String id){
		String key=id+"."+GRAY_ICON;
		action.putValue(GRAY_ICON,getIcon(key));
	}
	
	protected Icon getIcon(String key){
		return getResourceLocator().getIcon(key);
	}
	
	/**
	 * Configura AcceleratorKey para la accion
	 * 
	 * @param action
	 * @param id
	 */
	protected void configureAccelerator(Action action,String id){
		
		String key=id+"."+ACCELERATOR;
		String acceleratorKey = getString(key, null);
		if(acceleratorKey!=null){
			KeyStroke keyStroke = KeyStroke.getKeyStroke(acceleratorKey);
			action.putValue(Action.ACCELERATOR_KEY,keyStroke);
			if(logger.isInfoEnabled()&& keyStroke==null){
	        	logger.info("Action: "+key+" has an invalid accelerator: "+acceleratorKey);
	        }
			if(logger.isDebugEnabled()){
				logger.debug("\t Accelerator for action: "+key+" : "+keyStroke);
			}
		}
	}


	public void configure(final Action action,final String id) {
		
		//Configurando la etiqueta (Nombre)
		String key=id+"."+LABEL;
		String nameWithMnemonic=getString(key,key);		
		int index = nameWithMnemonic.indexOf(MNEMONIC_MARKER);
		String name = stripName(nameWithMnemonic, index);
		if(logger.isDebugEnabled()){
			logger.debug("\t Name to Action "+key+" "+": "+name);
		}
		//Configurando descripciones
		key=id+"."+SHORT_DESCRIPTION;
		String shortDescription = getString(key,key);
		if(logger.isDebugEnabled()){
			logger.debug("\t Tooltip to Action "+key+" "+": "+shortDescription);
		}
		
		key=id+"."+LONG_DESCRIPTION;
		String longDescription = getString(key,key);
		if(logger.isDebugEnabled()){
			logger.debug("\t Description to Action "+key+" "+": "+longDescription);
		}
		
		//Put the  values in the Action.
        action.putValue(Action.NAME,name);
        action.putValue(Action.SHORT_DESCRIPTION,      shortDescription);
        action.putValue(Action.LONG_DESCRIPTION,       longDescription);
        
        if(index!=-1){
        	int nm=nameWithMnemonic.charAt(index + 1);
        	Integer mnemonic=new Integer(nm);
        	action.putValue(Action.MNEMONIC_KEY,mnemonic);
        	if(logger.isDebugEnabled()){
        		logger.debug("\t Mnemonic for action "+id+": "+(char)nm);
        	}
        }
        configureSmallIcon(action,id);
        configureAccelerator(action,id);
	}
	
	private String getString(String key,String def){		
		String s=getResourceLocator().getMessage(key,def);		
		return s;
	}
	
	private static String stripName(String nameWithMnemonic, int aMnemonicIndex) {
        return aMnemonicIndex == -1
            ? nameWithMnemonic
            : nameWithMnemonic.substring(0, aMnemonicIndex)
                + nameWithMnemonic.substring(aMnemonicIndex + 1);
    }
	
	

	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}
	
	

}
