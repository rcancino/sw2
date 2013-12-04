package com.luxsoft.sw3.crm.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;


import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class CRM_ClienteBrowser extends SXAbstractDialog{
	
	JTextField inputField=new JTextField(20);
	
	public CRM_ClienteBrowser() {
		super("Clientes");
		setModalityType(ModalityType.MODELESS);
	}


	private static Action showAction;
	private CRM_ClientesPanel browser;

	@Override
	protected JComponent buildContent() {
		
		JPanel content=new JPanel(new BorderLayout(5,5));
		browser=new CRM_ClientesPanel();
		
		content.add(browser.getControl(),BorderLayout.CENTER);
		ToolBarBuilder builder=new ToolBarBuilder();
		for(Action a:browser.getActions()){
			builder.add(a);
		}
		
		JPanel tools=new JPanel(new BorderLayout(0,5));
		tools.add(browser.getFilterPanel(),BorderLayout.NORTH);
		tools.add(builder.getToolBar(),BorderLayout.CENTER);
		
		
		//content.add(,BorderLayout.SOUTH);
		
		content.add(tools,BorderLayout.NORTH);
		content.setPreferredSize(new Dimension(850,650));
		
		//content.add(inputPanel,BorderLayout.WEST);
		
		return content;
	}
	
	
	
	/**** Fin Personalizacion de comportamiento****/

	/* (non-Javadoc)
	 * @see com.jgoodies.uif.AbstractDialog#buildHeader()
	 */
	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Catalogo de clientes","");
	}



	/**
	 * Acceso a una Action que permite mostrar este browser.	 * 
	 * Patron FactoryMethod para se usado desde  Spring
	 * Existe solo para facilitar el uso en Spring
	 * 
	 * @return
	 */
	public static Action getShowAction(){		
		showAction=new SWXAction(){
				@Override
				protected void execute() {
					openDialog();
				}				
			};	
		showAction.putValue(Action.NAME, "Clientes");
		return showAction;
	}	
	
	public static void openDialog(){
		CRM_ClienteBrowser dialog=new CRM_ClienteBrowser();
		dialog.open();
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
				CRM_ClienteBrowser browser=new CRM_ClienteBrowser();
				browser.open();
				//System.exit(0);
			}

		});
	}

}
