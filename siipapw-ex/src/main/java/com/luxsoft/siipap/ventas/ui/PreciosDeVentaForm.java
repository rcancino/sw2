package com.luxsoft.siipap.ventas.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.lowagie.text.Font;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swx.catalogos.ProductoFinder;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVentaDet;

/**
 * Forma para el mantenimiento de listas de precios
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class PreciosDeVentaForm extends AbstractMasterDetailForm{
	
	

	public PreciosDeVentaForm(PrecioDeVentaFormModel model) {
		super(model);
		//setModal(false);
	}
	
	protected PrecioDeVentaFormModel getBaseModel(){
		return (PrecioDeVentaFormModel)getModel();
	}

	@Override
	protected JComponent buildMasterForm() {		
		JPanel panel=new JPanel(new GridLayout(1,2));
		
		final FormLayout layout=new FormLayout(
				"40dlu,2dlu,90dlu, 2dlu," +
				"40dlu,2dlu,90dlu"
				,"");
		final DefaultFormBuilder builder1=new DefaultFormBuilder(layout);
		//DefaultFormBuilder builder1=getDefaultMasterFormBuilder();
		builder1.append("Id",addReadOnly("id"));
		builder1.nextLine();
		builder1.append("Aplicada", addReadOnly("aplicada") );
		builder1.append("Autorizada", addReadOnly("autorizada") );
		builder1.nextLine();
		builder1.append("TC (Dolares)",getControl("tcDolares") );
		builder1.append("TC (Euros)" , getControl("tcEuros") );
		builder1.nextLine();
		builder1.append("Descripción",getControl("comentario"),5);
		builder1.setBorder(BorderFactory.createTitledBorder("Propiedades"));
		
		panel.add(builder1.getPanel());
		panel.add(buildFiltersPanel());
		return panel;
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if(property.startsWith("tc")){
			return Bindings.createDoubleBinding(model.getModel(property), 4, 4);
		}
		return null;
	}



	private JTextField provField=new JTextField(10);
	private JTextField lineaField=new JTextField(10);
	private JTextField marcaField=new JTextField(10);
	private JTextField claseField=new JTextField(10);	
	private JTextField prodField=new JTextField(10);
	
	private JTextField anchoField=new JTextField(10);
	private JTextField largoField=new JTextField(10);
	private JTextField calibreField=new JTextField(10);
	private JTextField kilosField=new JTextField(10);
	private JTextField gramosField=new JTextField(10);
	
	private JTextField paginaField=new JTextField(10);
	private JTextField columnaField=new JTextField(10);
	private JTextField presentacionField=new JTextField(10);
	
	
	
	
	private JComponent buildFiltersPanel(){
		final FormLayout layout=new FormLayout(
				" p,2dlu,p:g(.3),2dlu" +
				",p,2dlu,p:g(.3),2dlu" +
				",p,2dlu,p:g(.3)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		//builder.append("Proveedor", provField,5);
		builder.append("Producto",prodField,true);
		builder.append("Linea", lineaField);
		builder.append("Marca", marcaField);
		builder.append("Clase", claseField);
		
		builder.append("Ancho",anchoField);
		builder.append("Largo",largoField);
		builder.append("Calibre",calibreField);
		
		builder.append("Kilos",kilosField);
		builder.append("Gramos",gramosField);
		builder.nextLine();
		builder.append("Pagina",paginaField);
		builder.append("Columna",columnaField);
		builder.append("Presentación",presentacionField);
		
		builder.setBorder(BorderFactory.createTitledBorder("Filtros"));
		return builder.getPanel();
	}

	@Override
	protected TableFormat getTableFormat() {
		String[] props={
				"producto.linea.nombre"
				,"producto.marca.nombre"
				,"producto.clase.nombre"
				,"producto.clave"
				,"producto.descripcion"				
				,"producto.kilos"
				,"producto.gramos"
				,"proveedorClave"
				,"costo"
				,"costoUltimo"
				,"precio"
				,"precioAnterior"
				,"incrementoCalculado"				
				,"factor"
				,"precioCredito"
				,"precioAnteriorCredito"
				,"incrementoCalculadoCredito"				
				,"factorCredito"				
				};
		String[] names={
				"Linea"
				,"Marca"
				,"Clase"
				,"Producto"
				,"Descripción"
				,"Kgs"
				,"Grs"
				,"Prov"
				,"Costo"
				,"Costo U"
				,"Precio(CON)"
				,"P.A.  (CON)"
				,"% (CON)"
				,"Fac (CON)"
				,"Precio(CRE)"
				,"P.A.  (CRE)"
				,"% (CRE)"
				,"Fac (CRE)"
				};
		final boolean[] edits={false
				,false
				,false
				,false
				,false
				,false
				,false
				,false
				,false //"Costo"
				,false //"Costo U"				
				,true // "Precio(CON)"
				,false //"P.A.  (CON)"
				,false //"% (CON)"
				,false //"Fac (CON)"
				,true //"Precio(CRE)"
				,false ///"P.A.  (CRE)"
				,false //"% (CRE)"
				,false //"Fac (CRE)"
				};
		return GlazedLists.tableFormat(ListaDePreciosVentaDet.class, props,names,edits);
	}
	
	protected JComponent buildDetailPanel(){
		JTabbedPane tabPanel=new JTabbedPane();
		JComponent res=super.buildDetailPanel();
		res.setPreferredSize(new Dimension(1100,400));
		tabPanel.addTab("Precios", res);
		tabPanel.addTab("Diseño",buildPaginasPanel());
		return tabPanel;
	}
	
	private JXTable paginaGrid;
	
	protected JComponent buildPaginasPanel(){
		JPanel panel=new JPanel(new BorderLayout());		
		ToolBarBuilder builder=new ToolBarBuilder();
		builder.add(getInsertAction());
		DispatchingAction configAction=new DispatchingAction(this,"configurarPagina");
		configAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/layout.png"));
		builder.add(configAction);
		panel.add(builder.getToolBar(),BorderLayout.NORTH);
		paginaGrid=ComponentUtils.getStandardTable();
		
		String[] props={
				"producto.clave"
				,"producto.descripcion"
				,"descripcion"
				,"kilos"
				,"gramos"
				,"pagina"
				,"columna"
				,"grupo"
				,"presentacion"
				};
		String[] names={
				"Clave"
				,"Producto"
				,"Descripción"
				,"Kilos"
				,"Gramos"
				,"Página"
				,"Columna"
				,"Grupo"
				,"Presentacion"
				};
		boolean edits[]={
				false
				,false
				,true
				,true
				,true
				,true
				,true
				,true
				,true};
		final TableFormat tf=GlazedLists.tableFormat(ListaDePreciosVentaDet.class,props, names,edits);
		final EventTableModel tm=new EventTableModel(sortedPartidas,tf);
		paginaGrid.setModel(tm);
		paginaGrid.setSelectionModel(selection);
		TableComparatorChooser.install(paginaGrid,sortedPartidas,TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		
		paginaGrid.setEnabled(!model.isReadOnly());
		
		paginaGrid.getColumn("Descripción").setPreferredWidth(250);
		
		JComponent gridComponent=ComponentUtils.createTablePanel(paginaGrid);
		panel.add(gridComponent,BorderLayout.CENTER);
				
		return panel;
	}
	
	
		

	public void packGrid(){
		this.grid.packAll();
		this.paginaGrid.packAll();
	}
	
	@Override
	protected int getToolbarType() {
		return JToolBar.HORIZONTAL;
	}
	
	protected EventList getFilterList(final EventList source){
		EventList<MatcherEditor> matchers=new BasicEventList<MatcherEditor>();
		//matchers.add(new TextComponentMatcherEditor(this.tipoField,GlazedLists.textFilterator(new String[]{"tipo"})));		
		matchers.add(new TextComponentMatcherEditor(this.provField,GlazedLists.textFilterator(new String[]{"proveedorClave","proveedorNombre"})));

		matchers.add(new TextComponentMatcherEditor(this.prodField,GlazedLists.textFilterator(new String[]{"producto.clave","producto.descripcion"})));
		
		matchers.add(new TextComponentMatcherEditor(this.lineaField,GlazedLists.textFilterator(new String[]{"producto.linea.nombre"})));
		matchers.add(new TextComponentMatcherEditor(this.marcaField,GlazedLists.textFilterator(new String[]{"producto.marca.nombre"})));
		matchers.add(new TextComponentMatcherEditor(this.claseField,GlazedLists.textFilterator(new String[]{"producto.clase.nombre"})));

		matchers.add(new TextComponentMatcherEditor(this.anchoField,GlazedLists.textFilterator(new String[]{"producto.ancho"})));
		matchers.add(new TextComponentMatcherEditor(this.largoField,GlazedLists.textFilterator(new String[]{"producto.largo"})));
		matchers.add(new TextComponentMatcherEditor(this.calibreField,GlazedLists.textFilterator(new String[]{"producto.calibre"})));
		
		matchers.add(new TextComponentMatcherEditor(this.kilosField,GlazedLists.textFilterator(new String[]{"kilos"})));
		matchers.add(new TextComponentMatcherEditor(this.gramosField,GlazedLists.textFilterator(new String[]{"gramos"})));
		
		matchers.add(new TextComponentMatcherEditor(this.paginaField,GlazedLists.textFilterator(new String[]{"pagina"})));
		matchers.add(new TextComponentMatcherEditor(this.columnaField,GlazedLists.textFilterator(new String[]{"columna"})));
		matchers.add(new TextComponentMatcherEditor(this.presentacionField,GlazedLists.textFilterator(new String[]{"presentacion"})));
		
		MatcherEditor editor=new CompositeMatcherEditor(matchers);
		FilterList flist=new FilterList(source,editor);
		return flist;
	}
	
	protected Action[] getDetallesActions(){
		DispatchingAction reasignarCostoAction=new DispatchingAction(this,"reAsignarCosto");
		reasignarCostoAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/folder_edit.png"));
		
		DispatchingAction actualizarCostoAction=new DispatchingAction(this,"actualizarCostos");
		actualizarCostoAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/arrow_branch.png"));
		actualizarCostoAction.putValue(Action.SHORT_DESCRIPTION, "Actualizar el costo desde las listas de precios");
		
		return new Action[]{
				getInsertAction()				
				,getDeleteAction()
				,getEditAction()
				,getFormulaAction()
				,reasignarCostoAction
				,actualizarCostoAction
				};
	}
	public void insertPartida(){
		bulkInsert();
	}


	@Override
	protected void onWindowOpened() {
		packGrid();
	}
	
	private Action formulaAction;
	
	public Action getFormulaAction(){
		if(formulaAction==null){
			formulaAction=new AbstractAction("formula"){
				public void actionPerformed(ActionEvent e) {
					aplicarFormula();
				}
			};
			formulaAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/calculator.png"));
		}
		return formulaAction;
	}
	
	protected void aplicarFormula(){
		if(!sortedPartidas.isEmpty()){
			final FormulaForm form=new FormulaForm();
			form.open();
			if(!form.hasBeenCanceled()){
				List<ListaDePreciosVentaDet> selected=new ArrayList<ListaDePreciosVentaDet>(sortedPartidas);
				for(ListaDePreciosVentaDet d:selected){
					int index=partidasSource.indexOf(d);
					form.aplicarFormula(d);
					partidasSource.set(index,d);
				}
				
			}
		}
	}
	
	public void actualizarCostos(){
		getBaseModel().actualizarCostos();
		
	}

	@Override
	protected void adjustGrid(JXTable grid) {
		grid.setColumnControlVisible(true);
		
		grid.getColumnExt("Marca").setVisible(false);
		grid.getColumnExt("Clase").setVisible(false);
		grid.getColumnExt("Kgs").setVisible(false);
		grid.getColumnExt("Grs").setVisible(false);
		
		grid.getColumnExt("% (CON)").setCellRenderer(Renderers.getPorcentageRenderer());
		grid.getColumnExt("% (CRE)").setCellRenderer(Renderers.getPorcentageRenderer());
		
		TableCellRenderer renderer=Renderers.buildBoldDecimalRenderer(2);
		
		grid.getColumnExt("Fac (CON)").setCellRenderer(renderer);
		grid.getColumnExt("Fac (CRE)").setCellRenderer(renderer);
	}
	
	
	public void deletePartida(){		
		if(getSelected()!=null){
			List selected=new ArrayList(selection.getSelected());
			for(Object sel:selected){
				doDeletePartida(sel);
			}
		}	
	}
	
	public void bulkInsert(){		
		List<Producto> list=ProductoFinder.findWithDialog(true);
		for(Producto p:list){
			getBaseModel().agregarPrecio(p);
		}
		packGrid();
	}
	
	public void configurarPagina(){
		getBaseModel().congiurarPagina(sortedPartidas);
	}
	
	public void reAsignarCosto(){
		if(!selection.isSelectionEmpty()){
			getBaseModel().reasignarCosto(selection.getSelected());
		}
	}

	public static ListaDePreciosVenta showForm(){
		final PrecioDeVentaFormModel model=new PrecioDeVentaFormModel();
		final PreciosDeVentaForm form=new PreciosDeVentaForm(model);		
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}		
		return null;
	}
	
	
	
	private static class FormulaForm extends SXAbstractDialog{
		
		JComboBox pivote;
		JComboBox operadores;
		JFormattedTextField valor;
		JComboBox tipo;

		public FormulaForm() {
			super("Formula");
			initComponents();
		}
		
		private void initComponents(){
			pivote=new JComboBox(new String[]{"Precio Anterior","Costo Reposición","Costo Ultimo"});
			operadores=new JComboBox(new String[]{"*","/"});
			tipo=new JComboBox(new String[]{"CONTADO","CREDITO"});
			NumberFormatter nf=new NumberFormatter();
			nf.setValueClass(Double.class);
			
			valor=new JFormattedTextField(nf);
		}
		
		@Override
		protected JComponent buildContentPane(){
			JPanel panel=new JPanel(new BorderLayout());
			panel.add(super.buildContentPane(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
			return panel;
		}

		
		protected JComponent buildContent() {
			FormLayout layout=new FormLayout("p,2dlu,p","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append(tipo);
			builder.append("Base",pivote);
			builder.append("Operador",operadores);
			builder.append("Valor",valor);
			return builder.getPanel();
		}
		
		public void aplicarFormula(final ListaDePreciosVentaDet det){
			String tipo=(String)this.tipo.getSelectedItem();
			if(pivote.getSelectedItem().toString().startsWith("Precio")){
				Double val=(Double)valor.getValue();
				if(val!=null){
					String operador=operadores.getSelectedItem().toString();
					
					if(operador.equals("*")){
						//Incremento
						if(tipo.startsWith("CON")){
							det.aplicarIncrementoSobrePrecioAnterior(val,true);
							det.actualizarFactor(true);
						}else{
							det.aplicarIncrementoSobrePrecioAnterior(val,false);
							det.actualizarFactor(false);
						}
					}else if(operador.equals("/")){
						if(tipo.startsWith("CON")){
							det.aplicarDecrementoSobrePrecioAnterior(val,true);
							det.actualizarFactor(false);
						}else{
							det.aplicarDecrementoSobrePrecioAnterior(val,false);
							det.actualizarFactor(false);
						}						
					}					
				}
				
			}else if(pivote.getSelectedItem().toString().equals("Costo Reposición")){				
				boolean contado=tipo.startsWith("CON");
				Double val=(Double)valor.getValue();
				if(val!=null){
					det.aplicarFactor(val,contado);
				}
			}else if(pivote.getSelectedItem().toString().equals("Costo Ultimo")){
				boolean contado=tipo.startsWith("CON");
				Double val=(Double)valor.getValue();
				if(val!=null){
					det.aplicarFactorSobreCostoUltimo(val,contado);
				}
			}
		}
		
		public static ListaDePreciosVentaDet showForm(){
			FormulaForm form=new FormulaForm();
			form.open();
			if(!form.hasBeenCanceled()){
				//return (ListaDePreciosVentaDet)model.getBaseBean();
			}
			return null;
		}		
		
	}
	
	
	public static ListaDePreciosVenta showForm(ListaDePreciosVenta lp,boolean readOnly){
		final PrecioDeVentaFormModel model=new PrecioDeVentaFormModel(lp);
		model.setReadOnly(readOnly);
		PreciosDeVentaForm form=new PreciosDeVentaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getLista();
		}		
		return null;
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
				Object bean=showForm();
				showObject(bean);
				System.exit(0);
			}

		});
	}

}
