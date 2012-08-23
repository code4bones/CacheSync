SET ROOT=D:\devel\prj\src\Android\CustomCommand\bin

call D:\devel\prj\lib\Android\android-sdk\platform-tools\dx.bat --dex --output=%ROOT%/classes.dex %ROOT%/customcommand.jar 
echo "1"
D:\devel\prj\lib\Android\android-sdk\platform-tools\aapt.exe add %ROOT%/customcommand.jar classes.dex
echo "2"
D:\devel\prj\lib\Android\android-sdk\platform-tools\adb.exe push %ROOT%/customcommand.jar /mnt/sdcard/
echo "3"