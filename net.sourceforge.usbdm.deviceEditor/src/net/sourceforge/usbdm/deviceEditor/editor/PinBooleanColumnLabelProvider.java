package net.sourceforge.usbdm.deviceEditor.editor;

import org.eclipse.swt.graphics.Image;

import net.sourceforge.usbdm.deviceEditor.information.Pin;
import net.sourceforge.usbdm.deviceEditor.model.BaseModel;
import net.sourceforge.usbdm.deviceEditor.model.PinModel;

public abstract class PinBooleanColumnLabelProvider extends PinPropertyColumnLabelProvider {

   @Override
   public abstract String getToolTipText(Object element);

   private PinBooleanColumnLabelProvider(long mask, long offset) {
      super(mask, offset);
   }

   @Override
   public String getText(BaseModel baseModel) {
      Long value = super.getValue(baseModel);
      if (value == null) {
         return null;
      }
      return (value!=0)?"1":"0";
   }

   @Override
   public Image getImage(BaseModel baseModel) {      
      if (!(baseModel instanceof PinModel)) {
         return null;
      }
      Long value = super.getValue(baseModel);
      if (value == null) {
         return null;
      }
      return (value!=0)?checkedImage:uncheckedImage;
   }

   public static PinBooleanColumnLabelProvider getLk() {
      return new PinBooleanColumnLabelProvider(Pin.PORT_PCR_LK_MASK, Pin.PORT_PCR_LK_SHIFT) {
         @Override
         public String getToolTipText(Object element) {
            return "Lock PCR register after 1st write";
         }
      };
   }
   
   public static PinBooleanColumnLabelProvider getDse() {
      return new PinBooleanColumnLabelProvider(Pin.PORT_PCR_DSE_MASK, Pin.PORT_PCR_DSE_SHIFT) {
         @Override
         public String getToolTipText(Object element) {
            return "High Drive Strength Enable";
         }
      };
   }
   
   public static PinBooleanColumnLabelProvider getOde() {
      return new PinBooleanColumnLabelProvider(Pin.PORT_PCR_ODE_MASK, Pin.PORT_PCR_ODE_SHIFT) {
         @Override
         public String getToolTipText(Object element) {
            return "Open Drain Enable";
         }
      };
   }
   
   public static PinBooleanColumnLabelProvider getPfe() {
      return new PinBooleanColumnLabelProvider(Pin.PORT_PCR_PFE_MASK, Pin.PORT_PCR_PFE_SHIFT) {
         @Override
         public String getToolTipText(Object element) {
            return "Pin Filter Enable";
         }
      };
   }
   
   public static PinBooleanColumnLabelProvider getSre() {
      return new PinBooleanColumnLabelProvider(Pin.PORT_PCR_SRE_MASK, Pin.PORT_PCR_SRE_SHIFT) {
         @Override
         public String getToolTipText(Object element) {
            return "Slew Rate Limit Enable";
         }
      };
   }
   
}
