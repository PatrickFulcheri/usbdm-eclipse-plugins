/*
 Change History
+===================================================================================
| Revision History
+===================================================================================
| 19 Jan 15 | Added some ignored tags                                     4.10.6.250
| 16 Nov 13 | Added subfamily field                                       4.10.6.100
+===================================================================================
 */
package net.sourceforge.usbdm.peripheralDatabase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.usbdm.peripheralDatabase.Field.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * @author podonoghue
 *
 */
public class SVD_XML_Parser extends SVD_XML_BaseParser {

   static final String DERIVEDFROM_ATTRIB               = "derivedFrom";
   static final String REFRESH_WHOLE_PERIPHERAL_ATTRIB  = "refreshWholePeripheral";
   static final String PREFERREDACCESSWIDTH_ATTRIB      = "preferredAccessWidth";
   static final String FORCED_ACCESS_WIDTH              = "forcedAccessWidth";
   static final String FORCED_BLOCK_WIDTH               = "forcedBlockWidth";
   static final String SOURCEFILE_ATTRIB                = "sourceFile";
   static final String IGNOREOVERLAP_ATTRIB             = "ignoreOverlap";
   static final String HIDE_ATTRIB                      = "hide";

   static final String ALTERNATEREGISTER_TAG    = "alternateRegister";
   static final String ACCESS_TAG               = "access";
   static final String ADDRESSBLOCK_TAG         = "addressBlock";
   static final String ADDRESSOFFSET_TAG        = "addressOffset";
   static final String ADDRESSUNITSBITS_TAG     = "addressUnitBits";
   static final String ALTERNATEGROUP_TAG       = "alternateGroup";
   static final String APPENDTONAME_TAG         = "appendToName";
   static final String BASEADDRESS_TAG          = "baseAddress";
   static final String BITOFFSET_TAG            = "bitOffset";
   static final String BITWIDTH_TAG             = "bitWidth";
   static final String BITRANGE_TAG             = "bitRange";
   static final String CLUSTER_TAG              = "cluster";
   static final String CPU_TAG                  = "cpu";
   static final String DESCRIPTION_TAG          = "description";
   static final String DIM_TAG                  = "dim";
   static final String DIMINCREMENT_TAG         = "dimIncrement";
   static final String DIMINDEX_TAG             = "dimIndex";
   static final String DISABLECONDITION_TAG     = "disableCondition";
   static final String DISPLAYNAME_TAG          = "displayName";
   static final String ENDIAN_TAG               = "endian";
   static final String ENUMERATEDVALUES_TAG     = "enumeratedValues";
   static final String ENUMERATEDVALUE_TAG      = "enumeratedValue";
   static final String FALSE_TAG                = "false";
   static final String FIELDS_TAG               = "fields";
   static final String FIELD_TAG                = "field";
   static final String FPUPRESENT_TAG           = "fpuPresent";
   static final String VTORPRESENT_TAG          = "vtorPresent";
   static final String GROUPNAME_TAG            = "groupName";
   static final String HEADERSTRUCTNAME_TAG     = "headerStructName";
   static final String ISDEFAULT_TAG            = "isDefault";
   static final String INTERRUPT_TAG            = "interrupt";
   static final String INTERRUPTS_TAG           = "interrupts";
   static final String LICENSE_TAG              = "licenseText";
   static final String LSB_TAG                  = "lsb";
   static final String MODIFIEDWRITEVALUES_TAG  = "modifiedWriteValues";
   static final String MPUPRESENT_TAG           = "mpuPresent";
   static final String MSB_TAG                  = "msb";
   static final String NAME_TAG                 = "name";
   static final String NVICPRIOBITS_TAG         = "nvicPrioBits";
   static final String OFFSET_TAG               = "offset";
   static final String PERIPHERAL_TAG           = "peripheral";
   static final String PERIPHERALS_TAG          = "peripherals";
   static final String PREPENDTONAME_TAG        = "prependToName";
   static final String READACTION_TAG           = "readAction";
   static final String REGISTERS_TAG            = "registers";
   static final String REGISTER_TAG             = "register";
   static final String RESERVED_TAG             = "RESERVED";
   static final String RESETVALUE_TAG           = "resetValue";
   static final String RESETMASK_TAG            = "resetMask";
   static final String REVISION_TAG             = "revision";
   static final String SERIES_TAG               = "series";
   static final String SIZE_TAG                 = "size";
   static final String TRUE_TAG                 = "true";
   static final String USAGE_TAG                = "usage";
   static final String VALUE_TAG                = "value";
   static final String VENDORSYSTICKCONFIG_TAG  = "vendorSystickConfig";
   static final String VENDOREXTENSIONS_TAG     = "vendorExtensions";
   static final String VENDORID_TAG             = "vendorID";
   static final String VENDOR_TAG               = "vendor";
   static final String VERSION_TAG              = "version";
   static final String WRITECONSTRAINTS_TAG     = "writeConstraint";
   static final String WIDTH_TAG                = "width";
   static final String WRITECONSTRAINT_TAG      = "writeConstraint";
   

   DevicePeripherals devicePeripherals = null;          
   
   /**
    * Parse a <enumeratedValue> element
    * @param field 
    * 
    * @param  enumeratedValue <enumeratedValue> element
    * 
    * @return Enumeration described
    *  
    * @throws Exception 
    */
   private static Enumeration parseEnumeratedValue(Field field, Element enumeratedValue) throws Exception {

      Enumeration enumeration = new Enumeration();
      
         for (Node node = enumeratedValue.getFirstChild();
               node != null;
               node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
               continue;
            }
            Element element = (Element) node;
            try {
               if (element.getTagName() == NAME_TAG) {
                  enumeration.setName(element.getTextContent());
               }
               else if (element.getTagName() == DESCRIPTION_TAG) {
                  enumeration.setDescription(element.getTextContent().trim());
               }
               else if (element.getTagName() == VALUE_TAG) {
                  enumeration.setValue(getMappedEnumeratedValue(element.getTextContent()));
               }
               else if (element.getTagName() == ISDEFAULT_TAG) {
                  enumeration.setAsDefault();
               }
               else {
                  throw new Exception("Unexpected field in ENUMERATEDVALUE', value = \'"+element.getTagName()+"\'");
               }
            } catch (Exception e) {
               System.err.println("parseEnumeratedValue() - element =" + element.getTagName() + ", field =" + field.getName());
               e.printStackTrace();
               throw e;
            }
         }
      return enumeration;
   }
   
   /**
    * Convert enumerated values to standard form
    * e.g. #BBBB => 0bBBBB
    * 
    * @param value
    * 
    * @return
    */
   private static String getMappedEnumeratedValue(String value) {
      final ArrayList<Pair> mappedMacros = new ArrayList<Pair>();

      if (mappedMacros.size() == 0) {
         // Manually eliminate redundant definitions for peripherals which are a subset of earlier peripherals but not derived
         mappedMacros.add(new Pair(Pattern.compile("^\\#(.*)$"),                "0b$1"));
      }
      for (Pair p : mappedMacros) {
         Matcher matcher = p.regex.matcher(value);
         if (matcher.matches()) {
            if (p.replacement == null) {
               return null;
            }
            return matcher.replaceAll(p.replacement);
         }
      }
      return value;
}
   
   /**
    * Parse a <enumeratedValues> element
    * 
    * @param  enumeratedValuesElement <enumeratedValues> element
    * 
    * @return Device described 
    * 
    * @throws Exception 
    */
   private static void parseEnumeratedValues(Field field, Element enumeratedValuesElement) throws Exception {

      for (Node node = enumeratedValuesElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == ENUMERATEDVALUE_TAG) {
            field.addEnumeration(parseEnumeratedValue(field, element));
         }
         else {
            throw new Exception("Unexpected field in ENUMERATEDVALUES', value = \'"+element.getTagName()+"\'");
         }
      }
   }
   
   /**
    * Parse a <field> element
    * @param register 
    * 
    * @param  fieldElement <field> element
    * 
    * @return Device described
    *  
    * @throws Exception 
    */
   private static Field parseField(Register register, Element fieldElement) throws Exception {
      Field field = null;
      if (fieldElement.hasAttribute(DERIVEDFROM_ATTRIB)) {
         Field referencedField = null;
         if (register != null) {
            referencedField = register.findField(Peripheral.getMappedPeripheralName(fieldElement.getAttribute(DERIVEDFROM_ATTRIB)));
            if (referencedField == null) {
               // Try unmapped name
               referencedField = register.findField(fieldElement.getAttribute(DERIVEDFROM_ATTRIB));
            }
         }
         if (referencedField == null) {
            throw new Exception("Referenced field cannot be found: \"" + fieldElement.getAttribute(DERIVEDFROM_ATTRIB) + "\"");
         }
         field = new Field(referencedField);
      }
      else {
         field = new Field(register);
      }
      for (Node node = fieldElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction element = (ProcessingInstruction) node;
            if (element.getNodeName() == IGNOREOVERLAP_ATTRIB) {
               field.setIgnoreOverlap(true);
            }            
            else if (element.getNodeName() == HIDE_ATTRIB) {
               field.setHidden(true);
            }            
            continue;
         }
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == NAME_TAG) {
            field.setName(element.getTextContent().trim());
         }
         else if (element.getTagName() == DESCRIPTION_TAG) {
            field.setDescription(element.getTextContent().trim());
         }
         else if (element.getTagName() == LSB_TAG) {
            field.setBitOffset(getIntElement(element));
         }
         else if (element.getTagName() == MSB_TAG) {
            field.setBitwidth(getIntElement(element)-field.getBitOffset()+1);
         }
         else if (element.getTagName() == BITOFFSET_TAG) {
            field.setBitOffset(getIntElement(element));
         }
         else if (element.getTagName() == BITWIDTH_TAG) {
            field.setBitwidth(getIntElement(element));
         }
         else if (element.getTagName() == BITRANGE_TAG) {
            String bitRange = element.getTextContent();
            // TODO change to regex
            if ((bitRange.charAt(0) != '[')||(bitRange.charAt(bitRange.length()-1) != ']')) {
               throw new Exception("Illegal BITRANGE in FIELD', value = \'"+bitRange+"\'");
            }
            bitRange = bitRange.substring(1, bitRange.length()-1);
            int commaIndex = bitRange.indexOf(':');
            if (commaIndex < 0) {
               throw new Exception("Illegal BITRANGE in FIELD', value = \'"+bitRange+"\'");
            }
            String end   = bitRange.substring(0, commaIndex);
            String start = bitRange.substring(commaIndex+1);
            int startBit = Integer.parseInt(start);
            int endBit   = Integer.parseInt(end);
            field.setBitOffset(startBit);
            field.setBitwidth(endBit-startBit+1);
         }
         else if (element.getTagName() == ACCESS_TAG) {
            field.setAccessType(getAccessElement(element));
         }
         else if (element.getTagName() == MODIFIEDWRITEVALUES_TAG) {
            //TODO: Implement modifiedWriteValues
         }
         else if (element.getTagName() == WRITECONSTRAINTS_TAG) {
            //TODO: Implement writeConstraint
         }
         else if (element.getTagName() == READACTION_TAG) {
            //TODO: Implement readAction
         }
         else if (element.getTagName() == ENUMERATEDVALUES_TAG) {
            parseEnumeratedValues(field, element);
         }
         else {
            throw new Exception("Unexpected field in FIELD', value = \'"+element.getTagName()+"\'");
         }
      }
      return field;
   }

   /**
    * Parse a <fields> element
    * 
    * @param  register Register to add fields to
    * @param  fieldsElement <fields> element
    * 
    * @throws Exception 
    */
   private static void parseFields(Register register, Element fieldsElement) throws Exception {
      for (Node node = fieldsElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == FIELD_TAG) {
            Field field = parseField(register, element);
            if (!field.getName().equals(RESERVED_TAG)) {
               register.addField(field);
            }
         }
         else {
            throw new Exception(String.format("Unexpected field in <fields> reg = \'%s\', field = \'%s\'", 
                  register.getName(), element.getTagName()));
         }
      }
   }

   /**
    * Parse a <register> element
    * @param peripheral 
    * 
    * @param  registerElement <register> element
    * 
    * @return Register described
    *  
    * @throws Exception 
    */
   private static Register parseRegister(Peripheral peripheral, Cluster cluster, Element registerElement) throws Exception {

      Register register = new Register(peripheral, cluster);
      boolean derived = false;

      if (registerElement.hasAttribute(DERIVEDFROM_ATTRIB)) {
         Cluster referencedRegister = null;
         if (cluster != null) {
            referencedRegister = cluster.findRegister(Peripheral.getMappedPeripheralName(registerElement.getAttribute(DERIVEDFROM_ATTRIB)));
            if (referencedRegister == null) {
               // Try unmapped name
               referencedRegister = cluster.findRegister(registerElement.getAttribute(DERIVEDFROM_ATTRIB));
            }
         }
         if (referencedRegister == null) {
            referencedRegister = peripheral.findRegister(Peripheral.getMappedPeripheralName(registerElement.getAttribute(DERIVEDFROM_ATTRIB)));
            if (referencedRegister == null) {
               // Try unmapped name
               referencedRegister = peripheral.findRegister(registerElement.getAttribute(DERIVEDFROM_ATTRIB));
               if (referencedRegister == null) {
                  throw new Exception("Referenced register cannot be found: \""+registerElement.getAttribute(DERIVEDFROM_ATTRIB)+"\"");
               }
            }
         }
         register = (Register) ((Register) referencedRegister).clone();
         derived  = true;
      }
      else {
         register = new Register(peripheral, cluster);

         // Inherit default from device
         register.setWidth(peripheral.getWidth());
         register.setAccessType(peripheral.getAccessType());
         register.setResetValue(peripheral.getResetValue());
         register.setResetMask(peripheral.getResetMask());
      }
      for (Node node = registerElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction element = (ProcessingInstruction) node;
            if (element.getNodeName() == HIDE_ATTRIB) {
               register.setHidden(true);
            }            
            continue;
         }
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == NAME_TAG) {
          register.setName(element.getTextContent());
         }
         
         else if (element.getTagName() == ADDRESSOFFSET_TAG) {
            register.setAddressOffset(getIntElement(element));
         }
         else if (element.getTagName() == DISPLAYNAME_TAG) {
            if ((register.getName() == null) || (register.getName().length() == 0))
            register.setName(element.getTextContent());
         }
         else if (element.getTagName() == DESCRIPTION_TAG) {
            register.setDescription(element.getTextContent().trim());
         }
         else if (element.getTagName() == ALTERNATEGROUP_TAG) {
            register.setAlternateGroup(element.getTextContent());
         }
         else if (element.getTagName() == RESETVALUE_TAG) {
            register.setResetValue(getIntElement(element));
         }
         else if (element.getTagName() == RESETMASK_TAG) {
            register.setResetMask(getIntElement(element));
         }
         else if (element.getTagName() == DIM_TAG) {
         }
         else if (element.getTagName() == DIMINCREMENT_TAG) {
            register.setDimensionIncrement((int) getIntElement(element));
         }
         else if (element.getTagName() == DIMINDEX_TAG) {
            register.setDimensionIndexes(element.getTextContent());
         }
         else if (derived)  {
            throw new Exception(String.format("Unexpected field in derived <register>, p=%s, r=%s, v=%s", 
                  peripheral.getName(), register.getName(), element.getTagName()));
         }
         else if (element.getTagName() == SIZE_TAG) {
            register.setWidth(getIntElement(element));
         }
         else if (element.getTagName() == ACCESS_TAG) {
            register.setAccessType(getAccessElement(element));
         }
         else if (element.getTagName() == MODIFIEDWRITEVALUES_TAG) {
            //TODO: Implement modifiedWriteValues
         }
         else if (element.getTagName() == WRITECONSTRAINT_TAG) {
            //TODO: Implement modifiedWriteValues
         }
         else if (element.getTagName() == READACTION_TAG) {
            //TODO: Implement readAction
         }
         else if (element.getTagName() == FIELDS_TAG) {
//            System.err.println("parseRegister() Peripheral = " + peripheral.getName() + "register = " + register.getName());
            parseFields(register, element);
         }
         else if (element.getTagName() == ALTERNATEREGISTER_TAG) {
            //TODO: Implement ALTERNATEREGISTER_TAG
         }
         else {
            throw new Exception(String.format("Unexpected field in <register>, p=%s, r=%s, v=%s", 
                  peripheral.getName(), register.getName(), element.getTagName()));
         }
      }
      if (register.getWidth() == 0) {
         register.setWidth(peripheral.getWidth());
      }
      if (register.getAccessType() == null) {
         register.setAccessType(peripheral.getAccessType());
      }
      register.checkFieldAccess();
      register.checkFieldDimensions();
      return register;
   }

   /**
    * Parse a <cluster> element
    * @param peripheral 
    * 
    * @param  peripheralElement <register> element
    * 
    * @return Register described
    *  
    * @throws Exception 
    */
   private static Cluster parseCluster(Peripheral peripheral, Element clusterElement) throws Exception {

      Cluster cluster = new Cluster(peripheral);
      
      // Inherit default from device
      cluster.setAccessType(peripheral.getAccessType());
      cluster.setResetValue(peripheral.getResetValue());
      cluster.setResetMask(peripheral.getResetMask());

      for (Node node = clusterElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction element = (ProcessingInstruction) node;
            if (element.getNodeName() == HIDE_ATTRIB) {
               cluster.setHidden(true);
            }            
            else {
               System.err.println("parseRegister() - unknown attribute " + element.getNodeName());
            }
            continue;
         }
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == DIM_TAG) {
         }
         else if (element.getTagName() == DIMINCREMENT_TAG) {
            cluster.setDimensionIncrement((int) getIntElement(element));
         }
         else if (element.getTagName() == DIMINDEX_TAG) {
            cluster.setDimensionIndexes(element.getTextContent());
         }
         else if (element.getTagName() == NAME_TAG) {
            cluster.setName(element.getTextContent());
         }
         else if (element.getTagName() == ADDRESSOFFSET_TAG) {
            cluster.setAddressOffset(getIntElement(element));
         }
         else if (element.getTagName() == REGISTER_TAG) {
            Register register = parseRegister(peripheral, cluster, element);
            cluster.addRegister(register);
            if (register.getWidth() == 0) {
               register.setWidth(peripheral.getWidth());
            }
            if (register.getAccessType() == null) {
               register.setAccessType(peripheral.getAccessType());
            }
         }
         else {
            throw new Exception("Unexpected field in CLUSTER', value = \'"+element.getTagName()+"\'");
         }
      }
      return cluster;
   }

   /**
    * Parse a <registers> element
    * 
    * @param  peripheral Peripheral to add registers to
    * @param  peripheralElement <registers> element
    * 
    * @throws Exception 
    */
   private static void parseRegisters(Peripheral peripheral, Element peripheralElement) throws Exception {

      for (Node node = peripheralElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         try {
            if (element.getTagName() == REGISTER_TAG) {
               Register register = parseRegister(peripheral, null, element);
               peripheral.addRegister(register);
               if (register.getWidth() == 0) {
                  register.setWidth(peripheral.getWidth());
               }
               if (register.getAccessType() == null) {
                  register.setAccessType(peripheral.getAccessType());
               }
            }
            else if (element.getTagName() == CLUSTER_TAG) {
               Cluster cluster = parseCluster(peripheral, element);
               peripheral.addRegister(cluster);
//            if (register.getSize() == 0) {
//               register.setSize(peripheral.getSize());
//            }
//            if (register.getAccessType() == null) {
//               register.setAccessType(peripheral.getAccessType());
//            }
            }
            else {
               throw new Exception("Unexpected field in REGISTERS', value = \'"+element.getTagName()+"\'");
            }
         } catch (Exception e) {
            System.err.println("parseRegisters() - peripheral = " + peripheral.getName() + ", element tag =" + element.getTagName());
            throw e;
         }
      }
   }
   
   /**
    * Parse a <addressBlock> element
    * 
    * @param  addressBlockElement <addressBlock> element
    * 
    * @return AddressBlock described
    *  
    * @throws Exception 
    */
   private static AddressBlock parseAddressBlock(Element addressBlockElement) throws Exception {

//      System.out.println("parsePeripheral");
      AddressBlock addressBlock = new AddressBlock();

      for (Node node = addressBlockElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
//            System.out.println("parseAddressBlock - " + node.getTextContent());

         if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction element = (ProcessingInstruction) node;
            if (element.getNodeName() == WIDTH_TAG) {
               addressBlock.setWidthInBits((int)getIntElement(element));
            }            
            continue;
         }
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == OFFSET_TAG) {
            addressBlock.setOffset(getIntElement(element));
         }
         else if (element.getTagName() == SIZE_TAG) {
            addressBlock.setSize(getIntElement(element));
         }
         else if (element.getTagName() == WIDTH_TAG) {
            addressBlock.setWidthInBits(getIntElement(element));
         }
         else if (element.getTagName() == USAGE_TAG) {
            addressBlock.setUsage(element.getTextContent());
         }
         else {
            throw new Exception("Unexpected field in ADDRESSBLOCK', value = \'"+element.getTagName()+"\'");
         }
      }
      return addressBlock;
   }

   /**
    * Parse a <interrupt> element
    * 
    * @param  interruptElement <interrupt> element
    * 
    * @return InterruptEntry described
    *  
    * @throws Exception 
    */
   private static InterruptEntry parseInterrupt(Element interruptElement) throws Exception {

      InterruptEntry interruptEntry = new InterruptEntry();

      for (Node node = interruptElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == NAME_TAG) {
            String name = element.getTextContent().trim();//.toUpperCase();
            interruptEntry.setName(name.replace("INT_", ""));
         }
         else if (element.getTagName() == DESCRIPTION_TAG) {
            interruptEntry.setDescription(element.getTextContent().trim());
         }
         else if (element.getTagName() == VALUE_TAG) {
            interruptEntry.setIndexNumber((int)getIntElement(element));
         }
         else {
            throw new Exception("Unexpected field in INTERRUPT', value = \'"+element.getTagName()+"\'");
         }
      }
      return interruptEntry;
   }

   /**
    * Parse a <peripheral> element
    * 
    * @param  peripheralElement <peripheral> element
    * 
    * @return Peripheral described 
    * 
    * @throws Exception 
    */
   private static Peripheral parsePeripheral(DevicePeripherals device, Element peripheralElement) throws Exception {

      boolean derived       = false;
      boolean interruptsSet = false;
      Peripheral peripheral = null;
      
      if (peripheralElement.hasAttribute(DERIVEDFROM_ATTRIB)) {
         Peripheral referencedPeripheral = null;
         referencedPeripheral = device.findPeripheral(Peripheral.getMappedPeripheralName(peripheralElement.getAttribute(DERIVEDFROM_ATTRIB)));
         if (referencedPeripheral == null) {
            // Try unmapped name
            referencedPeripheral = device.findPeripheral(peripheralElement.getAttribute(DERIVEDFROM_ATTRIB));
            if (referencedPeripheral == null) {
               for (Peripheral x : device.getPeripherals()) {
                  System.err.println("Peripherals :" + x.getName());
               }
               throw new Exception(
                     "Referenced peripheral cannot be found: \""+peripheralElement.getAttribute(DERIVEDFROM_ATTRIB)+"\"\n");
            }
         }
         peripheral = (Peripheral) referencedPeripheral.clone();
         derived    = true;
      }
      else {
         peripheral = new Peripheral(device);
         // Inherit default from device
         peripheral.setWidth(device.getWidth());
         peripheral.setAccessType(device.getAccessType());
         peripheral.setResetValue(device.getResetValue());
         peripheral.setResetMask(device.getResetMask());
//         peripheral.setForcedBlockMultiple(32);
//         peripheral.setBlockAccessWidth(32);
      }
      for (Node node = peripheralElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction element = (ProcessingInstruction) node;
            if (element.getNodeName() == SOURCEFILE_ATTRIB) {
               peripheral.setSourceFilename(stripQuotes(element.getTextContent()));
            }            
            if (element.getNodeName() == PREFERREDACCESSWIDTH_ATTRIB) {
               peripheral.setBlockAccessWidth((int)getIntElement(element));
            }            
            if (element.getNodeName() == FORCED_ACCESS_WIDTH) {
               System.err.println("OPPPS");
            }            
            if (element.getNodeName() == FORCED_BLOCK_WIDTH) {
               peripheral.setForcedBlockMultiple((int)getIntElement(element));
            }            
            if (element.getNodeName() == REFRESH_WHOLE_PERIPHERAL_ATTRIB) {
               peripheral.setRefreshAll(true);
            }            
            continue;
         }
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == NAME_TAG) {
            peripheral.setName(element.getTextContent());
         }
         else if (element.getTagName() == VERSION_TAG) {
            //TODO: Implement version
         }
         else if (element.getTagName() == DESCRIPTION_TAG) {
            peripheral.setDescription(element.getTextContent().trim());
         }
         else if (element.getTagName() == GROUPNAME_TAG) {
            peripheral.setGroupName(element.getTextContent());
         }
         else if (element.getTagName() == PREPENDTONAME_TAG) {
            peripheral.setPrependToName(element.getTextContent());
         }
         else if (element.getTagName() == APPENDTONAME_TAG) {
            peripheral.setAppendToName(element.getTextContent());
         }
         else if (element.getTagName() == HEADERSTRUCTNAME_TAG) {
            peripheral.setHeaderStructName(element.getTextContent());
         }
         else if (element.getTagName() == DISABLECONDITION_TAG) {
            //TODO: Implement disableCondition
         }
         else if (element.getTagName() == BASEADDRESS_TAG) {
            peripheral.setBaseAddress(getIntElement(element));
         }
         else if (element.getTagName() == INTERRUPT_TAG) {
            if (derived && !interruptsSet) {
               // First interrupt - remove existing derived ones
               peripheral.clearInterruptEntries();
               interruptsSet = true;
            }
            InterruptEntry interruptEntry = parseInterrupt(element);
            if ((interruptEntry.getDescription() == null) || (interruptEntry.getDescription().length() == 0)) {
               interruptEntry.setDescription(peripheral.getDescription());
            }
            peripheral.addInterruptEntry(interruptEntry);
         }
         else if (derived) {
            throw new Exception("Unexpected field in derived PERIPHERAL', value = \'"+element.getTagName()+"\'");
         }
         else if (element.getTagName() == ACCESS_TAG) {
            peripheral.setAccessType(getAccessElement(element));
         }
         else if (element.getTagName() == RESETVALUE_TAG) {
            peripheral.setResetValue(getIntElement(element));
         }
         else if (element.getTagName() == RESETMASK_TAG) {
            peripheral.setResetMask(getIntElement(element));
         }
         else if (element.getTagName() == SIZE_TAG) {
            peripheral.setWidth(getIntElement(element));
         }
         else if (element.getTagName() == ADDRESSBLOCK_TAG ){
            peripheral.addAddressBlock(parseAddressBlock(element));
         }
         else if (element.getTagName() == REGISTERS_TAG) {
            parseRegisters(peripheral, element);
         }
         else {
            throw new Exception("parsePeripheral() - Unexpected field in PERIPHERAL', value = \'"+element.getTagName()+"\'");
         }
      }
      return peripheral;
   }

   /**
    * Parse a <peripherals> element
    * 
    * @param  device Device to add peripherals to
    * 
    * @param  peripheralsElement <peripherals> element
    * 
    * @throws Exception
    */
   private static void parsePeripherals(DevicePeripherals device, Element peripheralsElement) throws Exception {

//      System.out.println("parsePeripherals");

      for (Node node = peripheralsElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == PERIPHERAL_TAG) {
            Peripheral peripheral;
            try {
               peripheral = parsePeripheral(device, element);
            } catch (Exception e) {
               System.err.println("parsePeripherals() device = " + device.getName() + ", Tag = " + peripheralsElement.getTagName());
               throw e;
            }
            device.addPeripheral(peripheral);
            if (peripheral.getWidth() == 0) {
               peripheral.setWidth(device.getWidth());
            }
            if (peripheral.getAccessType() == null) {
               peripheral.setAccessType(device.getAccessType());
            }
         }
         else {
            throw new Exception("Unexpected field in PERIPHERALS', value = \'"+element.getTagName()+"\'");
         }
      }
   }

   /**
    * Parse a <cpu> element
    * 
    * @param  devicePeripherals Device to add peripherals to
    * 
    * @param  cpuElement <cpu> element
    * 
    * @throws Exception
    */
   private static void parseCpu(DevicePeripherals devicePeripherals, Element cpuElement) throws Exception {

      Cpu cpu = new Cpu();
      
      for (Node node = cpuElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == NAME_TAG) {
            cpu.setName(element.getTextContent());
         }
         else if (element.getTagName() == REVISION_TAG) {
            cpu.setRevision(element.getTextContent());
         }
         else if (element.getTagName() == ENDIAN_TAG) {
            cpu.setEndian(element.getTextContent());
         }
         else if (element.getTagName() == MPUPRESENT_TAG) {
            cpu.setMpuPresent(element.getTextContent().equalsIgnoreCase(TRUE_TAG));
         }
         else if (element.getTagName() == FPUPRESENT_TAG) {
            cpu.setFpuPresent(element.getTextContent().equalsIgnoreCase(TRUE_TAG));
         }
         else if (element.getTagName() == VTORPRESENT_TAG) {
            cpu.setVtorPresent(element.getTextContent().equalsIgnoreCase(TRUE_TAG));
         }
         else if (element.getTagName() == NVICPRIOBITS_TAG) {
            cpu.setNvicPrioBits((int)getIntElement(element));
         }
         else if (element.getTagName() == VENDORSYSTICKCONFIG_TAG) {
            // Ignored
         }
         else {
            throw new Exception("Unexpected field in CPU', value = \'"+element.getTagName()+"\'");
         }
      }
      devicePeripherals.setCpu(cpu);
   }

   /**
    * Parse a <interrupts> element
    * 
    * @param  devicePeripherals Device to add peripherals to
    * 
    * @param  interruptElement <interrupts> element
    * 
    * @throws Exception
    */
   private static void parseInterrupts(DevicePeripherals devicePeripherals, Element interruptElement) throws Exception {
      int lastEntryNumber = -1000;
      VectorTable vectorTable = VectorTable.factory(devicePeripherals.getCpu().getName());

      for (Node node = interruptElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == NAME_TAG) {
            vectorTable.setName(element.getTextContent());
         }
         else if (element.getTagName() == DESCRIPTION_TAG) {
            vectorTable.setDescription(element.getTextContent().trim());
         }
         else if (element.getTagName() == INTERRUPT_TAG) {
            InterruptEntry entry = parseInterrupt(element);
            if (entry.getIndexNumber() <= -100) {
               entry.setIndexNumber(++lastEntryNumber);
            }
            else if (entry.getIndexNumber() <= lastEntryNumber) {
               throw new Exception("Interrupt vectors must be monotonic, # + " + entry.getIndexNumber());
            }
            lastEntryNumber = entry.getIndexNumber();
            if (vectorTable.getEntry(lastEntryNumber) != null) {
               throw new Exception("Repeated Interrupt number");
            }
            if (entry.getIndexNumber()<0) {
               System.err.println("Warning: Discarding predefined vector \""+entry.getName()+"\"");
               continue;
            }
            vectorTable.addEntry(entry);
         }
         else {
            System.err.println("Unexpected field in <interrupts>', value = \'"+element.getTagName()+"\'");
         }
      }
      devicePeripherals.setVectorTable(vectorTable);
   }

   /**
    * Parse a <vendorExtensions> element
    * 
    * @param  devicePeripherals Device to add peripherals to
    * 
    * @param  vendorExtensionsElement <vendorExtensions> element
    * 
    * @throws Exception
    */
   private static void parseVendorExtensions(DevicePeripherals devicePeripherals, Element vendorExtensionsElement) throws Exception {
      for (Node node = vendorExtensionsElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == INTERRUPTS_TAG) {
            parseInterrupts(devicePeripherals, element);
         }
         else {
            System.err.println("Unexpected field in <vendorExtensions>', value = \'"+element.getTagName()+"\'");
         }
      }
   }

   /**
    * Parses document from top element
    * 
    * @throws Exception
    */
   public static void parseDocument(Path path, DevicePeripherals devicePeripherals) throws Exception {
      
      Document document = parseXmlFile(path);
      
      Element documentElement = document.getDocumentElement();

      if (documentElement == null) {
         System.out.println("DeviceDatabase.parseDocument() - failed to get documentElement");
      }
      for (Node node = documentElement.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
         if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
         }
         Element element = (Element) node;
         if (element.getTagName() == NAME_TAG) {
            devicePeripherals.setName(element.getTextContent());
         }
         else if (element.getTagName() == VERSION_TAG) {
            devicePeripherals.setVersion(element.getTextContent());
         }
         else if (element.getTagName() == DESCRIPTION_TAG) {
            devicePeripherals.setDescription(element.getTextContent().trim());
         }
         else if (element.getTagName() == ADDRESSUNITSBITS_TAG) {
            devicePeripherals.setAddressUnitBits(getIntElement(element));
         }
         else if (element.getTagName() == WIDTH_TAG) {
            devicePeripherals.setWidth(getIntElement(element));
         }
         else if (element.getTagName() == SIZE_TAG) {
            devicePeripherals.setWidth(getIntElement(element));
         }
         else if (element.getTagName() == ACCESS_TAG) {
            devicePeripherals.setAccessType(getAccessElement(element));
         }
         else if (element.getTagName() == RESETVALUE_TAG) {
            devicePeripherals.setResetValue(getIntElement(element));
         }
         else if (element.getTagName() == RESETMASK_TAG) {
            devicePeripherals.setResetMask(getIntElement(element));
         }
         else if (element.getTagName() == PERIPHERALS_TAG) {
            parsePeripherals(devicePeripherals, element);
         }
         else if (element.getTagName() == CPU_TAG) {
            parseCpu(devicePeripherals, element);
         }
         else if (element.getTagName() == VENDOR_TAG) {
            devicePeripherals.setVendor(element.getTextContent());
         }
         else if (element.getTagName() == VENDORID_TAG) {
            devicePeripherals.setVendor(element.getTextContent());
         }
         else if (element.getTagName() == LICENSE_TAG) {
            devicePeripherals.setLicense(element.getTextContent());
         }
         else if (element.getTagName() == SERIES_TAG) {
            // Ignore
         }
         else if (element.getTagName() == VENDOREXTENSIONS_TAG) {
            parseVendorExtensions(devicePeripherals, element);
         }
         else {
            throw new Exception("Unexpected field in DEVICE, value = \'"+element.getTagName()+"\'");
         }
      }
   }
   
}
