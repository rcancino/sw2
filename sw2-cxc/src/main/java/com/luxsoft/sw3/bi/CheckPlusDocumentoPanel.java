package com.luxsoft.sw3.bi;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.ventas.CheckPlusDocumento;
//import com.sun.pdfview.PDFViewer;




public class CheckPlusDocumentoPanel extends JPanel implements ListEventListener{
	
	private final CheckplusClienteFormModel model;
	
	private EventList partidas;
	private EventSelectionModel selectionModel;
	
	public CheckPlusDocumentoPanel(CheckplusClienteFormModel model) {
		this.model=model;
		init();
	}
	
	JXTable table;
	
	private void init(){
		setLayout(new BorderLayout());
		JToolBar toolbar=new JToolBar();
		toolbar.add(CommandUtils.createInsertAction(this, "insertar"));
		toolbar.add(CommandUtils.createDeleteAction(this, "eliminar"));
		toolbar.add(CommandUtils.createViewAction(this, "select"));
		add(toolbar,BorderLayout.NORTH);
		
		partidas=GlazedLists.eventList(new BasicEventList());
		partidas.addAll(model.getCliente().getDocumentos());
		partidas.addListEventListener(this);
		
		final TableFormat tf=GlazedLists.tableFormat(CheckPlusDocumento.class
			, new String[]{"tipo","descripcion","url"}
			,new String[]{"Tipo","Descripción","Archivo"}
		);
		final EventTableModel tm=new EventTableModel(partidas,tf);
		table=ComponentUtils.getStandardTable();
		table.setModel(tm);
		selectionModel=new EventSelectionModel(partidas);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				select();
			}
		});
		table.setSelectionModel(selectionModel);
		final JScrollPane sp=new JScrollPane(table);
		//sp.setPreferredSize(new Dimension(200,250));
		add(sp,BorderLayout.CENTER);
		table.packAll();
	}
	
	public void select(){
		CheckPlusDocumento ref=getSelected();
		if(ref!=null){
			/*
			if(ref.getUrl()!=null){
				try {
					URI uri=new URI(ref.getUrl());
					PDFViewer view=new PDFViewer(false);
					
					view.openFile(uri.toURL());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
		}
	}
	
	private CheckPlusDocumento getSelected(){
		Object obj=selectionModel.getSelected().isEmpty()?null:selectionModel.getSelected().get(0);
		return (CheckPlusDocumento)obj;
	}
	
	public void insertar(){
		DefaultFormModel formModel=new DefaultFormModel(Bean.proxy(CheckPlusDocumento.class));
		final DocumentoForm form=new DocumentoForm(formModel);
		form.open();
		if(!form.hasBeenCanceled()){
			CheckPlusDocumento target=new CheckPlusDocumento();
			Bean.normalizar(formModel.getBaseBean(), target, new String[0]);
			model.getCliente().agregarDocumento(target);
			/*
			if(model.getCliente().getId()==null)
				model.getCliente().agregarDocumento(target);
			else{
				target.setCliente(model.getCliente());
				ServiceLocator2.getHibernateTemplate().saveOrUpdate(target);
			}*/
			partidas.add(target);
		}
	}
	
	public void eliminar(){
		CheckPlusDocumento sel=getSelected();
		if(sel!=null){
			if(MessageUtils.showConfirmationMessage("Eliminar documento bancaria: "+sel, "Cliente CheckPlus")){
				model.getCliente().eliminarDocumento(sel);
				partidas.remove(sel);
			}
		}
		
	}
	
	public void listChanged(ListEvent listChanges) {
		/*while(listChanges.next()){
			
		}*/
		
		if(listChanges.next()){
			switch (listChanges.getType()) {
			case ListEvent.INSERT:				
				break;
			case ListEvent.DELETE:
				//doListChange();
				break;
			case ListEvent.UPDATE:
				//doListUpdated(listChanges);
				break;
			default:
				break;
			}				
		}
		model.validate();
		
	}
	
	
	public static class DocumentoForm extends AbstractForm{
		
		public DocumentoForm(IFormModel model) {
			super(model);
		}
		
		@Override
		protected JComponent buildFormPanel() {
			final FormLayout layout=new FormLayout(
					"p,2dlu,p:g(.5)" 
					,"");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			
			builder.append("Tipo",getControl("tipo"));
			builder.append("Descripcion",getControl("descripcion"));
			builder.append("PDF",getControl("archivo"));
			return builder.getPanel();
		}
		
		@Override
		protected JComponent createCustomComponent(String property) {
			
			if("tipo".equals(property)){
				String[] vals={"IFE","PASAPORTE","COMPROBANTE DOMICILIO","ESTADO DE CUENTA","ACTA CONSTITUTIVA","OTROS"};
				 SelectionInList s=new SelectionInList(vals, getModel().getModel(property));
				 JComponent box=BasicComponentFactory.createComboBox(s);
				 box.setEnabled(!getModel().isReadOnly());
				 return box;
			}else if("archivo".equals(property)){
				JButton btn=new JButton("Cargar archivo digitalizado");
				btn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						leerArchivo();
					}
				});
				return btn;
			}
			return null;
		}
		
		public void leerArchivo(){
			JFileChooser chooser=new JFileChooser();
			int res=chooser.showOpenDialog(this);
			if(res==JFileChooser.APPROVE_OPTION){
				File file=chooser.getSelectedFile();
				//byte[] bfile=new byte[(int)file.length()];
				try {
					//FileInputStream input=new FileInputStream(file);
					//input.read(bfile);
					//input.close();
					getModel().setValue("url", file.toURI().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	

}
