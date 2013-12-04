package com.luxsoft.sw2.server.ui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.EventHandler;

import javax.swing.JFrame;

import com.luxsoft.siipap.swing.DefaultApplicationWindow;
import com.luxsoft.sw2.server.services.LocalServerManager;

public class LocalServerApplicationWindow extends DefaultApplicationWindow{
	
	private String sucursal;
	
	public JFrame getWindow() {
		if(mainFrame==null){
			mainFrame=buildMainFrame();
			mainFrame.build();
			initSystemTry();
			mainFrame.addWindowListener(new WindowHandler());
			
		} 
		return mainFrame;
	}
	
	
	
	private void initSystemTry() {
		if(SystemTray.isSupported()){
			SystemTray tray=SystemTray.getSystemTray();
			
			Image img=getResourceLocator().getImage("application.icon.16x16");
			final TrayIcon trayIcon=new TrayIcon(img,"Servidor local SW2",buildSystemTryPopup());
			ActionListener actionListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            trayIcon.displayMessage("Action Event", 
		                "An Action Event Has Been Performed!",
		                TrayIcon.MessageType.INFO);
		        }
		    };
		            
		    trayIcon.setImageAutoSize(true);
		    trayIcon.addActionListener(actionListener);
		    trayIcon.addMouseListener(new TryIconMouseHandler());
		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }
		}
	}
	
	@Override
	protected void restoreWindowState() {
		super.restoreWindowState();
		getHeader().setTitulo(getHeader().getTitulo()+"  ("+getSucursal()+")");
		getHeader().setDescripcion("Servicios centraliados de información");
		
		
	}
	
	@Override
	protected void closeApplication() {
		LocalServerManager.close();
		super.closeApplication();
	}
	
	private PopupMenu buildSystemTryPopup(){
		PopupMenu menu=new PopupMenu();
		MenuItem exitItem=new MenuItem("Salir");
		exitItem.addActionListener(EventHandler.create(ActionListener.class, this, "close"));
		menu.add(exitItem);
		
		return menu;
	}

	

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

   

	private class WindowHandler extends WindowAdapter{
		
		@Override
		public void windowClosing(WindowEvent e) {
			close();
		}		

		@Override
		public void windowIconified(WindowEvent e) {
			getWindow().setVisible(false);
		}	
		
		
	}
	
	 class TryIconMouseHandler extends MouseAdapter {
         
	        public void mouseClicked(MouseEvent e) {
	            getWindow().setVisible(true);
	            getWindow().setExtendedState(JFrame.NORMAL);
	            getWindow().toFront();
	        }

	    };


}
