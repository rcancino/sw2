package com.luxsoft.siipap.swing.impl;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.luxsoft.siipap.swing.ResourceLocator;





/**
 * Default implementation of ResourceLocator that uses an Spring ApplicationContext
 * to resolve resources
 * 
 * Tambien puede ser usuado stand allone fijando sus propiedades resourceLoader y messageSource
 * 
 * @author Ruben Cancino
 *
 */
public class ResourceLocatorImpl implements ResourceLocator,ApplicationContextAware{
	
	
	private ResourceLoader resourceLoader;
	private MessageSource messageSource;
	
	private Logger logger=Logger.getLogger(getClass());
	
	

	public Icon getIcon(String key) {
		Image img=getImage(key);
		if(img!=null){
			return new ImageIcon(img);
		}
		return null;
	}

	public Image getImage(String key) {
		String path=getMessage(key);
		if(path!=null){
			final URL url=getURL(path);
			if(url!=null){
				Image img= createImage(url);				
				logger.debug("Image loaded: "+path);
				return img;
			}
		}
		logger.debug("Unable to find Image for key: "+key);
		return null;
	}
	
	public URL getURL(final String path){
		
		Resource resource=getResourceLoader().getResource(path);
		if(resource.exists()){
			try{
				return resource.getURL();
			}catch(IOException e){
				logger.error("Exception reading url: "+path,e);
			}
		}
		logger.debug("Unable to find URL for path: "+path);
		return null;
	}
	
	private Image createImage(URL url){
		try{
			Image img=ImageIO.read(url);
			return img;
		}catch(IOException e){
			logger.error("Exception reading image from URL: "+url.getPath(),e);
			return null;
		}
	}

	public String getMessage(String key) {
		return getMessage(key,null);
	}

	public String getMessage(String key, String defaultText) {
		String msg=getMessageSource().getMessage(key,null,defaultText,Locale.getDefault());
		
		logger.debug("Message Key: "+key+" resolved to: "+msg);
		return msg;
	}
	

	public void setApplicationContext(ApplicationContext applicationContext) {		
		setResourceLoader(applicationContext);
		setMessageSource(applicationContext);
	}
	
	public String toString(){
		if(getResourceLoader() instanceof ApplicationContext){
			ApplicationContext ctx=(ApplicationContext)getResourceLoader();
			ResourceBundleMessageSource rs=(ResourceBundleMessageSource)ctx.getBean("messageSource");
			return "ResourceLocatorImpl backed by context con messageSource: "+rs.toString();
		}else
			return getResourceLoader().toString();
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	
	

}
