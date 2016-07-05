package net.sourceforge.usbdm.deviceEditor.model;

import net.sourceforge.usbdm.deviceEditor.information.BitmaskVariable;
import net.sourceforge.usbdm.deviceEditor.model.Message.Severity;

public class BitmaskModel extends VariableModel {

   /**
    * 
    * @param parent        Parent model
    * @param provider      Associated variable provider
    * @param key           Variable key
    * @param description   Description for model
    * 
    * @note Added as child of parent if not null
    */
   public BitmaskModel(BaseModel parent, BitmaskVariable variable) {
      super(parent, variable);
   }
   
   @Override
   public BitmaskVariable getVariable() {
      return (BitmaskVariable)super.getVariable();
   }

   @Override
   public void setValueAsString(String value) {
      getVariable().setValue(value);
   }

   /**
    * Converts the given string into a form appropriate for this  model
    * 
    * @param value Value to format
    * 
    * @return String in appropriate form e.g. 24.56MHz
    */
   private String getValueAsString(long value) {
      return getVariable().getValueAsString(value);
   }

   @Override
   public String getValueAsString() {
      return getVariable().getValueAsString();
   }

   @Override
   Message getMessage() {
      Message msg = super.getMessage();
      if ((msg != null) && msg.greaterThan(Severity.WARNING)) {
         return msg;
      }
      String message = isValid(getValueAsString());
      if (message != null) {
         msg = new Message(message);
      }
      return msg;
   }

   @Override
   public String getToolTip() {
      StringBuffer sb = new StringBuffer();
      sb.append(super.getToolTip());
      boolean newLineNeeded = sb.length()>0;
      
      if (getVariable().getMin() != Long.MIN_VALUE) {
         if (newLineNeeded) {
            sb.append("\n");
            newLineNeeded = false;
         }
         sb.append("min="+getValueAsString(getVariable().getMin())+" ");
      }
      if (getVariable().getMax() != Long.MAX_VALUE) {
         if (newLineNeeded) {
            sb.append("\n");
            newLineNeeded = false;
         }
         sb.append("max="+getValueAsString(getVariable().getMax())+" ");
      }
      if (getVariable().getStep() != 1) {
         if (newLineNeeded) {
            sb.append("\n");
            newLineNeeded = false;
         }
         sb.append("step="+getValueAsString(getVariable().getStep())+" ");
      }
      return (sb.length() == 0)?null:sb.toString();
   }

}
