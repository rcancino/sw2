package com.luxsoft.sw3.cfd.ui.validaciones;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.swingx.JXBusyLabel;

import com.jgoodies.binding.beans.PropertyConnector;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.controls.AbstractFrame;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.utils.CommandUtils;

public class CFDValidadorFrame extends AbstractFrame{
	
	private boolean exitOnClose=true;

	private final CFDValidacionController controller;
	
	private JTabbedPane contentTab;
	
	private JXBusyLabel busyLabel;
	
	public CFDValidadorFrame() {
		this(false);
	}
	
	public CFDValidadorFrame(boolean exitOnClose) {
		super("validadorDeCFD");
		setTitle("Validador de Comprobantes fiscales CFD(I)");
		this.exitOnClose=exitOnClose;
		this.controller=new CFDValidacionController();
		this.controller.addPropertyChangeListener(new CFDHandler());
	}
	
	

	@Override
	protected JComponent buildContentPane() {
		setJMenuBar(buildMenuBar());
		JPanel panel=new JPanel(new BorderLayout());
		
		contentTab=new JTabbedPane();
		contentTab.addTab("Resultado de Validación", getValidacionPanel());
		panel.add(contentTab,BorderLayout.CENTER);
		
		panel.add(buildHeaderContent(),BorderLayout.NORTH);
		
		return panel;
	}
	
	protected void resize(JComponent component) {
		component.setPreferredSize(new Dimension(800,650));
	}

	@Override
	public void open() {
		super.build();
		super.open();
	}

	@Override
	protected void configureCloseOperation() {
		if(exitOnClose)
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		else
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	
	private JMenuBar buildMenuBar(){
		JMenuBar mBar=new JMenuBar();		
		JMenu menu1=new JMenu("Archivo");
		menu1.setMnemonic('A');
		menu1.add(getLeerAction());	
		menu1.add(getValidarAction());
		mBar.add(menu1);		
		return mBar;
	}
	
	private JComponent buildHeaderContent(){
		final JPanel headerContent=new JPanel(new BorderLayout());
		JToolBar bar=new JToolBar();
		bar.add(getLeerAction());
		bar.add(getValidarAction());
		bar.add(getValidacionPanel().getPrintAction());
		busyLabel=new JXBusyLabel();
		
		bar.add(busyLabel);
		PropertyConnector.connect(controller, CFDValidacionController.VALIDANDO_PROPERTY_NAME
				, busyLabel, "busy");
		PropertyConnector.connect(controller, CFDValidacionController.VALIDANDO_MESSAGE_PROPERTY_NAME
				, busyLabel, "text");
		
		headerContent.add(getHeader().getHeader(),BorderLayout.NORTH);
		headerContent.add(bar,BorderLayout.CENTER);
		return headerContent;
	}
	
	private Action leerXmlAction;
	
	private Action getLeerAction(){
		if(leerXmlAction==null){
			leerXmlAction=new DispatchingAction("Leer XML",this,"validarCFD");
			leerXmlAction.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images2/page_white_code.png"));
		}
		return leerXmlAction;
	}
	
	private Action validarAction;
	
	private Action getValidarAction(){
		if(validarAction==null){
			validarAction=new DispatchingAction(" Re-Validar", this,"validar");
			validarAction.putValue(Action.SMALL_ICON, CommandUtils.getIconFromResource("images/misc2/accept.png"));
		}
		return validarAction;
	}
	
	
	
	public void validar(){
		if(controller.getXmlFile()==null){
			validarCFD();
		}else{
			controller.validar();
		}
	}
	
	public void validarCFD(){
		File xmlFile=seleccionarArchivo();
		if(xmlFile!=null){
			this.controller.registrarComprobante(xmlFile);
			
		}
	}
	
	private File seleccionarArchivo(){
		JFileChooser chooser = new JFileChooser();
		final Preferences pref=Preferences.userRoot().node("luxsoft.siipap.cfd");
		final String key="cxp.files";
		String dirPath=pref.get(key, "/");
		chooser.setCurrentDirectory(new File(dirPath));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML (CFD)","xml");
		chooser.setFileFilter(filter);
		int res = chooser.showDialog(this, "Aceptar");
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if(file!=null)
				pref.put(key, file.getParent());
			return file;
		}else
			return null;
	}
	
	private ValidacionResultPanel validacionPanel;
	
	private ValidacionResultPanel getValidacionPanel(){
		if(validacionPanel==null){
			validacionPanel=new ValidacionResultPanel(controller);
		}
		return validacionPanel;
	}
	
	
	
	private Header header;
	
	private Header getHeader(){
		if(header==null){
			header=new Header("Validación de comprobantes fiscales CFD y CFDI", "Seleccione un archivo");
		}
		return header;
	}
	
	private class CFDHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if(CFDValidacionController.CFD_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())){
				getHeader().setTitulo("Archivo seleccionado: "+controller.getFileDesc());
				getHeader().setDescripcion("");
				//controller.validar();
			}else if(CFDValidacionController.VALIDANDO_PROPERTY_NAME.equals(evt.getPropertyName())){
				
			}
		}		
	}

	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				CFDValidadorFrame app=new CFDValidadorFrame(true);
				app.open();
				//System.exit(0);
			}

		});
	}
}
