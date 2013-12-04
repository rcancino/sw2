package com.luxsoft.siipap.swing;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;


/**
 * Interface for locating resources
 * 
 * @author Ruben Cancino
 *
 */
public interface ResourceLocator {
	
	/**
     * Return an <code>ImageIcon</code> using its <code>String</code> key.
     * 
     * @param key
     *            a key for the icon.
     * @return The image icon or null if an icono with the key is not found.
     * 
     */
    public Icon getIcon(String key);
    
    /**
     * Loads the image with the specified key. 
     * 
     * @param key
     *       The image key
     * @return The image or null if an image with the key is not found.
     */
    public Image getImage(String key);
    
    
    /**
     * Retrieves and answers a <code>String</code> for the given key from the
     * bundle. Logs a warning if the resource is missing.
     * 
     * @param key   the key used to lookup the localized string
     * @return the localized text or null if a message is not found under that key
     */
    public String getMessage(String key);
    
    /**
     * Retrieves and answers a <code>String</code> for the given key from the
     * bundle. Returns the default text if the resource is missing.
     * 
     * @param key           the key used to lookup the localized string
     * @param defaultText   a fallback text if the resource is missing
     * @return the localized text or default text or defaultText if a message is not found
     */
    public String getMessage(String key, String defaultText);
    
    /**
     * Regresa un URL
     * 
     * @param path
     * @return
     */
    public URL getURL(String path);

}
