/**
 * 
 */
package com.luxsoft.sw3.crm.catalogos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.xmlbeans.XmlObject;

import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.CXCActions;
import com.luxsoft.siipap.cxc.ui.clientes.CFDIMailsClienteForm;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.reports.ClientesNuevosBI;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.cfd.CFDUtils;
import com.luxsoft.sw3.cfd.model.Conversiones;
import com.luxsoft.sw3.crm.CRM_Roles;

public class CRM_ClientesPanel extends FilteredBrowserPanel<Cliente> implements ActionListener{
	
	private JTextField nombreField;
	private JTextField claveField;

	public CRM_ClientesPanel() {
		super(Cliente.class);
		
		
	}
	
	public void init(){
		addProperty("clave","nombre","deCredito","credito","plazo","suspendido","cedula","contacto","personaFisica");
		addLabels("Clave","Nombre","Credito","Límite","Plazo","Suspendido","Cedula","Contacto","P.F");
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

	protected List<Cliente> findData(){			
		if(claveField.hasFocus()){
			return  getManager().buscarClientePorClave(claveField.getText());
		}else{
			return getManager().buscarClientePorNombre(nombreField.getText());
		}
	}

	@Override
	protected EventList getSourceEventList() {
		// TODO Auto-generated method stub
		EventList origen=super.getSourceEventList();
		UniqueList unique=new UniqueList(origen,GlazedLists.beanPropertyComparator(Cliente.class, "id"));
		return unique;
	}

	public void actionPerformed(ActionEvent e) {
		load();
	}
	
	private Action printInfoReport;
	private Action printClientesNuevos;
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			printInfoReport=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"printInfoReport");
			printInfoReport.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/vcard_edit.png"));
			printInfoReport.putValue(Action.SHORT_DESCRIPTION, "Impresión de datos");
			
			printClientesNuevos=new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"printClientesNew");
			printClientesNuevos.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/table_go.png"));
			printClientesNuevos.putValue(Action.SHORT_DESCRIPTION, "Reporte Clientes Nuevos");
			
			Action actualizaCorreo=addAction(CXCActions.MantenimientoClientes.getId(), "actualizarCorreo", "Actualizar Correo");
			actualizaCorreo.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/vcard_edit.png"));
			actualizaCorreo.putValue(Action.SHORT_DESCRIPTION, "Actualizar correo");
			
			actions=new Action[]{
				getLoadAction()
				//,getEditAction()
				,getViewAction()
				//,printInfoReport
				,printClientesNuevos
				,addAction("", "validarParaCFD", "validarCFD")
				,actualizaCorreo
				};
		return actions;
	}
	
	
	
	@Override
	protected Cliente doEdit(Cliente bean) {
		if(!KernellSecurity.instance().hasRole(CRM_Roles.MANTENIMIENTO_CLIENTES.name()))
			return null;
		Cliente target=ServiceLocator2.getClienteManager().get(bean.getId());
		target= CRM_ClienteForm.showForm(target);
		if(target!=null){
			target= getManager().save(target);
			if(target.getCredito()==null){
				getManager().getClienteDao().eliminarCredito(target.getClave());
			}
			getManager().exportarCliente(target);
			return target;
		}
		return null;
	}

	@Override
	protected void doSelect(Object o) {
		Cliente bean=(Cliente)o;
		Cliente target=ServiceLocator2.getClienteManager().get(bean.getId());
		target= CRM_ClienteForm.showForm(target,true);
	}
	
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
	
	public void printClientesNew(){
		ClientesNuevosBI.run();
	}
	
	public void actualizarCorreo() {
		
		if(!selectionModel.getSelected().isEmpty()){
			Object selected=selectionModel.getSelected().get(0);
			Cliente c=(Cliente)selected;
			String clave=c.getClave();
			CFDIMailsClienteForm.showForm(clave);
		}
	}
	
	

	private ClienteManager getManager(){
		return ServiceLocator2.getClienteManager();
	}
	
	public void validarParaCFD(){
		Cliente c=(Cliente)getSelectedObject();
		if(c!=null){
			List errores=CFDUtils.validarClienteParaCFD(c);
			if(!errores.isEmpty()){
				StringBuffer buff=new StringBuffer();
				Iterator iter=errores.iterator();
				while(iter.hasNext()){
					Object next=iter.next();
					buff.append(next.toString());
					if(iter.hasNext())
						buff.append("\n");
				}
				MessageUtils.showMessage(buff.toString(), "Errores para la generación de CFD");
			}else
				JOptionPane.showMessageDialog(getControl(), "Datos del cliente  correctos para la generación de CFD","Validación de CFD",JOptionPane.INFORMATION_MESSAGE);
			
		}
	}
	
}