package com.luxsoft.siipap.swing.utils;

import java.util.prefs.Preferences;

import javax.swing.JComponent;

import com.jgoodies.uif.AbstractDialog;
//import com.jgoodies.uif.application.Application;
//import com.jgoodies.uif.splash.Splash;
import com.jgoodies.uifextras.convenience.DefaultSetupDialog;
import com.jgoodies.uifextras.convenience.DefaultSetupWelcomePanel;
import com.jgoodies.uifextras.convenience.SetupLicensePanel;
//import com.jgoodies.uifextras.convenience.SetupManager.WizardPanel;
import com.luxsoft.siipap.swing.Application;

/**
 * Manages the setup procedure: checks whether the user has accepted 
 * an appropriate license agreement, opens a license acceptance dialog, 
 * and stores the result in the user preferences.
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.1 $
 */

public final class SWSetupManager {

    private static final String ACCEPTED_LICENSE_KEY     = "setup.acceptedLicense";
    private static final String ACCEPTED_LICENSE_DEFAULT = "none";
    private static final String USAGE_COUNT_KEY          = "setup.usageCount";
    private static final int    INITIAL_USAGE_COUNT      = 0;

    private static JComponent welcomePanel;
    private static SetupLicensePanel licensePanel;

    private SWSetupManager() {
        // Override the default constructor; prevents instantiation.
    }

    /**
     * Checks and answers if the user has ever accepted the given version.
     */
    public static boolean hasAcceptedVersion(String version) {
        return version.equals(getAcceptedLicense());
    }

    /**
     * Returns the accepted license.
     */
    public static String getAcceptedLicense() {
        return userPrefs().get(
            ACCEPTED_LICENSE_KEY,
            ACCEPTED_LICENSE_DEFAULT);
    }

    /**
     * Sets the accepted license to the specified version string.
     */
    public static void setAcceptedLicense(String version) {
        Preferences prefs = userPrefs();
        prefs.put(ACCEPTED_LICENSE_KEY, version);
    }

    /**
     * Checks and answers whether the user has accepted the current license
     * agreement. Opens a license acceptance dialog, if necessary.
     */
    public static boolean checkLicense() {
        String version = Application.instance().getVersion();

        // If the user has accepted a valid license agreement do nothing.
        if (hasAcceptedVersion(version))
            return true;

        //Splash.ensureClosed();
        AbstractDialog dialog = buildSetupDialog();
        // ... ask for accepting the current License Agreement...
        dialog.open();
        if (dialog.hasBeenCanceled()) 
            return false;
        setAcceptedLicense(version);
        return true;
    }

    /**
     * Builds and answers the setup dialog using the welcome and license panel.
     */
    private static AbstractDialog buildSetupDialog() {
        return new DefaultSetupDialog(
            Application.instance().getMainFrame(),
            Application.instance().getName(),
            getWelcomePanel(),
            getLicensePanel());
    }

    /**
     * Answers the setup welcome panel; lazily initializes a default,
     * if no custom panel has been set.
     */
    private static JComponent getWelcomePanel() {
        if (welcomePanel == null) {
            welcomePanel = new DefaultSetupWelcomePanel();
        }
        return welcomePanel;
    }

    /**
     * Sets a welcome panel and overrides the default welcome panel.
     */
    public static void setWelcomePanel(WizardPanel panel) {
        if (!(panel instanceof JComponent))
            throw new IllegalArgumentException("Welcome panel must be a JComponent.");
        welcomePanel = (JComponent) panel;
    }

    /**
     * Answers the setup license panel; lazily initializes a default,
     * if no custom panel has been set.
     */
    private static SetupLicensePanel getLicensePanel() {
        if (licensePanel == null) {
            licensePanel = new SetupLicensePanel();
        }
        return licensePanel;
    }

    /**
     * Sets a license panel and overrides the default license panel.
     */
    public static void setLicensePanel(SetupLicensePanel panel) {
        licensePanel = panel;
    }

    // Usage Counter ********************************************************

    private static int count = -1;

    /**
     * Answers how often the tool has been startet. 
     */
    public static int usageCount() {
        if (count == -1) {
            count = userPrefs().getInt(USAGE_COUNT_KEY, INITIAL_USAGE_COUNT);
        }
        return count;
    }

    /**
     * Increments the usage counter by one.
     */
    public static void incrementUsageCounter() {
        userPrefs().putInt(USAGE_COUNT_KEY, ++count);
    }

    // Accessing Colloborators **********************************************

    private static Preferences userPrefs() {
        return Application.instance().getUserPreferences();
    }

    // Helper Class *********************************************************

    /**
     * An interface that describes the welcome and license agreement panels.
     */
    public interface WizardPanel {
        void build(JComponent buttonBar);
    }

}