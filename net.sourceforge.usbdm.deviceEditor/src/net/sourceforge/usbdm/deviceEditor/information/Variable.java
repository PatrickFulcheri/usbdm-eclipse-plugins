package net.sourceforge.usbdm.deviceEditor.information;

import net.sourceforge.usbdm.deviceEditor.model.Message;
import net.sourceforge.usbdm.deviceEditor.model.ObservableModel;

public class Variable extends ObservableModel {
   
   public enum Units {None, Hz, Seconds};
   
   private final   String  fName;
   private         String  fValue;
   private         Message fMessage = null;
   
   /** Indicates that the variable is locked and cannot be edited by user */
   private boolean fLocked = false;
   
   // Minimum permitted value
   private long fMin    = Long.MIN_VALUE;
   
   // Maximum permitted value
   private long fMax    = Long.MAX_VALUE;

   // Step size value
   private long fStep   = 1;
   
   // Offset used when applying value
   private long fOffset = 0;

   private String[] fChoices = null;
   
   /** Units of the quantity the variable represents e.g. Frequency => Hz*/
   private Units fUnits;
   
   Variable(String name, String value) {
      fName  = name;
      fValue = value;
   }

   /**
    * @return the Name
    */
   public String getName() {
      return fName;
   }

   /**
    * @return the Value
    */
   public String getValue() {
      return fValue;
   }

   /**
    * @param value The value to set
    */
   public void setValue(String value) {
//      System.err.println("Variable.setValue("+fName+", "+value+")");
      if (fValue.equals(value)) {
         return;
      }
      fValue = value;
      notifyListeners();
   }

   @Override
   public String toString() {
      return "Variable(Name=" + fName + ", value=" + fValue + ")";
   }

   /**
    * Get the variable value interpreted as a Long
    * 
    * @return
    */
   public long getValueAsLong() {
      return Long.decode(fValue);
   }

   public void setValue(long value) {
      setValue(Long.toString(value));
   }

   public void setMessage(String message) {
      if ((fMessage != null) && (message != null) && fMessage.equals(message)) {
         // No significant change
         return;
      }
      if (message == null) {
         setMessage((Message)null);
      }
      else {
         setMessage(new Message(message));
      }
   }

   public void setMessage(Message message) {
      if ((fMessage == null) && (message == null)) {
         // No change
         return;
      }
      if ((fMessage != null) && (message != null) && fMessage.equals(message)) {
         // No significant change
         return;
      }
      fMessage = message;
      notifyListeners();
   }
   
   public Message getMessage() {
      return fMessage;
   }

   public void setMin(long min) {
      fMin = min;
   }

   public void setMax(long max) {
      fMax = max;
   }

   public void setStep(long step) {
      fStep = step;
   }

   public void setOffset(long offset) {
      fOffset = offset;
   }

   public long getMin() {
      return fMin;
   }

   public long getMax() {
      return fMax;
   }

   public long getStep() {
      return fStep;
   }

   public long getOffset() {
      return fOffset;
   }

   /**
    * @return the choices
    */
   public String[] getChoices() {
      return fChoices;
   }

   /**
    * @param choices the choices to set
    */
   public void setChoices(String[] choices) {
      this.fChoices = choices;
   }

   public boolean getValueAsBoolean() {
      return (fValue.equalsIgnoreCase("1") || 
            fValue.equalsIgnoreCase("true")|| 
            fValue.equalsIgnoreCase("active")|| 
            fValue.equalsIgnoreCase("enabled"));
   }

   public void setBinaryValue(Boolean b) {
      setValue(b.toString());
   }

   /** Set if the variable is locked and cannot be edited by user
    * 
    * @return the locked
    */
   public boolean isLocked() {
      return fLocked;
   }

   /** Indicates if the variable is locked and cannot be edited by user
    * 
    * @param locked the locked to set
    */
   public void setLocked(boolean locked) {
      fLocked = locked;
   }

   /**
    * @return the units
    */
   public Units getUnits() {
      return fUnits;
   }

   /**
    * @param units The units to set
    */
   public void setUnits(Units units) {
      fUnits = units;
   }

}