set prj=CustomCommand
set jar=customcommand.jar
SET ROOT=D:\devel\prj\src\Android\%prj%\bin

mkdir %ROOT%\deploy
copy %ROOT%\%jar%  %ROOT%\deploy\
pushd %ROOT%\deploy\

call D:\devel\prj\lib\Android\android-sdk\platform-tools\dx.bat --dex --output=%ROOT%/deploy/classes.dex %ROOT%/deploy/%jar% 
D:\devel\prj\lib\Android\android-sdk\platform-tools\aapt.exe add %ROOT%/deploy/%jar% classes.dex
D:\devel\prj\lib\Android\android-sdk\platform-tools\adb.exe push %ROOT%/deploy/%jar% /mnt/sdcard/

popd