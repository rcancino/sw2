package com.luxsoft.siipap.inventario.ui.forms;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.inventario.ui.selectores.SelectorDeEntradasTRS;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;

import com.luxsoft.siipap.inventarios.utils.TransformacionesUtils;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;



public class TransformacionImportForm extends SXAbstractDialog implements ListEventListener<TransformacionDet>{
	
	private JXTable grid;

	public TransformacionImportForm() {
		super("Transformaciones a importar");
		
	}
	
	protected JComponent buildHeader(){
		return new HeaderPanel("Transfomraciones a importar","");
	}

	@Override
	protected JComponent buildContent() {
		JPanel panel=new JPanel(new BorderLayout());
		panel.add(buildMainPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}
	
	protected JComponent buildButtonBarWithOKCancel() {
        JPanel bar = ButtonBarFactory.buildHelpBar(
        		createInsertButton()
        		,createOKButton(true) 
        		, createCancelButton()
        		);
        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        return bar;
    }
	
	protected UIFButton createInsertButton() {
        UIFButton button = new UIFButton(getInsertAction());
        button.setVerifyInputWhenFocusTarget(false);
        return button;
    }
	
	private Action insertAction;
	
	public Action getInsertAction() {
    	if(insertAction==null){
    		insertAction=CommandUtils.createInsertAction(this, "importar");
    	}
    	return insertAction;
    }

	protected JComponent buildMainPanel(){
		JPanel panel=new JPanel(new BorderLayout());
		createGrid();
		JComponent gp=ComponentUtils.createTablePanel(grid);
		gp.setPreferredSize(new Dimension(850, 400));
		panel.add(gp,BorderLayout.CENTER);
		return panel;
	}
	
	EventList<TransformacionDet> source;
	
	protected void createGrid(){
		grid=ComponentUtils.getStandardTable();
		source=new BasicEventList<TransformacionDet>();
		source=new ObservableElementList<TransformacionDet>(source,GlazedLists.beanConnector(TransformacionDet.class));
		source.addListEventListener(this);
		String props[]={"sucursal.nombre","documento","fecha","producto.linea.nombre","producto.clase.nombre","clave","descripcion","cantidad","costoOrigen","gastos","costo","conceptoOrigen","renglon","origen.clave","origen.costo"};
		String labels[]={"Suc","Docto","Fecha","Linea","Clase","Prod","Desc","Cantidad","Costo orig","Gastos","Costo","Tipo","Rngl","Origen","Origen $"};
		boolean[] edits={false,false,false,false,false,false,false,false,true,true,false,false,false,false,false};
		TableFormat tf=GlazedLists.tableFormat(TransformacionDet.class,
				props
				, labels
				,edits
				);
		final EventTableModel tm=new EventTableModel(source,tf);
		grid.setModel(tm);
	}
	
	public void importar(){		
		List<MovimientoDet> movs=SelectorDeEntradasTRS.buscar(periodo);
		if(movs.isEmpty())
			return;
		source.clear();
		try {
			List<TransformacionDet> res=TransformacionesUtils.convertir(movs);
			source.addAll(res);
			grid.packAll();
		} catch (Exception e) {
			MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e), "Error al importar");
		}
		
	}

	public void listChanged(ListEvent<TransformacionDet> listChanges) {
		if(listChanges.next()){
			System.out.println("Lista cambio tipo: "+listChanges.getType());
			System.out.println("\t  "+listChanges);
			if(listChanges.getType()==ListEvent.UPDATE){
				System.out.println("Actualizando: "+listChanges);
				actualizarCostos();
			}
		}
		
	}
	
	public void actualizarCostos(){
		
		for(int index=0;index<source.size();index++){
			TransformacionDet det=source.get(index);
			if(det.getCantidad()>0){
				det.actualizarCosto();
				source.set(index, det);
			}
		}
	}
	
	private Periodo periodo;
	
	
	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		Object old=this.periodo;
		this.periodo = periodo;
		firePropertyChange("periodo", old, periodo);
	}

	public static List<TransformacionDet> importar(final Periodo periodo){
		//List<MovimientoDet> movs=SelectorDeEntradasTRS.buscar(periodo);
		TransformacionImportForm form=new TransformacionImportForm();
		form.setPeriodo(periodo);
		form.open();
		List<TransformacionDet> res=new ArrayList<TransformacionDet>();
		if(!form.hasBeenCanceled()){
			for(TransformacionDet det:form.source){				 
				det=ServiceLocator2.getTransformacionesManager().persistirImportacion(det);
				if(det!=null){
					res.add(det);
					res.add(det.getDestino());
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				//List<MovimientoDet> movs=SelectorDeEntradasTRS.buscar(Periodo.getPeriodoEnUnMes(5, 2009));
				//TransformacionesUtils.convertir(movs);
				importar(Periodo.getPeriodoEnUnMes(5, 2009));
			}
			
		});
	}

}
