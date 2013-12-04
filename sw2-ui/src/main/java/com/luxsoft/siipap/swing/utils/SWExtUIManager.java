package com.luxsoft.siipap.swing.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.prefs.Preferences;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import com.jgoodies.binding.beans.Model;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
//import com.jgoodies.uif.application.Application;
//import com.jgoodies.uif.laf.ExtUIManager;
import com.jgoodies.uif.laf.ExtUIManager;
import com.jgoodies.uif.laf.LookChoiceStrategies;
import com.jgoodies.uif.laf.LookChoiceStrategy;
//import com.jgoodies.uif.laf.LookConfiguration;
//import com.jgoodies.uif.laf.LookConfigurations;
//import com.jgoodies.uif.laf.LookConfigurations.ConfigurationComparator;

//import com.jgoodies.uif.laf.LookConfigurations;
import com.jgoodies.uif.util.ComponentTreeUtils;
import com.luxsoft.siipap.swing.Application;

/**
 * Modificacion menor a una clase de JGoodies Swing suite para
 * usar el objeto Application de SW en lugar del original, ya que este
 * ultimo utliza Spring IoC
 * 
 * A helper class that configures Swing related look issues:<ol>
 * <li>installs new look and feels.</li>
 * <li>sets system properties,</li>
 * <li>restores a look and feel with an optional theme, and</li>
 * <li>overrides UI defaults.</li>
 * </ol>
 * 
 * @author  Karsten Lentzsch
 * @version $Revision: 1.2 $
 * 
 * @see     LookChoiceStrategy
 * @see     LookChoiceStrategies
 * @see     LookConfiguration
 * @see     LookConfigurations
 * @see     javax.swing.UIManager
 */
@SuppressWarnings("unchecked")
public class SWExtUIManager {
	
	/**
     * A key for the user preferences used to store and restore 
     * the classname of the default Look&amp;Feel.
     * 
     * @see #getDefaultLookClassName()
     * @see #getDefaultLookAndFeel()
     * @see #getDefaultLookConfiguration()
     */
    public static final String DEFAULT_LAF_KEY = "laf.default";

    //private static final Logger LOGGER = Logger.getLogger("ExtUIManager");
    private static final Logger LOGGER=Logger.getLogger(SWExtUIManager.class);

    /**
     * Holds the current LookChoiceStrategy that is used
     * to provide a class name for the default L&amp;F
     * in case there's no default class name stored in the preferences.
     * 
     * @see #getDefaultLookClassName()
     */
    private static LookChoiceStrategy lookChoiceStrategy =
        LookChoiceStrategies.DEFAULT;
    
    /**
     * Holds the lazily created LookConfigurations.
     */
    private static LookConfigurations lookConfigurations;
    
    
    // Instance Creation ******************************************************
    
    private SWExtUIManager() {
        // Override the default constructor - prevents instantiation.
    }

    
    // Public API *************************************************************

    /**
     * Returns the current LookChoiceStrategy.
     * 
     * @return the current LookChoiceStrategy.
     */
    public static LookChoiceStrategy getLookChoiceStrategy() {
        return lookChoiceStrategy;
    }

    /**
     * Sets a LookChoiceStrategy that will be used to lookup
     * the very first look, if no look has been stored.
     * 
     * @param strategy the LookChoiceStrategy used to lookup the very first look
     */
    public static void setLookChoiceStrategy(LookChoiceStrategy strategy) {
        lookChoiceStrategy = strategy;
    }

    /**
     * Returns a clone of the LookConfigurations object which is lazily 
     * initialized with default values.
     * 
     * @return a clone of the LookConfigurations 
     */
    public static LookConfigurations getLookConfigurations() {
        if (lookConfigurations == null) {
            lookConfigurations = LookConfigurations.restoreFrom(Application.instance().getUserPreferences());
        }
        return (LookConfigurations) lookConfigurations.clone();
    }
    
    
    /**
     * Sets a new LookConfigurations object, stores them in the
     * Application's user preferences, sets the look and theme
     * and finally updates all component tree UIs.
     * Does nothing if the new LookConfigurations equals the old configurations
     * 
     * @param newLookConfigurations  the LookConfigurations to set
     */
    public static void setLookConfigurations(LookConfigurations newLookConfigurations) {
        if (newLookConfigurations.equals(lookConfigurations)) {
            return;
        }
        lookConfigurations = newLookConfigurations;
        newLookConfigurations.storeIn(Application.instance().getUserPreferences());
        setLookAndTheme(newLookConfigurations.getDefaultConfiguration());
        ComponentTreeUtils.updateAllUIs();
    }
    
    /**
     * Installs the Plastic L&amp;Fs, enables system fonts, restores
     * the LookConfigurations from the application preferences
     * and finally sets the selected look and theme.<p>
     * 
     * This method shall be called before the any UI components are created.
     * Therefore we don't need to update the component tree UIs.
     * In contrast, if you set a new LookConfigurations object later,
     * we store it in the prefs and update the component tree UIs.
     * 
     * @see #setLookConfigurations(LookConfigurations)
     * @see #setDefaultLookConfiguration(LookConfiguration)
     */
    public static void setup() {
        // Install extra looks
        UIManager.installLookAndFeel("JGoodies Plastic",    Options.PLASTIC_NAME);
        UIManager.installLookAndFeel("JGoodies Plastic 3D", Options.PLASTIC3D_NAME);
        UIManager.installLookAndFeel("JGoodies Plastic XP", Options.PLASTICXP_NAME);
                                     
    	
    	
        UIManager.put("Application.useSystemFontSettings", Boolean.TRUE);

        setLookAndTheme(getDefaultLookConfiguration());
      //  setLookAndTheme("com.sun.java.swing.plaf.gtk.GTKLookAndFeel",null);
    }


    // Default Look & Feel **************************************************
    
    /**
     * Looks up and return the class name of the default Look&amp;Feel 
     * from the user preferences. If none is available returns the
     * default classname as provided by the current LookChoiceStrategy.
     * 
     * @return the classname of the default Look&amp;Feel. 
     */
    public static String getDefaultLookClassName() {
    	if(Application.isLoaded()){
    		return Application.instance().getUserPreferences().get(
                    DEFAULT_LAF_KEY, 
                    getLookChoiceStrategy().getDefaultLookClassName());
    	}else{
    		return getLookChoiceStrategy().getDefaultLookClassName();
    	}
        
    }
    
    
    /**
     * Looks up the classname of the default Look&amp;Feel,
     * then create and returns an instance of this class.
     * Returns the UIManager's current Look&amp;Feel in case the above fails.
     * 
     * @return the default Look&amp;Feel
     */
    public static LookAndFeel getDefaultLookAndFeel() {
        String defaultLafClassName = getDefaultLookClassName();
        LookAndFeel laf = createLookAndFeelInstance(defaultLafClassName);
        if (laf != null) {
            return laf;
        } else {
            LOGGER.warn("Could not create the default L&F " + defaultLafClassName);
            return UIManager.getLookAndFeel();
        }
    }
    
    
    /**
     * Looks up and returns the default LookConfiguration,
     * that is the default Look&amp;Feel plus theme - if any.
     * 
     * @return the default LookConfiguration, i.e. L&amp;F plus theme.
     */
    public static LookConfiguration getDefaultLookConfiguration() {
    	if(!Application.isLoaded()){
    		LookAndFeel lf=createLookAndFeelInstance(Options.PLASTIC3D_NAME);
    		return new LookConfiguration(lf);
    	}
        return LookConfiguration.restoreFrom(
                getDefaultLookAndFeel(), 
                Application.instance().getUserPreferences());
    }
    
    
    /**
     * Stores the given LookAndFeel as default in the user preferences.
     * 
     * @param laf   the LookAndFeel to be set as default
     */
    public static void setDefaultLookAndFeel(LookAndFeel laf) {
        if (laf != null) {
            Application.instance().getUserPreferences().put(DEFAULT_LAF_KEY, laf.getClass().getName());
        }
    }
    
    
    /**
     * Sets the LookConfiguration used as default. Used in this class'
     * <code>#setup</code> to determine the default look&amp;feel plus theme.
     * Also used when restoring look configurations from the user preferences.
     * 
     * @param lookConfiguration  the configuration used as default
     */
    public static void setDefaultLookConfiguration(LookConfiguration lookConfiguration) {
        setDefaultLookAndFeel(lookConfiguration.getLookAndFeel());
    }
    
    
    // Look and Feel Instantiation ******************************************

    /**
     * Creates and returns an instance of <code>LookAndFeel</code> for the 
     * specified class name. Before the class is loaded, the given class name
     * may be replaced as defined in the JGoodies Looks <code>Options</code>
     * class. For example the Sun Windows L&amp;F is replaced by the
     * JGoodies Windows L&amp;F.
     * 
     * @param className   the name of the class to be instantiated
     * @return the LookAndFeel instance for the given class name,
     *     a replacement applied 
     */
    public static LookAndFeel createLookAndFeelInstance(String className) {
        String replacementClassName =
            Options.getReplacementClassNameFor(className);
        try {
            Class clazz = Class.forName(replacementClassName);
            return (LookAndFeel) clazz.newInstance();
        } catch (Throwable t) {
            String message =
                "Class name="
                    + className
                    + "\nReplacement class name="
                    + replacementClassName;
            //LOGGER.log(Level.WARNING, message, t);
            LOGGER.warn(message,t);
        }
        return null;
    }
    
    
    /**
     * Looks up and returns a list of supported look&amp;feels.
     * 
     * @return a list of supported look&amp;feels.
     */
    public static List lookupSupportedLookAndFeelInstances() {
        UIManager.LookAndFeelInfo[] lafInfos =
            UIManager.getInstalledLookAndFeels();
        List result = new ArrayList(lafInfos.length);
        for (int i = 0; i < lafInfos.length; i++) {
            String className = lafInfos[i].getClassName();
            LookAndFeel laf = createLookAndFeelInstance(className);
            if ((laf != null) && (laf.isSupportedLookAndFeel()))
                result.add(laf);
        }
        return result;
    }
    

    // Setting the Look And Theme *******************************************

    private static void setLookAndTheme(LookConfiguration configuration) {
    	//if(Application.isLoaded())
    		setLookAndTheme(configuration.getLookAndFeel(), configuration.getTheme());
    }
    
    
    /**
     * Sets a look and theme. Wraps <code>LookUtils.setLookAndTheme</code>
     * with an exception handling.
     * 
     * @see LookUtils#setLookAndTheme(javax.swing.LookAndFeel, java.lang.Object)
     */
    private static void setLookAndTheme(LookAndFeel laf, Object theme) {
        try {
            LOGGER.info("Setting L&F: " + laf.getName());
            LookUtils.setLookAndTheme(laf, theme);
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.warn("Can't set unsupported look and feel:" + laf.getName());
        }
    }

    
    // Accessing the Supported Look and Feels *******************************

    /**
     * Holds a lazily created List of the supported LookAndFeels.
     * This list can be eagerly initialized by means of the
     * EagerInitializer.
     * 
     * @see #getSupportedLookAndFeelInstances()
     * @see com.jgoodies.uif.lazy.Preparables
     */
    private static List supportedLafs;

    /**
     * Lazily initializes and returns the list of supported look and feels
     * sorted by name.
     * 
     * @return a List of the supported look&amp;feels
     */
    
	public static List getSupportedLookAndFeelInstances() {
        if (supportedLafs == null) {
            supportedLafs = lookupSupportedLookAndFeelInstances();
            Collections.sort(supportedLafs, new LafNameComparator());
        }
        return supportedLafs;
    }


    private static class LafNameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            LookAndFeel laf1 = (LookAndFeel) o1;
            LookAndFeel laf2 = (LookAndFeel) o2;
            return laf1.getName().toUpperCase().compareTo(
                laf2.getName().toUpperCase());
        }
    }
    
    
 

}


/**
 * Describes a Look and Feel with its optional color theme.
 * Future versions may support font hints and font size hints.
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.2 $
 * 
 * @see	LookConfigurations
 * @see	java.util.prefs.Preferences
 * @see javax.swing.LookAndFeel
 */
@SuppressWarnings("unchecked")
 class LookConfiguration extends Model implements Cloneable {
    
    // Names of the Bound Bean Properties *************************************
    
    /**
     * The name of the bound read-write property <em>theme</em>.
     */
    public static final String PROPERTYNAME_THEME  = "theme";
    
    /**
     * The name of the bound read-only property <em>themes</em>.
     */
    public static final String PROPERTYNAME_THEMES = "themes";
    
    
    // ************************************************************************

    private static final String THEME_KEY_PREFIX = "laf.themeName.";
    private static final Logger LOGGER = Logger.getLogger("LookConfiguration");
    
    private static final List PLASTIC_THEMES =
        Collections.unmodifiableList(PlasticLookAndFeel.getInstalledThemes());

    private static final List NO_THEMES =
        Collections.EMPTY_LIST;

    
    // ************************************************************************

    /**
     * Refers to the LookAndFeel instance this configuration describes.
     */
    private final LookAndFeel laf;
    
    /**
     * Holds an optional theme associated with the LookAndFeel.
     */
    private Object theme;
    
    
    // Instance Creation ******************************************************
    
    /**
     * Constructs a LookConfiguration for the specified LookAndFeel.
     * 
     * @param laf   the L&amp;f instance this configuration describes
     */
    public LookConfiguration(LookAndFeel laf) {
        this(laf, null);
    }

    /**
     * Constructs a LookConfiguration for the specified LookAndFeel and theme.
     * 
     * @param laf    the L&amp;f instance this configuration describes
     * @param theme  the L&amp;f's currently selected theme
     */
    public LookConfiguration(LookAndFeel laf, Object theme) {
        if (laf == null)
            throw new NullPointerException("LookAndFeel must not be null.");
        this.laf = laf;
        this.theme = theme != null ? theme : LookUtils.getDefaultTheme(laf);
    }
    
    
    /**
     * Returns a LookConfiguration for the specified LookAndFeel
     * with the theme - if any - read from the given Preferences.
     * 
     * @param laf   the L&amp;f instance this configuration describes
     * @param prefs the Preferences to read the theme from
     * 
     * @return the LookConfiguration read from the given Preferences
     */
    static LookConfiguration restoreFrom(LookAndFeel laf, Preferences prefs) {
        return new LookConfiguration(laf, restoreThemeFrom(laf, prefs));
    }
    
    
    // Accessors **************************************************************
    
    /**
     * Returns the LookAndFeel described by this configuration.
     * 
     * @return the LookAndFeel described by this configuration.
     */
    public LookAndFeel getLookAndFeel() {
        return laf;
    }

    
    /**
     * Returns the list of themes available for this configuration's L&amp;F.
     * Currently only Plastic L&amp;Fs return a list, all other L&amp;Fs
     * return an empty list.
     * 
     * @return a List of themes available for this configuration's L&amp;F.
     */
    public List getThemes() {
        return (laf instanceof PlasticLookAndFeel)
            ? PLASTIC_THEMES
            : NO_THEMES;
    }
    
    
    /**
     * Returns the theme associated with this configuration's L&amp;F,
     * null if none.
     * 
     * @return the theme associated with this configuration's L&amp;F
     */
    public Object getTheme() {
        return theme;
    }
    
    
    /**
     * Associates the given theme with this configuration's L&amp;F.
     * 
     * @param newTheme  the theme to be associated with the L&amp;F
     */
    public void setTheme(Object newTheme) {
        Object oldTheme = getTheme();
        this.theme = newTheme;
        firePropertyChange(PROPERTYNAME_THEME, oldTheme, newTheme);
    }
    
    
    // Misc *******************************************************************
    
    /**
     * Stores the configuration in the specified Preferences.
     * 
     * @param prefs   the Preferences node to the this configuration in
     */
    void storeIn(Preferences prefs) {
        LOGGER.info("Storing the LookConfiguration for " + laf);

        if (getTheme() != null) {
            prefs.put(themeKey(laf), theme.getClass().getName());
        }
    }

    /**
     * Restores the configuration for the specified LookAndFeel
     * from the given Preferences.
     * 
     * @return the restored theme - if any
     */
    private static Object restoreThemeFrom(LookAndFeel laf, Preferences prefs) {
        String themeName = prefs.get(themeKey(laf), null);
        if (themeName != null) {
            LOGGER.info(
                "Restoring theme for " + laf.getName() + ": " + themeName);
        }
        return themeName == null ? null : createThemeInstance(themeName);
    }

    /**
     * Creates and answers a theme instance for the specified class name.
     * Return <code>null</code> in case of an instantiation exception.
     */
    private static Object createThemeInstance(String themeClassName) {
        try {
            Class theClass = Class.forName(themeClassName);
            return theClass.newInstance();
        } catch (Exception e) {
            LOGGER.info("Class name=" + themeClassName, e);
            return null;
        }
    }

    private static String themeKey(LookAndFeel laf) {
        return THEME_KEY_PREFIX + laf.getName();
    }

    private String getLafClassName() {
        return laf.getClass().getName();
    }
    
    
    // Overriding Object Behavior *********************************************

    /**
     * Returns a clone of this LookConfigurations instance.
     * 
     * @return a cloned version of this object
     */
    public Object clone() {
        // Safe, since this is a final class
        return new LookConfiguration(getLookAndFeel(), getTheme());
    }
    

    /**
     * Two LookConfiguration instance are equal if and only if
     * the class names of the LookAndFeels are equal and
     * the themes are equal.
     * 
     * @return true if this configuration is equal to the given object 
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LookConfiguration))
            return false;

        LookConfiguration config = (LookConfiguration) o;
        return getLafClassName().equals(config.getLafClassName())
            && (((theme == null) && (config.theme == null))
             || ((theme != null) && (theme.equals(config.theme))));
    }

    
    /**
     * Returns this configuration's hash code. 
     * A poor but correct implementation. LookConfiguration instances
     * will likely not be used in HashMaps or HashSets.
     * 
     * @return this configuation's hash code
     */
    public int hashCode() {
        return laf.hashCode();
    }

    
    /**
     * Returns a string representation that lists the default configuration
     * and all configurations with look&amp;feel name and optional theme.
     * 
     * @return a string representation that list the default configuration
     *    and all configurations with look&amp;feel name and optional theme.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("lafName=");
        buffer.append(getLookAndFeel().getName());
        if (getTheme() != null) {
            buffer.append("; theme=");
            buffer.append(getTheme().getClass().getName());
        }
        return buffer.toString();
    }
   

}
 /**
  * Describes the set of available instances of LookConfiguration
  * with one of them selected as default.
  * 
  * @author Karsten Lentzsch
  * @version $Revision: 1.2 $
  * 
  * @see	LookConfiguration
  * @see ExtUIManager
  * @see	java.util.prefs.Preferences
  * @see javax.swing.LookAndFeel
  */
 @SuppressWarnings("unchecked")
 class LookConfigurations extends Model implements Cloneable {
     
     // Names of the Bound Bean Properties *************************************
     
     /**
      * The name of the bound read-only property <em>configurations</em>.
      */
     public static final String PROPERTYNAME_CONFIGURATIONS = 
         "configurations";
     
     /**
      * The name of the bound read-write property <em>defaultConfiguration</em>.
      */
     public static final String PROPERTYNAME_DEFAULT_CONFIGURATION = 
         "defaultConfiguration";
     
     
     // ************************************************************************
                 
     /** 
      * Maps Look&amp;Feel class names to instances of LookConfiguration.
      */
     private final Map configurations;
     
     /**
      * Holds the default LookConfiguration.
      * 
      * @see #getDefaultConfiguration()
      * @see #setDefaultConfiguration(LookConfiguration)
      */
     private LookConfiguration defaultConfiguration;


     // Instance Creation ****************************************************

     /**
      * Constructs LookConfigurations object with the specified selection.
      * 
      * @param selection  the selected LookConfiguration
      */
     public LookConfigurations(LookConfiguration selection) {
         this.configurations = new HashMap();
         this.defaultConfiguration = selection;
     }

     /**
      * Looks up and returns the LookConfigurations from the given Preferences.
      * 
      * @param prefs   the Preferences node to restore from
      * @return the restored LookConfigurations object
      */
     static LookConfigurations restoreFrom(Preferences prefs) {
         LookConfiguration defaultConfig = SWExtUIManager.getDefaultLookConfiguration();
         LookConfigurations result = new LookConfigurations(defaultConfig);

         for (Iterator i = ExtUIManager.getSupportedLookAndFeelInstances().iterator(); i.hasNext();) {
             LookAndFeel laf = (LookAndFeel) i.next();
             result.putConfiguration(LookConfiguration.restoreFrom(laf, prefs));
         }
         return result;
     }

     
     // Public API *************************************************************
     
     /**
      * Returns the default LookConfiguration.
      * 
      * @return the default LookConfiguration
      */
     public LookConfiguration getDefaultConfiguration() {
         return defaultConfiguration;
     }

     /**
      * Sets a new default LookConfiguration.
      * 
      * @param newDefaultConfiguration  the new default configuration
      */
     public void setDefaultConfiguration(LookConfiguration newDefaultConfiguration) {
         LookConfiguration oldDefaultConfiguration = getDefaultConfiguration();
         this.defaultConfiguration = newDefaultConfiguration;
         firePropertyChange(
                 PROPERTYNAME_DEFAULT_CONFIGURATION, 
                 oldDefaultConfiguration, 
                 newDefaultConfiguration);
     }
     
     
     /**
      * Returns the Collection of LookConfiguration instances
      * held by this LookConfigurations.
      *  
      * @return the Collection of LookConfiguration instances
      *     held by this LookConfigurations.
      */
     public Collection getConfigurations() {
         List list = new ArrayList(configurations.values());
         Collections.sort(list, new ConfigurationComparator());
         return list;
     }

     
     // Implementation *********************************************************
     
     /**
      * Looks up and returns the <code>LookConfiguration</code>
      * for the specified <code>LookAndFeel</code>.
      */
     private LookConfiguration getConfiguration(LookAndFeel laf) {
         LookConfiguration config =
             (LookConfiguration) configurations.get(getKey(laf));
         return config != null ? config : new LookConfiguration(laf);
     }

     /**
      * Puts the given <code>LookConfiguration</code> in the map of 
      * all configurations using the config's look and feel as key.
      */
     private void putConfiguration(LookConfiguration config) {
         configurations.put(getKey(config), config);
     }

     private String getKey(LookConfiguration config) {
         return getKey(config.getLookAndFeel());
     }

     private String getKey(LookAndFeel laf) {
         return laf.getClass().getName();
     }
     

     /**
      * Stores the selection and all configurations in the specified Preferences.
      * 
      * @param prefs   the Preferences node to store this LookConfigurations in
      */
     void storeIn(Preferences prefs) {
         SWExtUIManager.setDefaultLookConfiguration(getDefaultConfiguration());
         for (Iterator i = configurations.values().iterator(); i.hasNext();) {
             LookConfiguration configuration = (LookConfiguration) i.next();
             configuration.storeIn(prefs);
         }
     }

     // Overriding Object Behavior *********************************************

     /**
      * Returns a clone of this LookConfigurations instance.
      * 
      * @return a cloned version of this object
      */
     public Object clone() {
         LookConfiguration defaultConfig = 
             (LookConfiguration) defaultConfiguration.clone();
         // Safe, since this is a final class
         LookConfigurations result = new LookConfigurations(defaultConfig);
         for (Iterator i = configurations.values().iterator(); i.hasNext();) {
             LookConfiguration config = (LookConfiguration) i.next();
             result.putConfiguration(config.equals(defaultConfig)
                     ? defaultConfig
                     : (LookConfiguration) config.clone());
         }
         return result;
     }
     

     /**
      * Checks and answers if this LookConfigurations equals the given object.
      * Two LookConfigurations are equal if and only if they have equals 
      * LookConfiguration instances as default, and if all LookConfiguration
      * instances are equal too.
      * 
      * @return true if this LookConfigurations object equals the given object
      */
     public boolean equals(Object o) {
         if (this == o)
             return true;
         if (!(o instanceof LookConfigurations))
             return false;

         LookConfigurations other = (LookConfigurations) o;
         if (!getDefaultConfiguration().equals(other.getDefaultConfiguration()))
             return false;

         for (Iterator i = configurations.values().iterator(); i.hasNext();) {
             LookConfiguration config1 = (LookConfiguration) i.next();
             LookConfiguration config2 = other.getConfiguration(config1.getLookAndFeel());
             if (config2 == null || !config1.equals(config2))
                 return false;
         }
         return true;
     }

     /**
      * Returns this object's hash code. A poor but correct implementation. 
      * Instances of LookConfigurations will likely not be used in HashMaps 
      * or HashSets.
      * 
      * @return this object's hash code
      */
     public int hashCode() {
         return defaultConfiguration.hashCode();
     }
     
     
     /**
      * Returns a string representation that lists the default configuration
      * and all configurations with look&amp;feel name and optional theme.
      * 
      * @return a string representation that list the default configuration
      *    and all configurations with look&amp;feel name and optional theme.
      */
     public String toString() {
         StringBuffer buffer = new StringBuffer("LookConfigurations[");
         buffer.append("\n    default=");
         buffer.append(getDefaultConfiguration().toString());
         for (Iterator i = getConfigurations().iterator(); i.hasNext();) {
             LookConfiguration configuration = (LookConfiguration) i.next();
             buffer.append("\n    ");
             buffer.append(configuration.toString());
         }
         buffer.append("\n]");
         return buffer.toString();
     }
     
     
     // Helper Code ************************************************************
     
     private static class ConfigurationComparator implements Comparator {
         public int compare(Object o1, Object o2) {
             LookConfiguration config1 = (LookConfiguration) o1;
             LookConfiguration config2 = (LookConfiguration) o2;
             return config1.getLookAndFeel().getName().toUpperCase().compareTo(
                     config2.getLookAndFeel().getName().toUpperCase());
         }
     }
     
     
     

 }