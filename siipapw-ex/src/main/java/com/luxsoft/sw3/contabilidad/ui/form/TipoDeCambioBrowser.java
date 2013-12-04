package com.luxsoft.sw3.contabilidad.ui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.TipoDeCambio;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.SWXAction;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;


/**
 *  Browser para el mantenimiento de entidades de tipo {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class TipoDeCambioBrowser extends SXAbstractDialog{
	
	JTextField inputField=new JTextField(20);
	
	public TipoDeCambioBrowser() {
		super("Tipos de cambio");
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
		return new HeaderPanel("Tipos de cambio por día","");
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
		TipoDeCambioBrowser dialog=new TipoDeCambioBrowser();
		dialog.open();
	}
	

	public static class ClientesFilteredPanel extends FilteredBrowserPanel<TipoDeCambio> implements ActionListener{
		
		private JTextField fecha;
		private JTextField moneda;

		public ClientesFilteredPanel() {
			super(TipoDeCambio.class);
			
			
		}
		
		public void init(){
			addProperty("fecha","factor");
			addLabels("Fecha","T.C.");
			fecha=new JTextField(10);
			fecha.addActionListener(this);
			moneda=new JTextField(10);
			moneda.addActionListener(this);
			//installTextComponentMatcherEditor("Fecha", GlazedLists.textFilterator(new String[]{"clave"}),moneda);
			installTextComponentMatcherEditor("Fecha", GlazedLists.textFilterator(new String[]{"fecha"}),fecha);
			ComponentUtils.addF2Action(fecha, getLoadAction());
			//ComponentUtils.addF2Action(moneda, getLoadAction());
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

		@Override
		public boolean doDelete(TipoDeCambio bean) {
			ServiceLocator2.getUniversalDao().remove(TipoDeCambio.class, bean.getId());
			return true;
		}

		@Override
		protected TipoDeCambio doEdit(TipoDeCambio bean) {			
			TipoDeCambio target=(TipoDeCambio)ServiceLocator2.getUniversalDao().get(TipoDeCambio.class, bean.getId());
			DefaultFormModel model=new DefaultFormModel(Bean.proxy(TipoDeCambio.class));
			BeanUtils.copyProperties(target, model.getBaseBean(),new String[]{"id"});
			TipoDeCambioForm form=new TipoDeCambioForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				target.setFactor((Double)model.getValue("factor"));
				target.setFecha((Date)model.getValue("fecha"));
				return (TipoDeCambio)ServiceLocator2.getUniversalDao().save(target);
			}
			return bean;
		}

		@Override
		protected void doSelect(Object o) {			
			TipoDeCambio target=(TipoDeCambio)o;
			DefaultFormModel model=new DefaultFormModel(Bean.proxy(TipoDeCambio.class));
			model.setReadOnly(true);
			BeanUtils.copyProperties(target, model.getBaseBean(),new String[]{"id"});
			TipoDeCambioForm form=new TipoDeCambioForm(model);
			form.open();
		}

		@Override
		protected TipoDeCambio doInsert() {
			DefaultFormModel model=new DefaultFormModel(Bean.proxy(TipoDeCambio.class));
			TipoDeCambioForm form=new TipoDeCambioForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				TipoDeCambio target=new TipoDeCambio();			
				target.setFactor((Double)model.getValue("factor"));
				target.setFecha((Date)model.getValue("fecha"));
				return (TipoDeCambio)ServiceLocator2.getUniversalDao().save(target);
			}
			return null;
		}

		protected List<TipoDeCambio> findData(){			
			return ServiceLocator2.getHibernateTemplate().find("from TipoDeCambio t order by t.fecha desc");
			
		}

		public void actionPerformed(ActionEvent e) {
			load();
		}
		
		@Override
		protected void adjustMainGrid(JXTable grid) {
			grid.getColumnExt("T.C.").setCellRenderer(Renderers.buildBoldDecimalRenderer(4));
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
				TipoDeCambioBrowser browser=new TipoDeCambioBrowser();
				browser.open();
				//System.exit(0);
			}

		});
	}

}
