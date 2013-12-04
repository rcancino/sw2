package com.luxsoft.siipap.swing;

import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.ActionManager;

/**
 * This class is the core of the framework. A singleton that provides access to 
 * localized/branded/plataform-specific resources, persistent session state
 * ,Application configuration, Application description and Actions
 * 
 * 
 * This class is the application central singleton facade. It references the application's
 * main collaborators. 
 * It is mandatory to properly initialize this class during 
 * startup process. All dependencies are injected by spring IoC
 * 
 * Must of the time this class acts as a singleton facade in which  services are delegated to other
 * components.
 * The one operation assigned to this component is to close the application for which fires events through the
 * ApplicationContext
 * 
 * TODO Correct docmentation
 * @author Ruben Cancino
 *
 */
public class Application implements ApplicationContextAware,InitializingBean{
	
	/**
	 * The Application singleton instance
	 */
	private static Application SINGLETON;
	
	/**
	 * The main application window
	 */
	private ApplicationWindow mainWindow;
	
    /**
     * Spring ApplicationContext instance
     * 
     */
	private ApplicationContext applicationContext;
	
	/**
	 * Instanco holder of ResourceLocator
	 */
	private ResourceLocator resourceLocator;	
	
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Preferences globalPreferences;
	
	private Preferences userPreferences;
	
	private String userPreferencesRootName="luxsoft.swing.user";
	
	private ActionManager actionManager;
	
	/**
	 * Default consotructor 
	 *
	 */
	private  Application(){
		
	}
	
	/**
     * Return the single application instance.
     * 
     * @return The application
     */
	public static synchronized Application instance(){
		if(SINGLETON==null){
			SINGLETON=new Application();
		}
		return SINGLETON;
	}
	
	/**
	 * Returns true if the singleton instance has been initialized
	 * 
	 * @return
	 */
	public static synchronized boolean isLoaded(){		
		return SINGLETON!=null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {		
		this.applicationContext=applicationContext;
		
	}
	
	/**
     * Retrieves and answers a <code>String</code> for the given key from the
     * resource locator. Returns the default text if the resource is missing.
     * 
     * @param key           the key used to lookup the localized string
     * @param defaultText   a fallback text if the resource is missing
     * @return the localized text or default text
     */
    public String getMessage(String key, String defaultText){
    	return getResourceLocator().getMessage(key,defaultText);
    }
    
    /**
     * Return an <code>ImageIcon</code> using its <code>String</code> key.
     * @param key
     * @return
     */
    public Icon getIcon(String key){
    	return getResourceLocator().getIcon(key);
    }
    
    public Icon getIconFromPath(String path){
    	Image img=getImageFromPath(path);
    	if(img!=null)
    		return new ImageIcon(img);
    	return null;
    }
    
    public Image getImageFromPath(String path){
    	Resource r=getApplicationContext().getResource(path);
    	if(r.exists()){
    		try {
    			URL url=r.getURL();
    			return ImageIO.read(url);
    		} catch (Exception e) {
    			logger.error(e);
    			return null;
    		}
    	}
    	return null;
    }
    
    /**
     * Opens the main window 
     *
     */
    public void open(){
    	fireApplicationStarting();
    	getMainWindow().open();
    }
    
    /**
     * Does everything necessary to close the application:
     * fires an event that indicates that the application is closing,
     * disposes all frames and their owned windows, 
     * fires an application closed event,
     * and finally exits the system.
     *
     */
    public  void close() {
    	try {
    		fireApplicationClosing();
            
            Frame[] frames = Frame.getFrames();
            for (int i = 0; i < frames.length; i++) {
                Frame frame = frames[i];
                if (frame instanceof JFrame) {
                    JFrame jframe = (JFrame) frame;
                    Window[] ownedWindows = jframe.getOwnedWindows();
                    for (int j = 0; j < ownedWindows.length; j++) {
                        ownedWindows[j].dispose();
                    }
                }
                frame.dispose();
            }        
            fireApplicationClosed();
            closeServiceLayer();
            System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			System.exit(-1);
		}
        
    }
    
    public void closeServiceLayer(){
    	ServiceLocator2.close();
    }
    
    /**
     * Returns the user preferences.
     * 
     * @return the user preferences
     */
    public  Preferences getUserPreferences() {
    	if(userPreferences==null){
			userPreferences=Preferences.userRoot().node(getUserPreferencesRootName());
		}
		return userPreferences;
    }
    
    public Preferences getGlobalPreferences(){
    	if(globalPreferences==null){
			globalPreferences=Preferences.systemRoot();
		}
		return globalPreferences;
    }
    
        
    /**
     * Ruta para almacenar las preferencias del usuario, en Windows se almacenan en el Registry
     * 
     * @return
     */
    public String getUserPreferencesRootName() {
		return userPreferencesRootName;
	}
	public void setUserPreferencesRootName(String userPreferencesRootName) {
		this.userPreferencesRootName = userPreferencesRootName;
	}
	
	
	
	public ActionManager getActionManager() {
		if(actionManager==null){
			actionManager=(ActionManager)applicationContext.getBean("actionManager");
		}
		return actionManager;
	}

	/**
     * Propagates an Application closing event through the ApplicationContext
     *
     */
    protected void fireApplicationStarting(){ 
    	if(logger.isDebugEnabled()){
    		logger.debug("Disparando evento: "+EventType.APPLICATION_STARTING.name());
    	}
    	getApplicationContext().publishEvent(SwingApplicationEvent.getEvent(this,EventType.APPLICATION_STARTING));
    }

	/**
     * Propagates an Application closing event through the ApplicationContext
     *
     */
    protected void fireApplicationClosing(){  
    	if(logger.isDebugEnabled()){
    		logger.debug("Disparando evento: "+EventType.APPLICATION_CLOSING.name());
    	}
    	getApplicationContext().getBeanDefinitionCount();
    	getApplicationContext().publishEvent(SwingApplicationEvent.getEvent(this,EventType.APPLICATION_CLOSING));
    }
    
    /**
     * Propagates an Application closed event through the ApplicationContext
     *
     */
    protected void fireApplicationClosed(){  
    	if(logger.isDebugEnabled()){
    		logger.debug("Disparando evento: "+EventType.APPLICATION_CLOSED.name());
    	}
    	getApplicationContext().publishEvent(SwingApplicationEvent.getEvent(this,EventType.APPLICATION_CLOSED));
    	
    }
    
    /******************************** Collaborators injected by Spring IoC ************************/
    
	/**
	 * UI Acces poin to the Spring ApplicationContext
	 * 
	 * @return
	 */
	public ApplicationContext getApplicationContext(){
		//Assert.state(isLoaded(),"The global  application instance must be initialized first.");
		return applicationContext;
	}
	
	public Page getMainPage(){
		if(getMainWindow()==null) return null;
		return getMainWindow().getWindowPage();
	}
	

	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}
	
	

	/**
     * Returns a frame that is intended to be used as default parent,
     * for example when opening a notifier that has no indiviual parent.
     * 
     * Normally this dependency has to be injected by Spring IoC
     * 
     * @return the application's default parent frame
     */
    public  JFrame getMainFrame() {
        return getMainWindow().getWindow();
    }
    

	public ApplicationWindow getMainWindow() {
		return mainWindow;
	}

	public void setMainWindow(ApplicationWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void afterPropertiesSet() throws Exception {
		logger.info("Cheking Application mandatory dependencies...");
		Assert.notNull(getMainPage(),"Application can't operate without a Page");
		Assert.notNull(getMainWindow(),"Application can't operate without an ApplicationWindow");
		Assert.notNull(getApplicationContext(),"Application can't operate without an ApplicationContext");
		logger.info("Dependencias del objeto Application ..OK...");
		
	}
	
	
	
	private String version="N/A";
	private String name="Siipap Swing";



	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	 

}
