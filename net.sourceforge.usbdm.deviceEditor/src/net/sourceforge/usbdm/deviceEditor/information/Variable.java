package net.sourceforge.usbdm.deviceEditor.information;

import java.util.Map;

import net.sourceforge.usbdm.cdt.utilties.ReplacementParser;
import net.sourceforge.usbdm.deviceEditor.model.BaseModel;
import net.sourceforge.usbdm.deviceEditor.model.Status;
import net.sourceforge.usbdm.deviceEditor.model.ObservableModel;
import net.sourceforge.usbdm.deviceEditor.model.VariableModel;
import net.sourceforge.usbdm.deviceEditor.peripherals.VariableProvider;
import net.sourceforge.usbdm.deviceEditor.model.Status.Severity;

public abstract class Variable extends ObservableModel implements Cloneable {
   
   static DeviceInfo fDeviceInfo=null;
   
   /**
    * Set device info object used to register variables
    * 
    * @param deviceInfo
    */
   static void setDeviceInfo(DeviceInfo deviceInfo) {
      fDeviceInfo = deviceInfo;
   }
   
   /**
    * Units for physical quantities
    */
   public enum Units {None, Hz, s};

   /**
    * Class to hold the Name/Value pair
    */
   public static class Pair {
      
      /** Name used by GUI/model */
      public final String name;
      
      /** Value used by substitution */
      public final String value;
      
      /**
       * 
       * @param name  Name used by GUI/model
       * @param value Value used by data
       */
      public Pair(String name, String value) {
         this.name  = name;
         this.value = value;
      }
   }

   /** Name of variable visible to user */
   private  String  fName;
   
   /** Key used to identify variable */
   private  String  fKey;
   
   /** Indicates that the variable is locked and cannot be edited by user */
   private boolean fLocked = false;

   /** Indicates the variable is disabled */
   private boolean fEnabled = true;

   /** Description of variable */
   private String fDescription = null;

   /** Tool tip for this variable */
   private String fToolTip = null;
   
   /** Status of variable */
   private Status fStatus = null;
   
   /** Origin of variable value */
   private String fOrigin = null;

   /** Indicates this variable is derived (calculated) from other variables */
   private boolean fDerived = false;

   protected boolean fDebug = false;

   private String fForEnumeration;

   private String fForVariable;

   /**
    * Constructor
    * 
    * @param name Name to display to user.
    * @param key  Key for variable
    */
   public Variable(String name, String key) {
      fName = name;
      fKey  = key;
   }

   /**
    * @return the Name
    */
   public String getName() {
      return fName;
   }

   /**
    * @return the key
    */
   public String getKey() {
      return fKey;
   }

   /**
    * @param key The key to set
    */
   public void setKey(String key) {
      this.fKey = key;
   }

   /**
    * Get the variable value as a string for use in substitutions
    * 
    * @return String for text substitutions (in C code)
    */
   public abstract String getSubstitutionValue();

   /**
    * Get variable value as a string suitable for user display
    * 
    * @return String for display
    */
   public abstract String getValueAsString();
   
   /**
    * Sets variable value<br>
    * Listeners are informed if the variable changes
    * 
    * @param value The value to set
    * 
    * @return True if variable actually changed value
    */
   public abstract boolean setValue(Object value);

   /**
    * Sets variable value without affecting listeners
    * 
    * @param value The value to set
    */
   public abstract void setValueQuietly(Object value);

   /**
    * Get the variable value as a string for use in saving state
    * 
    * @return the Value
    */
   public abstract String getPersistentValue();

   /**
    * Set the variable value from a string used in restoring state<br>
    * Listeners are not affected
    * 
    * @param value The value to restore
    */
   public abstract void setPersistentValue(String value);

   /**
    * Sets variable default value
    * 
    * @param value The value to set
    */
   public abstract void setDefault(Object value);

   /**
    * Sets variable disabled value
    * 
    * @param value The value to set
    */
   public abstract void setDisabledValue(Object value);

   /**
    * Gets variable default value
    * 
    * @return The default value
    */
   public abstract Object getDefault();

   private String getSimpleClassName() {
      String s = getClass().toString();
      int index = s.lastIndexOf(".");
      return s.substring(index+1, s.length());
   }
   
   @Override
   public String toString() {
      return String.format(getSimpleClassName()+"(Name=%s, Key=%s, value=%s (%s))", getName(), getKey(), getSubstitutionValue(), getValueAsString());
   }

   /**
    * Set error status of variable
    * 
    * @param message
    */
   public void setStatus(String message) {
      if ((fStatus != null) && (message != null) && fStatus.equals(message)) {
         // No significant change
         return;
      }
      if (message == null) {
         setStatus((Status)null);
      }
      else {
         setStatus(new Status(message));
      }
   }

   /**
    * Set status of variable
    * 
    * @param message
    */
   public void setStatus(Status message) {
      if ((fStatus == null) && (message == null)) {
         // No change
         return;
      }
      if ((fStatus != null) && (message != null) && fStatus.equals(message)) {
         // No significant change
         return;
      }
      fStatus = message;
      notifyListeners();
   }
   
   /**
    * Clear status of variable
    */
   public void clearStatus() {
      setStatus((Status) null);
   }
   
   /**
    * Get status of variable
    * 
    * @return
    */
   public Status getStatus() {
      if (!isEnabled()) {
         if (fStatus != null) {
            return new Status(fStatus.getText(), Severity.INFO);
         }
         return null;
      }
      String status = isValid();
      if (status != null) {
         return new Status(status, Severity.ERROR);
      }
      return fStatus;
   }

   /**
    * Get status of variable<br>
    * Filters out status messages with less than INFO severity
    * 
    * @return
    */
   public Status getFilteredStatus() {
      if ((fStatus != null) && (fStatus.getSeverity().greaterThan(Severity.INFO))) {
         return fStatus;
      }
      return null;
   }

   /**
    * Get the origin of variable value<br>
    * This is intended to indicate how the value originated or is derived (calculated)<br>
    * Defaults to the description if not explicitly set by setOrigin().
    * 
    * @return The origin
    */
   public String getOrigin() {
      return (fOrigin!=null)?fOrigin:fDescription;
   }

   /**
    * Get the origin of variable value<br>
    * This is intended to indicate how the value originated or is derived (calculated)
    * 
    * @return The origin
    */
   public String getRawOrigin() {
      return fOrigin;
   }

   /**
    * Set the origin of variable value
    * 
    * @param origin The origin to set
    */
   public void setOrigin(String origin) {
      if ((fOrigin == null) && (origin == null)) {
         // No change
         return;
      }
      if ((fOrigin != null) && (fOrigin.equalsIgnoreCase(origin))) {
         // No significant change
         return;
      }
      fOrigin = origin;
      notifyListeners();
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
    * @param locked The locked state to set
    * 
    * @return True if variable actually changed lock state
    */
   public boolean setLocked(boolean locked) {
      if (fLocked == locked) {
         return false;
      }
      fLocked = locked;
      notifyListeners();
      return true;
   }

   /**
    * Get value as a boolean
    * 
    * @return Value as boolean
    */
   public boolean getValueAsBoolean() {
      throw new RuntimeException(this+"("+getClass()+") is not compatible with boolean" );
   }

   /**
    * Get value as a boolean without reference to whether it is enabled
    * 
    * @return value as boolean
    */
   public boolean getRawValueAsBoolean() {
      throw new RuntimeException(this+"("+getClass()+") is not compatible with boolean" );
   }

   /**
    * Get the value as a long
    * 
    * @return Value as long (if supported)
    */
   public long getValueAsLong() {
      throw new RuntimeException(this+"("+getClass()+") is not compatible with long" );
      }

   /**
    * Get variable value as long without reference to whether it is enabled
    * 
    * @return Raw value as long (if supported)
    */
   public long getRawValueAsLong() {
      throw new RuntimeException(this+"("+getClass()+") is not compatible with long" );
   }

   /**
    * Get value as a double if representable
    * 
    * @return Value as double (if supported)
    */
   public double getValueAsDouble() {
      throw new RuntimeException(this+"("+getClass()+") is not compatible with double" );
   }

   /**
    * Get value as a double without reference to whether it is enabled
    * 
    * @return Raw value as double (if supported)
    */
   public double getRawValueAsDouble() {
      throw new RuntimeException(this+"("+getClass()+") is not compatible with double" );
   }

   /**
    * Checks if the value is valid for assignment to this variable
    * 
    * @param value String to validate
    * 
    * @return Error message or null if valid
    */
   public String isValid(String value) {
      return null;
   }

   /**
    * Checks if the current variable value is valid
    * 
    * @return Error message or null if valid
    */
   public String isValid() {
      return null;
   }
   
   /**
    * Checks is a character is 'plausible' for this variable<br>
    * Used to validate initial text entry in dialogues<br>
    * Used to restrict key entry when editing.
    * 
    * @param character Character to validate
    * 
    * @return Error message or null if valid
    */
   public String isValidKey(char character) {
      return null;
   }

   /**
    * Set the enabled state of variable
    * 
    * @param enabled State to set
    * 
    * @return true if the enabled state changed
    */
   public boolean enable(boolean enabled) {
      if (fEnabled == enabled) {
         return false;
      }
      fEnabled = enabled;
      notifyListeners();
      notifyStatusListeners();
      return true;
   }

   /**
    * @return The enabled state of variable
    */
   public boolean isEnabled() {
      return fEnabled;
   }

   /**
    * Gets description of variable
    * 
    * @return string
    */
   public String getDescription() {
      return fDescription;
   }

   /**
    * Set description of variable
    * 
    * @param description
    */
   public void setDescription(String description) {
      fDescription = description;
   }

   /**
    * Set tool tip
    * 
    * @param toolTip
    */
   public void setToolTip(String toolTip) {
      fToolTip = toolTip;
   }

   /**
    * Get tool tip
    * 
    * @return toolTip
    */
   public String getToolTip() {
      return fToolTip;
   }

   /**
    * Get tool tip.<br>
    * This will be constructed from:
    * <li>Status e.g. warning etc. {@link #setStatus(Status)}
    * <li>Explicitly set Tooltip {@link #setToolTip(String)}
    * <li>Origin {@link #setOrigin(String origin)})
    * 
    * @return String
    */
   public String getDisplayToolTip() {
      StringBuilder sb = new StringBuilder();
      Status status = getStatus();
      if (status != null) {
         if (status.greaterThan(Status.Severity.WARNING)) {
            sb.append(status.getText());
         }
         else if (status != null) {
            sb.append(status.getSimpleText());
         }
      }
      if (fToolTip != null) {
         if (sb.length() != 0) {
            sb.append('\n');
         }
         sb.append(fToolTip);
      }
      if (fOrigin != null) {
         if (sb.length() != 0) {
            sb.append('\n');
         }
         sb.append("Origin: ");
         sb.append(fOrigin);
      }
      return (sb.length()==0)?null:sb.toString();
   }

   /**
    * Creates model for displaying this variable
    * 
    * @param parent Parent for the new model
    * 
    * @return {@link VariableModel}
    */
   public abstract VariableModel createModel(BaseModel parent);

   /**
    * Print string if debugging on
    * 
    * @param string
    */
   public void debugPrint(String string) {
      if (fDebug) {
         System.err.println(string);
      }
   }
   
   /** 
    * Set if this variable is derived (calculated) from other variables 
    * 
    * @param derived 
    */
   public void setDerived(boolean derived) {
      fDerived = derived;
   }
   
   /** 
    * Get if this variable is derived (calculated) from other variables 
    * 
    * @return true if derived 
    */
   public boolean getDerived() {
      return fDerived;
   }
   
   /** 
    * Get if this variable is derived (calculated) from other variables 
    * 
    * @return  
    */
   public boolean isDerived() {
      return fDerived;
   }

   /**
    * Indicates that the variable value corresponds to the default.
    * 
    * @return
    */
   public abstract boolean isDefault();

   /**
    * Return clone of object
    * 
    * @param name 
    * @param symbols
    * 
    * @return Clone
    * 
    * @throws CloneNotSupportedException
    * 
    * @note All listeners are removed from clone
    */
   public Variable clone(String name, Map<String,String> symbols) throws CloneNotSupportedException {
      Variable var = null;
      // Create cloned variable
      var = (Variable) super.clone();
      var.removeAllListeners();
      var.fName         = ReplacementParser.substitute(fName, symbols);
      var.fKey          = ReplacementParser.substitute(fKey, symbols);
      var.fToolTip      = ReplacementParser.substitute(fToolTip, symbols);
      var.fDescription  = ReplacementParser.substitute(fDescription, symbols);
      var.fOrigin       = ReplacementParser.substitute(fOrigin, symbols);
      return var;
   }

   /**
    * Return indexed clone of object (if necessary)
    * 
    * @param provider Provider to register cloned variable with
    * @param index    Index used to modify variable name
    * 
    * @return Clone with modified name or original object if name it is not indexed
    * 
    * @throws CloneNotSupportedException
    * 
    * @note All listeners are removed from clone
    */
   public Variable clone(VariableProvider provider, int index) throws CloneNotSupportedException {
      if (!fName.matches("^.*\\[\\d+\\]$")) {
         // Not indexed - just return itself
         return this;
      }
      Variable var = null;
      String key = fKey.replaceAll("\\[\\d+\\]$", "["+index+"]");
      var = fDeviceInfo.safeGetVariable(key);
      if (var != null) {
         // Use existing variable - probably generated by an alias
         return var;
      }
      // Create cloned variable
      var = (Variable) super.clone();
      var.fName = fName.replaceAll("\\[\\d+\\]$", "["+index+"]");
      var.fKey  = fKey.replaceAll("\\[\\d+\\]$", "["+index+"]");
      var.removeAllListeners();
      provider.addVariable(var);
      return var;
   }

   /**
    * Reset to default value
    */
   public void reset() {
      setValue(getDefault());
   }
   
   public void setDebug(boolean value) {
      fDebug = value;
   }

   public void addForIteration(String forVariable, String enumeration) {
      fForVariable = forVariable;
      fForEnumeration = enumeration;
   }
   
   public String getForEnumeration() {
      return fForEnumeration;
   }
   
   public String getForVariable() {
      return fForVariable;
   }
}