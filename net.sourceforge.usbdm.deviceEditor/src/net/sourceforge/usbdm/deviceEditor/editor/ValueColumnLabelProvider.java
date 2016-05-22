package net.sourceforge.usbdm.deviceEditor.editor;

import org.eclipse.swt.graphics.Image;

import net.sourceforge.usbdm.deviceEditor.Activator;
import net.sourceforge.usbdm.deviceEditor.model.BaseModel;
import net.sourceforge.usbdm.deviceEditor.model.BinaryVariableModel;
import net.sourceforge.usbdm.deviceEditor.model.EditableModel;

class ValueColumnLabelProvider extends BaseLabelProvider {
   
   private  Image lockedImage    = null;
   private  Image checkedImage   = null;
   private  Image uncheckedImage = null;
   private  Image disabledImage = null;

   ValueColumnLabelProvider(TreeEditor viewer) {
      if (Activator.getDefault() != null) {
         lockedImage    = Activator.getDefault().getImageDescriptor(Activator.ID_LOCKED_NODE_IMAGE).createImage();
         checkedImage   = Activator.getDefault().getImageDescriptor(Activator.ID_CHECKBOX_CHECKED_IMAGE).createImage();
         uncheckedImage = Activator.getDefault().getImageDescriptor(Activator.ID_CHECKBOX_UNCHECKED_IMAGE).createImage();
         disabledImage  = Activator.getDefault().getImageDescriptor(Activator.ID_DISABLED_IMAGE).createImage();
      }
   }

   @Override
   public String getText(BaseModel baseModel) {
      return baseModel.getValueAsString();
   }

   @Override
   public Image getImage(BaseModel baseModel) {
      if (!baseModel.isEnabled()) {
         return disabledImage;
      }
      if ((baseModel instanceof EditableModel) && !baseModel.canEdit()) {
         return lockedImage;
      }
      if (baseModel instanceof BinaryVariableModel) {
         return ((Boolean)((BinaryVariableModel)baseModel).getVariable().getValueAsBoolean())?checkedImage:uncheckedImage;
      }
      return null;
   }

   @Override
   public void dispose() {
      super.dispose();
      if (lockedImage != null) {
         lockedImage.dispose();
         lockedImage = null;
      }
      if (checkedImage != null) {
         checkedImage.dispose();
         checkedImage = null;
      }
      if (uncheckedImage != null) {
         uncheckedImage.dispose();
         uncheckedImage = null;
      }
      if (disabledImage != null) {
         disabledImage.dispose();
         disabledImage = null;
      }
   }

}
