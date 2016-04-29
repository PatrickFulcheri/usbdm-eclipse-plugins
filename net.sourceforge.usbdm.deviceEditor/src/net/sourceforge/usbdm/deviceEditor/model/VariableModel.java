package net.sourceforge.usbdm.deviceEditor.model;

/**
 * Model for a variable maintained by a provider
 */
public class VariableModel extends EditableModel {

   private final IModelEntryProvider fProvider;
   private final String              fKey;
   
   /**
    * Create model
    * 
    * @param parent        Parent model
    * @param provider      Provider that owns the variable
    * @param key           Key used to access the variable
    * @param description   Description for the display
    */
   public VariableModel(BaseModel parent, IModelEntryProvider provider, String key, String description) {
      super(parent, key, description);
      fProvider      = provider;
      fKey           = key;
   }

   @Override
   public String getValueAsString() {
      return fProvider.getVariableValue(fKey);
   }

   @Override
   public void setValueAsString(String value) {
      fProvider.setVariableValue(fKey, value);
   }

   @Override
   public boolean canEdit() {
      return true;
   }

   @Override
   protected void removeMyListeners() {
   }

}
