package net.sourceforge.usbdm.deviceEditor.information;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.DialogSettings;

import net.sourceforge.usbdm.deviceEditor.information.MappingInfo.Origin;
import net.sourceforge.usbdm.deviceEditor.model.IModelChangeListener;
import net.sourceforge.usbdm.deviceEditor.model.ObservableModel;

/**
 * Information about a pin<br>
 * <li>Pin name
 * <li>Peripheral signals mapped to that pin
 */
public class Pin extends ObservableModel implements Comparable<Pin>, IModelChangeListener {
   
   /** Pin used to denote a disabled mapping */
   public final static Pin DISABLED_PIN = new Pin(null, "Disabled");
   
   /** Pin comparator */
   public static Comparator<String> comparator = Utils.comparator;
   
   /*
    * ==================================================================================
    */
   /** Port instance e.g. PTA3 => A (only if has PCR) */
   private String fPortInstance = null;

   /** Pin instance e.g. PTA3 => 3 (only if has PCR) */
   private String fPortPin = null;
   
   /** Name of the pin, usually the port name e.g. PTA1 */
   private String fName;
   
   /** Map of signals mapped to this pin ordered by mux value */
   private Map<MuxSelection, MappingInfo> fMappedSignals = new TreeMap<MuxSelection, MappingInfo>();

   /** Multiplexor value at reset */
   private MuxSelection fResetMuxValue = MuxSelection.unused;

   /** Default multiplexor value */
   private MuxSelection fDefaultMuxValue = MuxSelection.unused;

   /** User description of pin use */
   private String fPinUseDescription = "";

   /** Current multiplexor setting */
   private MuxSelection fMuxValue = MuxSelection.reset;
   
   /** Device info owning this pin */
   private DeviceInfo fDeviceInfo;

   /**
    * Create empty pin function for given pin
    * @param deviceInfo 
    * 
    * @param fName Name of the pin, usually the port name e.g. PTA1
    */
   Pin(DeviceInfo deviceInfo, String name) {
      fDeviceInfo = deviceInfo;
      this.fName  = name;
      Pattern p = Pattern.compile("^\\s*PT(.)(\\d*)\\s*$");
      Matcher m = p.matcher(name);
      if (m.matches()) {
         fPortInstance = m.group(1);
         fPortPin      = m.group(2);
      }
   }
   
   /**
    * Connect Pin as listener for changes on pin multiplexing
    */
   public void connectListeners() {
      for (MuxSelection muxValue:fMappedSignals.keySet()) {
         MappingInfo mappingInfo = fMappedSignals.get(muxValue);
         mappingInfo.addListener(this);
      }
   }
   
   /**
    * Disconnect Pin as listener for changes on pin multiplexing
    */
   public void disconnectListeners() {
      for (MuxSelection muxValue:fMappedSignals.keySet()) {
         MappingInfo mappingInfo = fMappedSignals.get(muxValue);
         mappingInfo.removeListener(this);
      }
   }
   
   /**
    * Get PCR register e.g. PORTA->PCR[3]
    * 
    * @return
    */
   public String getPCR() {
      if (fPortInstance == null) {
         return "0";
      }
      return String.format("&PORT%s->PCR[%s]", fPortInstance, fPortPin);
   }
   
   /**
    * Get Gpio class declaration e.g. GpioA<3>
    * 
    * @return
    */
   public String getGpioClass() {
      return String.format("Gpio%s<%s>", fPortInstance, fPortPin);
   }
   
   /**
    * Get Base pointer for Port e.g. PORTC_BasePtr
    * 
    * @return
    */
   public String getPORTBasePtr() {
      if (fPortInstance == null) {
         return null;
      }
      return String.format("PORT%s_BasePtr", fPortInstance);
   }
   
   /**
    * Get PCR register address as integer e.g. PORTC_BasePtr+offsetof(PORT_Type,PCR[2])
    * 
    * @return
    */
   public String getPCRasInt() {
      if (fPortInstance == null) {
         return null;
      }
      return String.format("%s+offsetof(PORT_Type,PCR[%s])", getPORTBasePtr(), fPortPin);
   }
   
   /**
    * Get clock mask e.g. PORTA_CLOCK_MASK
    * 
    * @return
    */
   public String getClockMask() {
      if (fPortInstance == null) {
         return null;
      }
      return String.format("PORT%s_CLOCK_MASK", fPortInstance);
   }
   
   /**
    * Get clock mask e.g. PORTA_CLOCK_MASK
    * 
    * @return
    */
   public String getGpioReg() {
      if (fPortInstance == null) {
         return null;
      }
      return String.format("GPIO%s_BasePtr", fPortInstance);
   }
   
   /**
    * Get clock mask e.g. PORTA_CLOCK_MASK
    * 
    * @return
    */
   public String getGpioBitNum() {
      if (fPortInstance == null) {
         return null;
      }
      return fPortPin;
   }
   
   /**
    * Get name of the pin, usually the port name e.g. PTA1 
    * 
    * @return Pin name
    */
   public String getName() {
      return fName;
   }
   
   /**
    * Get location of the pin e.g. p23 
    * 
    * @return Pin name
    */
   public String getLocation() {
      String location = null;
      if (fDeviceInfo != null) {
         location = fDeviceInfo.getDeviceVariant().getPackage().getLocation(this);
      }
      return location;
   }
   
   /**
    * Indicates if the pin is available in the package
    * 
    * @return
    */
   public boolean isAvailableInPackage() {
      if (this == DISABLED_PIN) {
         // The disabled pin is always available
         return true;
      }
      return getLocation() != null;
   }

   /**
    * Get name of the pin with package location e.g. PTA1(p36) 
    * 
    * @return Pin name
    */
   public String getNameWithLocation() {
      String location = getLocation();
      if ((location == null) || (location.length() == 0)) {
         return fName;
      }
      else {  
         return fName + " ("+location+")";
      }
   }
   
   /**
    * Get description of functions mapped to this pin
    * 
    * @return Description
    */
   public String getDescription() {
      return "";
   }
    
   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("Pin("+fName+",\n   R:"+fResetMuxValue+",\n   D:"+fDefaultMuxValue+",\n   C:"+fMuxValue);
      for (MuxSelection muxSelection:fMappedSignals.keySet()) {
         sb.append(",\n   "+fMappedSignals.get(muxSelection).toString());
      }
      sb.append(")");
      return sb.toString();
   }

   /**
    * Sets the reset pin mapping
    * 
    * @param mux reset peripheral function on this pin<br>
    * Note - The value should never be <b>MuxSelection.reset</b> i.e. it is the actual mux value used for reset
    * 
    * @throws RuntimeException If pin already has default or new default not found as available pin mapping
    */
   public void setResetValue(MuxSelection mux) {
      if ((fResetMuxValue != mux) && (fResetMuxValue != MuxSelection.unused)) {
         throw new RuntimeException("Pin "+fName+" already has reset value "+fResetMuxValue);
      }
      if (fResetMuxValue == MuxSelection.reset) {
         throw new RuntimeException("Pin "+fName+" illegal reset value "+mux);
      }
      fResetMuxValue = mux;
   }

   /**
    * Returns the reset pin mapping
    * 
    * @return reset peripheral function on this pin
    */
   public MuxSelection getResetValue() {
      return fResetMuxValue;
   }

   /**
    * Sets the default pin mapping
    * 
    * @param mux default peripheral function on this pin
    * 
    * @throws RuntimeException If pin already has default or new default not found as available pin mapping
    */
   public void setDefaultValue(MuxSelection mux) {
      if ((fDefaultMuxValue != mux) && (fDefaultMuxValue != MuxSelection.unused)) {
         throw new RuntimeException("Pin "+fName+" already has default value "+fDefaultMuxValue);
      }
      // Set current value to default if not already set
      if ((fMuxValue == MuxSelection.unused) || (fMuxValue == MuxSelection.reset)) {
         setMuxSelection(mux);
      }
      fDefaultMuxValue = mux;
   }

   /**
    * Returns the default pin mapping
    * 
    * @return default peripheral function on this pin
    */
   public MuxSelection getDefaultValue() {
      if (fMuxValue == MuxSelection.unused) {
         setMuxSelection(fResetMuxValue);
      }
      return fDefaultMuxValue;
   }

   /**
    * Sets the default signal mapping for the pin
    * 
    * @param defaultSignals  Name of signals to look for e.g. <b><i>GPIOE_1/LLWU_P0</i></b>
    * 
    * @throws Exception If pin already has default or value not available pin mapping
    */
   public void setDefaultSignals(DeviceInfo factory, final String defaultSignals) {
      for (MuxSelection muxValue:fMappedSignals.keySet()) {
         MappingInfo mappingInfo= fMappedSignals.get(muxValue);
         if (mappingInfo.getSignalList().equalsIgnoreCase(defaultSignals) && (mappingInfo.getMux() != MuxSelection.reset)) {
            setDefaultValue(mappingInfo.getMux());
            break;
         }
      }
      if (fDefaultMuxValue == null) {
         throw new RuntimeException("Signal "+defaultSignals+" not found as option for pin " + fName);
      }
   }

   /**
    * Sets the reset peripheral signals for the pin
    * 
    * @param resetSignals  Name of signals to look for e.g. <b><i>GPIOE_1/LLWU_P0</i></b>
    * 
    * @throws Exception If pin already has reset or value not available pin mapping
    */
   public void setResetSignals(DeviceInfo factory, final String resetSignals) {
      for (MuxSelection muxValue:fMappedSignals.keySet()) {
         MappingInfo mappingInfo= fMappedSignals.get(muxValue);
         if (mappingInfo.getSignalList().equalsIgnoreCase(resetSignals) && (mappingInfo.getMux() != MuxSelection.reset)) {
            setResetValue(mappingInfo.getMux());
            break;
         }
      }
      // Disabled is not necessarily in list of mappings
      // If necessary create mux0=disabled if free
      if ((fResetMuxValue == MuxSelection.unused) && resetSignals.equalsIgnoreCase(MuxSelection.disabled.name())) {
         if (fMappedSignals.get(MuxSelection.mux0) == null) {
            factory.createMapping(Signal.DISABLED_SIGNAL, this, MuxSelection.mux0);
            setResetValue(MuxSelection.mux0);
         }
      }
      if (fResetMuxValue == MuxSelection.unused) {
         throw new RuntimeException("Reset function "+resetSignals+" not found as option for pin " + fName);
      }
   }

   /**
    * Get PCR initialisation string e.g. for <b><i>PTB4</b></i>
    * <pre>
    * "PORTB_CLOCK_MASK, PORTB_BasePtr,  GPIOB_BasePtr,  4, "
    * OR
    * "0, 0, 0, 0, "
    * </pre>
    * 
    * @param pin The pin being configured
    * 
    * @return
    * @throws Exception 
    */
   String getPCRInitString() throws Exception {
      String portClockMask = getClockMask();
      if (portClockMask == null) {
         // No PCR - probably an analogue pin
         return "0, 0, 0, 0, ";
      }
      String pcrRegister      = getPORTBasePtr();
      String gpioRegister     = getGpioReg();
      String gpioBitNum       = getGpioBitNum();

      return String.format("%-17s %-15s %-15s %-4s", portClockMask+",", pcrRegister+",", gpioRegister+",", gpioBitNum+",");
   }

   @Override
   public int compareTo(Pin o) {
      return comparator.compare(fName, o.fName);
   }

   /**
    * Get map of signals mapped to this pin ordered by mux value
    * 
    * @return
    */
   public Map<MuxSelection, MappingInfo> getMappedSignals() {
      return fMappedSignals;
   }

   /** 
    * Set description of pin use 
    */
   public void setPinUseDescription(String pinUseDescription) {
      fPinUseDescription = pinUseDescription;
      fDeviceInfo.setDirty(true);
      notifyListeners();
   }

   /** 
    * Get description of pin use 
    */
   public String getPinUseDescription() {
      return fPinUseDescription;
   }

   /** 
    * Set current pin multiplexor setting 
    * 
    * @param newMuxValue Multiplexor value to set
    */
   public void setMuxSelection(MuxSelection newMuxValue) {
//      System.err.println("Pin("+fName+")::setMuxSelection("+newMuxValue+")");
      if (this == DISABLED_PIN) {
         return;
      }
      if (newMuxValue == fMuxValue) {
         return;
      }
      fDeviceInfo.setDirty(true);
      MappingInfo mapInfo = fMappedSignals.get(fMuxValue);
      fMuxValue = newMuxValue;
      if (mapInfo != null) {
         mapInfo.select(MappingInfo.Origin.pin, false);
      }
      mapInfo = fMappedSignals.get(fMuxValue);
      if (mapInfo != null) {
         mapInfo.select(MappingInfo.Origin.pin, true);
      }
   }

   /** 
    * Get current pin multiplexor value 
    */
   public MuxSelection getMuxValue() {
      return fMuxValue;
   }

   /**
    * Add a signal mapping to this pin
    * 
    * @param signal      Signal to add
    * @param muxValue    Mux selection to select this signal on pin
    * @return
    */
   public MappingInfo addSignal(Signal signal, MuxSelection muxValue) {
      if (muxValue == MuxSelection.fixed) {
         fResetMuxValue    = MuxSelection.fixed;
         fDefaultMuxValue  = MuxSelection.fixed;
         setMuxSelection(MuxSelection.fixed);
      }
      MappingInfo mapInfo = fMappedSignals.get(muxValue);
      if (mapInfo == null) {
         // Create new mapping
         mapInfo = new MappingInfo(this, muxValue);
         fMappedSignals.put(muxValue, mapInfo);
      }
      mapInfo.addSignal(signal);
      signal.addMapping(mapInfo);
      return mapInfo;
   }

   /** Key for mux selection persistence */
   public static final String MUX_SETTINGS_KEY = "_muxSetting"; 
   
   /** Key for description selection persistence */
   public static final String DESCRIPTION_SETTINGS_KEY = "_descriptionSetting"; 
   
   /**
    * Load pin settings from settings object
    * 
    * @param settings Settings object
    */
   public void loadSettings(DialogSettings settings) {
      String value = settings.get(fName+MUX_SETTINGS_KEY);
      if (value != null) {
         MuxSelection muxValue = MuxSelection.valueOf(value);
         setMuxSelection(muxValue);
      }
      value = settings.get(fName+DESCRIPTION_SETTINGS_KEY);
      if (value != null) {
         setPinUseDescription(value);
      }
   }

   /**
    * Save pin settings to settings object
    * 
    * @param settings Settings object
    */
   public void saveSettings(DialogSettings settings) {
      if ((fMuxValue != fDefaultMuxValue) && (fMuxValue != MuxSelection.fixed)) {
         settings.put(fName+MUX_SETTINGS_KEY, fMuxValue.name());
      }
      String desc = getPinUseDescription();
      if ((desc != null) && !desc.isEmpty()) {
         settings.put(fName+DESCRIPTION_SETTINGS_KEY, getPinUseDescription());
      }
   }

   /**
    * Get the currently mapped signal for this pin
    * 
    * @return Mapped signal or <b>MappingInfo.DISABLED_MAPPING</b> if none
    */
   public MappingInfo getMappedSignal() {
      MappingInfo rv = fMappedSignals.get(fMuxValue);
      if (rv == null) {
         rv = MappingInfo.DISABLED_MAPPING;
      }
      return rv;
   }

   @Override
   public void modelElementChanged(ObservableModel model) {
      if (model instanceof MappingInfo) {
         MappingInfo mappingInfo = (MappingInfo) model;
         if (mappingInfo.isSelected()) {
            // Signal has been mapped to this pin
            if (fMuxValue == mappingInfo.getMux()) {
//               System.err.println("Pin("+fName+").modelElementChanged("+mappingInfo+") - Selection Ignored");
               // Already mapped - ignore
               return;
            }
            // Deselect other signals
            for (MuxSelection muxValue:fMappedSignals.keySet()) {
               if (muxValue != mappingInfo.getMux()) {
                  fMappedSignals.get(muxValue).select(Origin.pin, false);
               }
            }
            // Select this signal
            fMuxValue = mappingInfo.getMux();
            fDeviceInfo.setDirty(true);
         }
         else {
            // Signal may have been unmapped
            if (fMuxValue != mappingInfo.getMux()) {
               // Not currently mapped - ignore
//               System.err.println("Pin("+fName+").modelElementChanged("+mappingInfo+") - Unselection Ignored");
               return;
            }
            fMuxValue = fResetMuxValue;
            fDeviceInfo.setDirty(true);
         }
      }
//      System.err.println("Pin("+fName+").modelElementChanged("+fMuxValue+") - Changed");
      if (fMuxValue == null) {
         throw new RuntimeException("Impossible mapping");
      }
      notifyListeners();
   }

   @Override
   public void modelStructureChanged(ObservableModel model) {
      notifyStructureChangeListeners();
   }
   
//   public String isValid() {
//      int mapCount = 0;
//      for (MuxSelection key:fMappedSignals.keySet()) {
//         MappingInfo mappedSignal = fMappedSignals.get(key);
//         if (mappedSignal.isSelected()) {
//            mapCount++;
//         }
//      }
//      return (mapCount>1)?"Conflict":null;
//   }
}