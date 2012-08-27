SET target=csPluginAddons
SET jar=code4bones.jar
SET com=com/google/cachesync
SET utl=com/code4bones/utils/
SET ws=D:\devel\prj\src\Android\CacheSync
SET root=%ws%\bin\classes\

set base=-C %root%
SET classes=-C %root% %com%/CommandObj.class -C %root% %com%/ICommandObj.class -C %root% %com%/CommandArgs.class
set classes=%classes% %base% %com/Commands.class

jar cvf %jar% %classes% -C %root% %utl%
copy %ws%\%jar% D:\devel\prj\src\Android\%target%\libs\