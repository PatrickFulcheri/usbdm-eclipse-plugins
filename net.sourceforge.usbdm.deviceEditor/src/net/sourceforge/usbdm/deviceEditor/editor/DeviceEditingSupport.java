package net.sourceforge.usbdm.deviceEditor.editor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import net.sourceforge.usbdm.deviceEditor.model.SelectionModel;

public class DeviceEditingSupport extends EditingSupport {

   private TreeViewer viewer;

   public DeviceEditingSupport(TreeViewer viewer) {
      super(viewer);
      this.viewer = viewer;
   }

   @Override
   protected boolean canEdit(Object element) {
      if (!(element instanceof SelectionModel)) {
         return false;
      }
      SelectionModel SelectionModel = (SelectionModel)element;
      return SelectionModel.canEdit();
   }

   @Override
   protected CellEditor getCellEditor(Object element) {
      if (!(element instanceof SelectionModel)) {
         return null;
      }
      SelectionModel SelectionModel = (SelectionModel)element;

      String[] choices = SelectionModel.getValues();
      return new ChoiceCellEditor(viewer.getTree(), choices);
   }

   @Override
   protected Object getValue(Object element) {
      if (!(element instanceof SelectionModel)) {
         return null;
      }
      SelectionModel SelectionModel = (SelectionModel)element;
      return SelectionModel.getValueAsString();
   }

   @Override
   protected void setValue(Object element, Object value) {
      if (!(element instanceof SelectionModel)) {
         return;
      }
      SelectionModel SelectionModel = (SelectionModel)element;
      SelectionModel.setValue((String) value);
      
      viewer.update(element, null);
      }

   static class ChoiceCellEditor extends ComboBoxCellEditor {

      public ChoiceCellEditor(Composite tree, String[] choices) {
         super(tree, choices, SWT.READ_ONLY);
         setValueValid(true);
      }
      
      @Override
      protected Object doGetValue() {
         int index = (Integer) super.doGetValue();
         String[] items = getItems();
         if ((index<0) || (index>=items.length)) {
            index = 0;
         }
         String item = items[index];
         return item;
      }
      
      @Override
      protected void doSetValue(Object value) {
         String[] items = getItems();
         for (int index=0; index<items.length; index++) {
            if (items[index].equalsIgnoreCase(value.toString())) {
               super.doSetValue(index);
               return;
            }
         }
      }
   }
   

}
