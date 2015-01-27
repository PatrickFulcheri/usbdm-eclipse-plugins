package net.sourceforge.usbdm.peripherals.model;

public class ClusterModel extends RegisterHolder {

   /**
    * Create ClusterModel
    * 
    * @param parent
    * @param information
    * 
    * @throws Exception
    */
   public ClusterModel(BaseModel parent, ModelInformation information) throws Exception {
      super(parent, information.getClusterName(), information.getDescription());
      address = information.getClusterAddress();
   }

   private PeripheralModel getPeripheralModel() {
      BaseModel model = this;
      while ((model != null) && !(model instanceof PeripheralModel)) {
         model = model.parent;
      }
      return (PeripheralModel)model;
   }
   
   @Override
   public MemoryBlockCache findAddressBlock(long address, long sizeInBytes) {
      PeripheralModel peripheralModel = getPeripheralModel();
      if (peripheralModel != null) {
         return peripheralModel.findAddressBlock(address, sizeInBytes);
      }
      return null;
   }

   @Override
   public String getValueAsString() throws MemoryException {
      return "";
   }

   @Override
   public void registerChanged(RegisterModel child) {
      PeripheralModel peripheralModel = getPeripheralModel();
      if (peripheralModel != null) {
         peripheralModel.registerChanged(child);
      }
   }
}
