package com.luxsoft.siipap.swing.utils;


import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;


import com.luxsoft.siipap.swing.controls.PlasticFieldCaret;



public class NumberCellEditor extends DefaultCellEditor {
    
    private static Class[] argTypes = new Class[]{String.class};
    java.lang.reflect.Constructor constructor;
    
    public NumberCellEditor() {
        this(null);
    }
    public NumberCellEditor(NumberFormat formatter) {
        super(createFormattedTextField(formatter));
        final JFormattedTextField textField = ((JFormattedTextField)getComponent());
        
        textField.setName("Table.editor");
        textField.setHorizontalAlignment(JTextField.RIGHT);
        
        // remove action listener added in DefaultCellEditor
        textField.removeActionListener(delegate);
        // replace the delegate created in DefaultCellEditor
        delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    ((JFormattedTextField)getComponent()).setValue(value);
                }

                public Object getCellEditorValue() {
                    JFormattedTextField textField = ((JFormattedTextField)getComponent());
                    try {
                        textField.commitEdit();
                        return textField.getValue();
                    } catch (ParseException ex) {
                        return null;
                    }
                }
        };
        textField.addActionListener(delegate);
    }
    
    @Override
    public boolean stopCellEditing() {
        // If the user tries to tab out of the field, the textField will call stopCellEditing().
        // Check for a valid edit, and don't let the focus leave until the edit is valid.
        if (!((JFormattedTextField) editorComponent).isEditValid()) return false;
        return super.stopCellEditing();
    }
    
    
    
    /** Override and set the border back to normal in case there was an error previously */
    public Component getTableCellEditorComponent(JTable table, Object value,
                                             boolean isSelected,
                                             int row, int column) {
        ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
        try {
            final Class type = table.getColumnClass(column);
            // Assume that the Number object we are dealing with has a constructor which takes
            // a single string parameter.
            if (!Number.class.isAssignableFrom(type)) {
                throw new IllegalStateException("NumberEditor can only handle subclasses of java.lang.Number");
            }
            constructor = type.getConstructor(argTypes);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Number subclass must have a constructor which takes a string", ex);
        }
        JTextField tf=(JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        tf.selectAll();
        return tf;
    }
    
    @Override
    public Object getCellEditorValue() {
        Number number = (Number) super.getCellEditorValue();
        if (number==null) return 0;
        // we use a String value as an intermediary between the Number object returned by the 
        // the NumberFormat and the kind of Object the column wants.
        try {
            return constructor.newInstance(new Object[]{number.toString()});
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("NumberEditor not propertly configured", ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException("NumberEditor not propertly configured", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("NumberEditor not propertly configured", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("NumberEditor not propertly configured", ex);
        }
    }


    /**
     * Use a static method so that we can do some stuff before calling the
     * superclass.
     */
    private static JFormattedTextField createFormattedTextField(
            NumberFormat formatter) {
    	//NumberEditorNumberFormat ff=new NumberEditorNumberFormat(formatter);
    	/*NumberFormatter numberFormatter=new NumberFormatter(NumberFormat.getInstance());
    	numberFormatter.setAllowsInvalid(false);
    	numberFormatter.setCommitsOnValidEdit(true);
    	numberFormatter.setMinimum(0);
    	numberFormatter.setValueClass(Double.class);
        final JFormattedTextField textField = new JFormattedTextField(
               numberFormatter);
        */
    	final JFormattedTextField textField = new JFormattedTextField(
               new NumberEditorNumberFormat(formatter));
        
        /*
         * FIXME: I am sure there is a better way to do this, but I don't know
         * what it is. JTable sets up a binding for the ESCAPE key, but
         * JFormattedTextField overrides that binding with it's own. Remove the
         * JFormattedTextField binding.
         */
        InputMap map = textField.getInputMap();
        while (map != null) {
            map.remove(KeyStroke.getKeyStroke("pressed ESCAPE"));
            map = map.getParent();
        }
        /*
         * Set an input verifier to prevent the cell losing focus when the value
         * is invalid
         */
        textField.setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent input) {
                JFormattedTextField ftf = (JFormattedTextField) input;
                return ftf.isEditValid();
            }
        });
        /*
         * The formatted text field will not call stopCellEditing() until the
         * value is valid. So do the red border thing here.
         */
        textField.addPropertyChangeListener("editValid",
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getNewValue() == Boolean.TRUE) {
                            ((JFormattedTextField) evt.getSource())
                                    .setBorder(new LineBorder(Color.black));
                           
                        } else {
                            ((JFormattedTextField) evt.getSource())
                                    .setBorder(new LineBorder(Color.red));
                           
                        }
                    }
                });
        return textField;
    }
    
    static class NumberEditorNumberFormat extends Format {
        private final NumberFormat childFormat;

        public NumberEditorNumberFormat(NumberFormat childFormat) {
            if (childFormat == null) {
                childFormat = NumberFormat.getInstance();
            }
            this.childFormat = childFormat;
        }

        @Override
        public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
            if (obj == null)
                return new AttributedString("").getIterator();
            return childFormat.formatToCharacterIterator(obj);
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo,
                FieldPosition pos) {
            if (obj == null)
                return new StringBuffer("");
            return childFormat.format(obj, toAppendTo, pos);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            if (source == null) {
                pos.setIndex(1); // otherwise Format thinks parse failed
                return null;
            }
            if (source.trim().equals("")) {
                pos.setIndex(1); // otherwise Format thinks parse failed
                return null;
            }
            Object val = childFormat.parseObject(source, pos);
            /*
             * The default behaviour of Format objects is to keep parsing as long as
             * they encounter valid data. By for table editing we don't want
             * trailing bad data to be considered a "valid value". So set the index
             * to 0 so that the parse(Object) method knows that we had an error.
             */
            if (pos.getIndex() != source.length()) {
                pos.setErrorIndex(pos.getIndex());
                pos.setIndex(0);
            }
            return val;
        }
    }
    
    
}

