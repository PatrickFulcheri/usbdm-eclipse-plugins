package net.sourceforge.usbdm.cdt.tools;

public class UsbdmConstants {
   public final static String PAGE_ID = "net.sourceforge.usbdm.cdt.wizardPage"; //$NON-NLS-1$

   public static final String GCC_COMMAND_LINUX       = "gcc";             //$NON-NLS-1$
   public static final String GCC_COMMAND_WINDOWS     = "gcc.exe";         //$NON-NLS-1$

   public final static String LINKER_MEMORY_MAP_COLDFIRE_V1 = 
         "  /* Default Map - Unknow device */\n" +              //$NON-NLS-1$
         "  rom (rx)  : ORIGIN = 0x00000000, LENGTH = 128K\n"+  //$NON-NLS-1$
         "  ram (rwx) : ORIGIN = 0x00800000, LENGTH = 24K\n"+   //$NON-NLS-1$
         "  gpio (rw) : ORIGIN = 0x00c00000, LENGTH = 16\n"+    //$NON-NLS-1$
         "  io (rw)   : ORIGIN = 0x00ff8000, LENGTH = 32K\n";   //$NON-NLS-1$
   public final static String LINKER_MEMORY_MAP_COLDFIRE_Vx = 
         "  /* Default Map - Unknow device  */\n" +             //$NON-NLS-1$
         "  rom (rx)  : ORIGIN = 0x00000000, LENGTH = 128K\n"+  //$NON-NLS-1$
         "  ram (rwx) : ORIGIN = 0x00800000, LENGTH = 24K\n"+   //$NON-NLS-1$
         "  gpio (rw) : ORIGIN = 0x00c00000, LENGTH = 16\n"+    //$NON-NLS-1$
         "  io (rw)   : ORIGIN = 0x00ff8000, LENGTH = 32K\n";   //$NON-NLS-1$
   public final static String LINKER_MEMORY_MAP_COLDFIRE_KINETIS = 
         "  /* Default Map - Unknow device  */\n" +             //$NON-NLS-1$
         "  rom (rx)  : ORIGIN = 0x00000000, LENGTH = 128K\n"+  //$NON-NLS-1$
         "  ram (rwx) : ORIGIN = 0x00800000, LENGTH = 24K\n"+   //$NON-NLS-1$
         "  gpio (rw) : ORIGIN = 0x00c00000, LENGTH = 16\n"+    //$NON-NLS-1$
         "  io (rw)   : ORIGIN = 0x00ff8000, LENGTH = 32K\n";   //$NON-NLS-1$
   
   // These constants are used for project templates keys
   public final static String TARGET_DEVICE_KEY                = "targetDevice";           //$NON-NLS-1$
   public final static String BUILD_TOOL_ID_KEY                = "buildToolId";            //$NON-NLS-1$
   public final static String BUILD_TOOL_BIN_PATH_KEY          = "buildToolBinPath";       //$NON-NLS-1$
   public final static String GDB_COMMAND_KEY                  = "gdbCommand";             //$NON-NLS-1$
   public final static String USBDM_DEBUG_MODE_KEY             = "usbdmDebugMode";         //$NON-NLS-1$
   public final static String USBDM_GDB_SPRITE_KEY             = "usbdmGdbSprite";         //$NON-NLS-1$
   public final static String TARGET_DEVICE_FAMILY_KEY         = "targetDeviceFamily";     //$NON-NLS-1$
   public final static String TARGET_DEVICE_SUBFAMILY_KEY      = "targetDeviceSubFamily";  //$NON-NLS-1$
   public final static String TARGET_DEVICE_NAME_KEY           = "targetDeviceName";       //$NON-NLS-1$
   public final static String LINKER_MEMORY_MAP_KEY            = "linkerMemoryMap";        //$NON-NLS-1$
   public final static String EXTERNAL_HEADER_FILE_KEY         = "externalHeaderFile";     //$NON-NLS-1$
   public final static String EXTERNAL_VECTOR_TABLE_KEY        = "externalVectorTable";    //$NON-NLS-1$
   public final static String C_DEVICE_PARAMETERS_KEY          = "cDeviceParameters";      //$NON-NLS-1$
   public final static String USBDM_APPLICATION_PATH_KEY       = "usbdmApplicationPath";   //$NON-NLS-1$

   public final static String EXCLUDED_FILES_PATTERN_KEY       = "excludedFilesPattern";    //$NON-NLS-1$
   public final static String LINKER_FILE_KEY                  = "linkerFile";              //$NON-NLS-1$
   
   // These constants are used both for the dialogue persistent storage AND the page data map keys
   final static String SHARED_DEFAULTS_PREFIX_KEY       = "prefix";                 //$NON-NLS-1$
   final static String SHARED_DEFAULTS_PATH_KEY         = "path";                   //$NON-NLS-1$

   // Locations to look for device stationery
   final static String STATIONERY_PATH     = "Stationery/";                         //$NON-NLS-1$
   public final static String PROJECT_HEADER_PATH = STATIONERY_PATH+"Project_Headers/";    //$NON-NLS-1$
   public final static String VECTOR_TABLE_PATH   = STATIONERY_PATH+"Vector_Table/";       //$NON-NLS-1$
   
   public final static String SUB_FAMILY_CORTEX_M4 = "CortexM4";
   public final static String SUB_FAMILY_CFV1      = "CFV1";
   public final static String SUB_FAMILY_CFV1_PLUS = "CFV1Plus";
   public final static String SUB_FAMILY_CFV2      = "CFV2";
   
   // These keys are used in the project options
   public static final String USBDM_GCC_PATH_OPTION_KEY            = "net.sourceforge.usbdm.cdt.toolchain.cross.codesourceryPath";
   public static final String USBDM_GCC_PREFIX_OPTION_KEY          = "net.sourceforge.usbdm.cdt.toolchain.cross.prefix";

   public static final String USBDM_GCC_PROC_ARM_MCPU_OPTION_KEY               = "net.sourceforge.usbdm.cdt.arm.toolchain.mcpu";
   public static final String USBDM_GCC_PROC_ARM_MTHUMB_OPTION_KEY             = "net.sourceforge.usbdm.cdt.arm.toolchain.mthumb";
   public static final String USBDM_GCC_PROC_ARM_MFLOAT_ABI_OPTION_KEY         = "net.sourceforge.usbdm.cdt.arm.toolchain.mfloat-abi";
   public static final String USBDM_GCC_PROC_ARM_MFPU_OPTION_KEY               = "net.sourceforge.usbdm.cdt.arm.toolchain.mfpu";
   public static final String USBDM_GCC_PROC_ARM_FSHORT_DOUBLE_OPTION_KEY      = "net.sourceforge.usbdm.cdt.arm.toolchain.short-double";

   public static final String USBDM_GCC_PROC_COLDFIRE_MCPU_OPTION_KEY          = "net.sourceforge.usbdm.cdt.coldfire.toolchain.mcpu";
   public static final String USBDM_GCC_PROC_COLDFIRE_MFLOAT_ABI_OPTION_KEY    = "net.sourceforge.usbdm.cdt.coldfire.toolchain.mfloat-abi";
   public static final String USBDM_GCC_PROC_COLDFIRE_MFPU_OPTION_KEY          = "net.sourceforge.usbdm.cdt.coldfire.toolchain.mfpu";
   public static final String USBDM_GCC_PROC_COLDFIRE_FSHORT_DOUBLE_OPTION_KEY = "net.sourceforge.usbdm.cdt.coldfire.toolchain.short-double";

   public static final String USBDM_GCC_DEBUG_LEVEL_OPTION_KEY     = "net.sourceforge.usbdm.cdt.toolchain.debug.debugLevel";
   public static final String USBDM_GCC_DEBUG_FORMAT_OPTION_KEY    = "net.sourceforge.usbdm.cdt.toolchain.debug.mdebugformat";
   public static final String USBDM_GCC_DEBUG_OTHER_OPTION_KEY     = "net.sourceforge.usbdm.cdt.toolchain.debug.other";
   public static final String USBDM_GCC_DEBUG_PROF_OPTION_KEY      = "net.sourceforge.usbdm.cdt.toolchain.debug.prof";
   public static final String USBDM_GCC_DEBUG_GPROF_OPTION_KEY     = "net.sourceforge.usbdm.cdt.toolchain.debug.gprof";
                                                                   
   // These keys are used as keys to CDT Variables                 
   public static final String ARM_BUILDTOOLS_OPTIONS               = "net.sourceforge.usbdm.cdt.arm.toolchain.buildtools";
   public static final String COLDFIRE_BUILDTOOLS_OPTIONS          = "net.sourceforge.usbdm.cdt.coldfire.toolchain.buildtools";
   
   public static final String PATH_NOT_SET      = "PATH NOT SET";
   public static final String COMMAND_NOT_SET   = "COMMAND NOT SET";
   public static final String PREFIX_NOT_SET    = "PREFIX NOT SET";
   
}
