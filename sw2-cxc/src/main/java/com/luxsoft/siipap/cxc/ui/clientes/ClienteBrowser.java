package com.luxsoft.siipap.cxc.ui.clientes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.ui.clientes.altas.ClienteController;
import com.luxsoft.siipap.model.Modulos;
import com.luxsoft.siipap.model.core.AutorizacionClientePCE;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ClienteBrowser extends SXAbstractDialog{
	
	JTextField inputField=new JTextField(20);
	
	public ClienteBrowser() {
		super("Clientes");
		setModalityType(ModalityType.MODELESS);
	}


	private static Action showAction;
	private ClientesFilteredPanel browser;

	@Override
	protected JComponent buildContent() {
		JPanel content=new JPanel(new BorderLayout(5,5));
		
		browser=new ClientesFilteredPanel();
		
		
		content.add(browser.getControl(),BorderLayout.CENTER);
		
		//final JToolBar bar=new JToolBar(JToolBar.VERTICAL);
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
		return showAction;
	}	
	
	public static void openDialog(){
		ClienteBrowser dialog=new ClienteBrowser();
		dialog.open();
	}
	

	public static class ClientesFilteredPanel extends FilteredBrowserPanel<Cliente> implements ActionListener{
		
		private JTextField nombreField;
		private JTextField claveField;

		public ClientesFilteredPanel() {
			super(Cliente.class);
			
			
		}
		
		public void init(){
			addProperty("clave","nombre","deCredito","credito","plazo","suspendido","contraEntrega","contacto");
			addLabels("Clave","Nombre","Credito","Límite","Plazo","Suspendido","PCE","Contacto");
			nombreField=new JTextField(10);
			nombreField.addActionListener(this);
			claveField=new JTextField(10);
			claveField.addActionListener(this);
			installTextComponentMatcherEditor("Clave", GlazedLists.textFilterator(new String[]{"clave"}),claveField);
			installTextComponentMatcherEditor("Nombre", GlazedLists.textFilterator(new String[]{"nombre"}),nombreField);
			ComponentUtils.addF2Action(nombreField, getLoadAction());
			ComponentUtils.addF2Action(claveField, getLoadAction());
		}
		
		

		@Override
		protected void executeLoadWorker(SwingWorker worker) {
			TaskUtils.executeSwingWorker(worker);
		}

		protected DefaultFormBuilder getFilterPanelBuilder(){
			if(filterPanelBuilder==null){
				FormLayout layout=new FormLayout(
						"p,2dlu,p, 3dlu," +
						"p,2dlu,p:g","");
				DefaultFormBuilder builder=new DefaultFormBuilder(layout);
				builder.getPanel().setOpaque(false);
				filterPanelBuilder=builder;
			}
			return filterPanelBuilder;
		}

		/* (non-Javadoc)
		 * @see com.luxsoft.siipap.swing.browser.BrowserPanel#buildGridPanel(org.jdesktop.swingx.JXTable)
		 +
		@Override
		protected JScrollPane buildGridPanel(JXTable grid) {
			grid.getColumn(0).setPreferredWidth(25);
			grid.getColumn(1).setPreferredWidth(250);
			grid.getColumn(2).setPreferredWidth(150);
			JScrollPane sp=super.buildGridPanel(grid);
			sp.setPreferredSize(new Dimension(600,300));
			return sp;
		}*/		

		@Override
		protected Cliente doEdit(Cliente bean) {
			if(!KernellSecurity.instance().isResourceGranted(CXCActions.MantenimientoClientes.getId(), Modulos.CXC))
				return null;
			Cliente target=ServiceLocator2.getClienteManager().get(bean.getId());
			target= ClienteForm.showForm(target);
			if(target!=null){
				target= getManager().save(target);
				if(target.getCredito()==null){
					getManager().getClienteDao().eliminarCredito(target.getClave());
				}
				return target;
			}
			return null;
		}

		@Override
		protected void doSelect(Object o) {
			Cliente bean=(Cliente)o;
			Cliente target=ServiceLocator2.getClienteManager().get(bean.getId());
			target= ClienteForm.showForm(target,true);
		}

		@Override
		protected Cliente doInsert() {
			return ClienteController.getInstance().registrar();
		}

		protected List<Cliente> findData(){			
			if(claveField.hasFocus()){
				return  getManager().buscarClientePorClave(claveField.getText());
			}else{
				return getManager().buscarClientePorNombre(nombreField.getText());
			}
			
		}

		public void actionPerformed(ActionEvent e) {
			load();
		}
		
		
		
		@Override
		public Action[] getActions() {
			if(actions==null)
				printInfoReport=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"printInfoReport");
				printInfoReport.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/vcard_edit.png"));
				printInfoReport.putValue(Action.SHORT_DESCRIPTION, "Impresión de datos");
				
				Action pagoCOE=addAction(CXCActions.MantenimientoClientes.getId(), "autorizarPagoContraEntrega", "Autorizar PCE");
				pagoCOE.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/money_add.png"));
				pagoCOE.putValue(Action.SHORT_DESCRIPTION, "Autorizar pago contra entrega");
				
				Action cacnelpagoCOE=addAction(CXCActions.MantenimientoClientes.getId(), "cancelarPagoContraEntrega", "Cancelar PCE");
				cacnelpagoCOE.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/money_delete.png"));
				cacnelpagoCOE.putValue(Action.SHORT_DESCRIPTION, "Cancelar pago contra entrega");
				
				actions=new Action[]{
					getLoadAction()
					,getInsertAction()
					//,getDeleteAction()
					,getEditAction()
					,getViewAction()
					,printInfoReport
					,pagoCOE
					,cacnelpagoCOE
					};
				
			return actions;
		}
		
		private Action printInfoReport;
		
		public void printInfoReport(){
			for(Object o:selectionModel.getSelected()){
				//String msg=JOptionPane.showInputDialog(getControl(), "Comentario adicional CxC", "Comentario CxC", JOptionPane.INFORMATION_MESSAGE);
				Cliente c=(Cliente)o;
				c=getManager().get(c.getId());
				StringBuffer msg=new StringBuffer();
				for(Map.Entry<String, String> com:c.getComentarios().entrySet()){
					msg.append(MessageFormat.format("{0} - {1}  ; ", com.getKey(),com.getValue()));
				}
				StringBuffer tels=new StringBuffer();
				for(Map.Entry<String, String> com:c.getTelefonos().entrySet()){
					tels.append(MessageFormat.format("{0} - {1}  ; ", com.getKey(),com.getValue()));
				}
				Map params=new HashMap();
				params.put("CLAVE", c.getClave());
				params.put("COMENTARIO", msg.toString());
				params.put("TELEFONOS", tels.toString());
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/FichaCliente.jasper"), params);
			}
		}
		
		public void autorizarPagoContraEntrega(){
			if(!selectionModel.getSelected().isEmpty()){
				Object selected=selectionModel.getSelected().get(0);
				int index=source.indexOf(selected);
				if(index!=-1){
					Cliente c=(Cliente)selected;
					AutorizacionClientePCE aut =new AutorizacionClientePCE(c);
					aut.setAutorizo(KernellSecurity.instance().getCurrentUserName());
					c=getManager().save(c);
					source.set(index, c);
					JOptionPane.showMessageDialog(getControl(), "Cliente autorizado para pago contra entrega");
					System.out.println("Autorizando pago contra entrega a:"+c.getAutorizacionPagoContraEntrega());
				}
				
			}
		}
		
		public void cancelarPagoContraEntrega(){
			if(!selectionModel.getSelected().isEmpty()){
				Object selected=selectionModel.getSelected().get(0);
				int index=source.indexOf(selected);				
				Cliente c=(Cliente)selected;
				if(c.isContraEntrega()){
					if(index!=-1){						
						ServiceLocator2.getUniversalDao().remove(AutorizacionClientePCE.class, c.getAutorizacionPagoContraEntrega().getId());
						c=getManager().get(c.getId());
						source.set(index, c);
						JOptionPane.showMessageDialog(getControl(), "Aurorización para pago contra entrega cancelada");
					}
				}
			}
		}

		private ClienteManager getManager(){
			return ServiceLocator2.getClienteManager();
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
				ClienteBrowser browser=new ClienteBrowser();
				browser.open();
				//System.exit(0);
			}

		});
	}

}
