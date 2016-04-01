package net.sourceforge.usbdm.deviceEditor.model;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;

import net.sourceforge.usbdm.deviceEditor.information.MappingInfo;
import net.sourceforge.usbdm.deviceEditor.information.MuxSelection;
import net.sourceforge.usbdm.deviceEditor.information.PinInformation;

public class PinModel extends PinMappingModel implements IModelChangeListener {

   /** Associated pin */
   protected final PinInformation fPinInformation;
   
   /** Default selection index */
   protected int fDefaultSelection = 0;

   /** Mappings corresponding to selections */
   protected final ArrayList<MuxSelection> fMappingInfos = new ArrayList<MuxSelection>();

   public PinModel(BaseModel parent, PinInformation pinInformation) {
      super(parent, pinInformation.getName(), pinInformation.getDescription());

      fPinInformation = pinInformation;
      
      pinInformation.addListener(this);

      Map<MuxSelection, MappingInfo> mappingInfoMap = pinInformation.getMappedFunctions();
      MuxSelection defaultMuxValue  = pinInformation.getDefaultValue();

      // List of values to choose from
      ArrayList<String> values = new ArrayList<String>();
      for (MuxSelection muxSelection:mappingInfoMap.keySet()) {
         if (muxSelection == MuxSelection.fixed) {
            // Uses icon so no prefix
            values.add(mappingInfoMap.get(muxSelection).getFunctionList());
            fMappingInfos.add(muxSelection);
            break;
         }
         MappingInfo mappingInfo = mappingInfoMap.get(muxSelection);
         mappingInfo.addListener(this);
         if (muxSelection == MuxSelection.reset) {
            values.add(muxSelection.getShortName()+": ("+mappingInfo.getFunctionList()+")");
         }
         else {
            values.add(muxSelection.getShortName()+": "+mappingInfo.getFunctionList());
         }
         if (muxSelection == defaultMuxValue) {
            fDefaultSelection = values.size()-1;
         }
         fMappingInfos.add(muxSelection);
      }
      if (values.size()>1) {
         // Add default entry
         values.add(values.get(fDefaultSelection).replaceFirst(".:", "D:"));
      }
      
      fValues     = values.toArray(new String[values.size()]);
      fSelection  = fDefaultSelection;
      MuxSelection mapping = fMappingInfos.get(fSelection);

      pinInformation.getMappedFunctions().get(mapping).select(MappingInfo.Origin.pin, true);
   }

   @Override
   public void setValue(String value) {
      super.setValue(value);
      if (fSelection == fValues.length-1) {
         // Last entry is the default
         fSelection = fDefaultSelection;
      }
      MuxSelection  currentMuxValue = fMappingInfos.get(fSelection);
      if (currentMuxValue == null) {
         currentMuxValue = MuxSelection.disabled;
      }
      fPinInformation.setMuxSelection(currentMuxValue);
      notifyMuxChange();
   }

   void notifyMuxChange() {
      MuxSelection  currentMuxValue = fMappingInfos.get(fSelection);
      
      MappingInfo mappingInfo;
      // De-select inactive mapping
      for (MuxSelection muxValue:fMappingInfos) {
         mappingInfo = fPinInformation.getMappedFunctions().get(muxValue);
         if ((mappingInfo != null) && (muxValue != currentMuxValue)) {
            mappingInfo.select(MappingInfo.Origin.pin, false);
         }
      }
      // Select active mapping
      mappingInfo = fPinInformation.getMappedFunctions().get(currentMuxValue);
      if (mappingInfo != null) {
         mappingInfo.select(MappingInfo.Origin.pin, true);
      }
   }

   @Override
   public void modelElementChanged(ObservableModel model) {
      if (model instanceof MappingInfo) {
         mappingChanged((MappingInfo) model);
      }      
      else if (model instanceof PinInformation) {
         pinChanged((PinInformation) model);
      }
   }
   
   public void pinChanged(PinInformation pinInformation) {
      System.err.println("Changed");
      for (TreeViewer viewer:getViewers()) {
         viewer.update(this,  null);
         viewer.update(this.fParent,  null);
      }
   }

   public void mappingChanged(MappingInfo mappingInfo) {
      if (fMessage != mappingInfo.getMessage()) {
         setMessage(mappingInfo.getMessage());
         for (TreeViewer viewer:getViewers()) {
            viewer.update(this,  null);
            viewer.update(this.fParent,  null);
         }
      }
      boolean selected     = mappingInfo.isSelected();
      int changedSelection = fMappingInfos.indexOf(mappingInfo.getMux());
      if (selected) {
         // A function has been mapped to this pin - do update if needed
         if (fSelection != changedSelection) {
            //               System.err.println(String.format("Pin(%s)::modelElementChanged(%s) = %s", fName, mappingInfo, selected?"selected":"unselected"));
            fSelection = changedSelection;
            //               notifyMuxChange();
            for (TreeViewer viewer:getViewers()) {
               viewer.update(this,  null);
            }
         }
      }
      else {
         if (fSelection == changedSelection) {
            // The current function has been unmapped from this pin 
            //               System.err.println(String.format("Pin(%s)::modelElementChanged(%s) = %s", fName, mappingInfo, selected?"selected":"unselected"));
            fSelection = 0;
            //               notifyMuxChange();
            for (TreeViewer viewer:getViewers()) {
               viewer.update(this,  null);
            }
         }
      }
   }

   /**
    * Get description of pin use
    * 
    * @return
    */
   public String getPinUseDescription() {
      return fPinInformation.getPinUseDescription();
   }
   
   /**
    * Set description of pin use
    * 
    * @return
    */
   public void setPinUseDescription(String pinUseDescription) {
      fPinInformation.setPinUseDescription(pinUseDescription);
      // Update watchers of active mapping
      MappingInfo mappingInfo = fPinInformation.getMappedFunctions().get(fMappingInfos.get(fSelection));
      mappingInfo.notifyListeners();
   }
   
   @Override
   public void modelStructureChanged(ObservableModel model) {
      // Not used
   }


   @Override
   public boolean isReset() {
      return (fMappingInfos.get(fSelection) == MuxSelection.reset);
   }

   public boolean isUnmapped() {
      MappingInfo mappingInfo = fPinInformation.getMappedFunctions().get(fMappingInfos.get(fSelection));
      return mappingInfo.getPin() == PinInformation.DISABLED_PIN;
   }

}
