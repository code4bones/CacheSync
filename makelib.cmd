echo %1
SET jar=code4bones.jar
SET com=com/google/cachesync
SET utl=com/code4bones/utils/
SET ws=D:\devel\prj\src\Android\CacheSync
SET root=%ws%\bin\classes\

jar cvf %jar% -C %root% %com%/CommandObj.class -C %root% %com%/ICommandObj.class -C %root% %com%/CommandArgs.class -C %root% %utl%
copy %ws%\%jar% D:\devel\prj\src\Android\CustomCommand\libs\