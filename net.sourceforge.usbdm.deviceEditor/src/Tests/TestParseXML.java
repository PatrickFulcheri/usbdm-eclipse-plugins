package Tests;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sourceforge.usbdm.deviceEditor.information.DeviceInfo;
import net.sourceforge.usbdm.deviceEditor.information.DeviceInformation;
import net.sourceforge.usbdm.deviceEditor.information.DevicePackage;
import net.sourceforge.usbdm.deviceEditor.information.PinInformation;
import net.sourceforge.usbdm.deviceEditor.xmlParser.ParseFamilyXML;

public class TestParseXML {
   
   private static final DirectoryStream.Filter<Path>xmlFilter = new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(Path path) throws IOException {
         return path.getFileName().toString().matches(".*\\.xml$");
      }
   };

   public static void main(String[] args) throws Exception {
      
      Path directory = Paths.get("");
      
      // Locate data output directory  
      Path outputDirectory = directory.resolve("xml2");

      // Create output directories if needed  
      if (!outputDirectory.toFile().exists()) {
         Files.createDirectory(outputDirectory);
      }
      DeviceInfo deviceInfo = null;
      DirectoryStream<Path> folderStream = Files.newDirectoryStream(directory.resolve("xml").toAbsolutePath(), xmlFilter);
      for (Path filePath : folderStream) {
         if (!Files.isRegularFile(filePath)) {
            continue;
         }
         /*
          * Process each input file
          */
         System.err.println("Processing " + filePath.getFileName() + " ======================== ");
         
         ParseFamilyXML parser = new ParseFamilyXML();
         deviceInfo = parser.parseFile(filePath);
         
//         report(deviceInfo);
         
//         WriteFamilyXML writer = new WriteFamilyXML();
//         Path xmlFilePath = outputDirectory.resolve(filePath.getFileName());
//         writer.writeXMLFile(xmlFilePath, deviceInfo);
      }
      report(deviceInfo);
   }

   static void report(DeviceInfo deviceInfo) {
      for (String key:deviceInfo.getDevices().keySet()) {
         DeviceInformation deviceInformation = deviceInfo.findDevice(key);
         System.err.println("deviceInformation = " + deviceInformation);
      }
      for (String packageName:deviceInfo.getDevicePackages().keySet()) {
         DevicePackage devicePackage = deviceInfo.findDevicePackage(packageName);
         System.err.println("Package = " + devicePackage);
         for (String pinName:devicePackage.getPins().keySet()) {
            String location = devicePackage.getLocation(pinName);
            System.err.println(pinName + " => " + location);
         }
      }
      for(String pinName:deviceInfo.getPins().keySet()) {
         PinInformation pin = deviceInfo.findPin(pinName);
         System.err.println("Pin = " + pin.getName());
      };
      for(String peripheralName:deviceInfo.getPeripheralNames()) {
         System.err.println("Peripheral = " + peripheralName);
      };
      for(String peripheralFunction:deviceInfo.getPeripheralFunctions().keySet()) {
         System.err.println("PeripheralFunction = " + peripheralFunction);
      };

   }

}
