package net.sourceforge.usbdm.deviceEditor.model;

import java.util.ArrayList;
import java.util.Map;

import net.sourceforge.usbdm.deviceEditor.information.MappingInfo;
import net.sourceforge.usbdm.deviceEditor.information.MuxSelection;
import net.sourceforge.usbdm.deviceEditor.information.Pin;

public class PinModel extends SelectionModel implements IModelChangeListener {

   /** Associated pin */
   private final Pin fPin;

   /** Mappings corresponding to selections */
   private final MappingInfo[] fMappingInfos;

   public PinModel(BaseModel parent, Pin pin) {
      super(parent, pin.getNameWithLocation(), pin.getDescription());

      fPin = pin;
      fPin.addListener(this);

      MuxSelection currentMuxValue  = fPin.getMuxValue();

      fSelection        = 0;

      Map<MuxSelection, MappingInfo> mappingInfoMap = fPin.getMappableSignals();

      MappingInfo fixedMapping = mappingInfoMap.get(MuxSelection.fixed);
      if (fixedMapping != null) {
         // Fixed mapping for pin
         fChoices      = new String[] {fixedMapping.getSignalList()};
         fMappingInfos = null;
         return;
      }
      
      // List of values to choose from
      ArrayList<String>      values       = new ArrayList<String>();
      ArrayList<MappingInfo> mappingInfos = new ArrayList<MappingInfo>();
      
      values.add(MuxSelection.unassigned.getShortName()+": Unassigned");
      mappingInfos.add(MappingInfo.UNASSIGNED_MAPPING);
      
      for (MuxSelection muxSelection:mappingInfoMap.keySet()) {
         MappingInfo mappingInfo = mappingInfoMap.get(muxSelection);

         mappingInfos.add(mappingInfo);
         if (muxSelection == MuxSelection.unassigned) {
            values.add(muxSelection.getShortName()+": ("+mappingInfo.getSignalList()+")");
         }
         else {
            values.add(muxSelection.getShortName()+": "+mappingInfo.getSignalList());
         }
         if (muxSelection == currentMuxValue) {
            fSelection = values.size()-1;
         }
      }

      fChoices      = values.toArray(new String[values.size()]);
      fMappingInfos = mappingInfos.toArray(new MappingInfo[mappingInfos.size()]);
      
      fPin.connectListeners();
      fPin.setMuxSelection(currentMuxValue);
   }

   /**
    * Find selection index corresponding to mapping info from signal
    * 
    * @param value
    * @return
    */
   int findValueIndex(MappingInfo value) {
      if (fMappingInfos == null) {
         // Fixed mapping
         return 0;
      }
      for (int index=0; index<fMappingInfos.length; index++) {
         if (fMappingInfos[index] == value) {
            return index;
         }
      }
      return -1;
   }
   
   @Override
   public String getValueAsString() {
      return fChoices[findValueIndex(fPin.getMappedSignal())];
   }

   @Override
   public void setValueAsString(String value) {
      super.setValueAsString(value);
      fPin.setMappedSignal(fMappingInfos[fSelection]);
      checkConflicts();
   }

   @Override
   public void modelElementChanged(ObservableModel model) {
      if (model instanceof Pin) {
         update();
         checkConflicts();
      }
   }

   @Override
   public String getDescription() {
      setDescription(getPinUseDescription());
      return super.getDescription();
   }

   /**
    * Get user description of pin use
    * 
    * @return
    */
   public String getPinUseDescription() {
      return fPin.getPinUseDescription();
   }

   /**
    * Set user description of pin use
    * 
    * @return
    */
   public void setPinUseDescription(String pinUseDescription) {
      fPin.setPinUseDescription(pinUseDescription);
   }

   @Override
   public boolean isUnassigned() {
      return (fPin.getMuxValue() == MuxSelection.unassigned);
   }

   /**
    * Get Message for model
    * 
    * May return an error message from the pin mapping instead.
    */
   @Override
   Message getMessage() {
      Message rv = fPin.getMappedSignal().getMessage();
      if (rv != null) {
         return rv;
      }
      return super.getMessage();
   }

   @Override
   protected void removeMyListeners() {
      fPin.removeListener(this);
      fPin.disconnectListeners();
   }

   @Override
   public String toString() {
      return "PinModel("+fName+") => "+getValueAsString();
   }

   @Override
   public void elementStatusChanged(ObservableModel observableModel) {
      updateAncestors();
   }

   @Override
   public void modelStructureChanged(ObservableModel model) {
      // Not used
   }
}
