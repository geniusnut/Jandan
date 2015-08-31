@echo �л�����Ŀ¼
cd /d %~dp0

set CurrentDate=%date:~0,4%%date:~5,2%%date:~8,2%
set CurrentTime=%time:~0,2%%time:~3,2%%time:~6,2%
set Resultpath="E:\KINGSOFT_DUBA\build\Build_Result\ksmobile\%module_name%\%CurrentDate%.%BUILD_NUMBER%"
set PythonExePath="E:\KINGSOFT_DUBA\build\Build_Tools\Python\python.exe"
set ANT_OPTS=%ANT_OPTS% -Xmx256m

del /f /q "%cd%\assets\descpvirus.db"
del /f /q "%cd%\assets\privacy_cache.db"
del /f /q "%cd%\assets\process_tips.db"
del /f /q "%cd%\assets\se_cloud_hf.db"
del /f /q "%cd%\assets\strings2_*.db.zip"
del /f /q "%cd%\assets\clearpath5_*.db.zip"


del /f /a /q "%cd%\src\com\cleanmaster\resource"
del /f /a /q "%cd%\product"
del /f /a /q "%cd%\product\bin0"
del /a /q "E:\KINGSOFT_DUBA\build\Build_Result\ksmobile"

rd /s /q "%cd%\src\com\cleanmaster\resource"
rd /s /q "%cd%\product"
rd /s /q "%cd%\product\bin0"
rd /s /q "E:\KINGSOFT_DUBA\build\Build_Result\ksmobile"

md "%cd%\product"
rem md "%cd%\product\bin0"
md "%cd%\product\bin1"
md "%cd%\product\bin2"
md "E:\KINGSOFT_DUBA\build\Build_Result\ksmobile"

echo %BUILD_NUMBER% > E:\KINGSOFT_DUBA\build\Build_Files\MobileDuba.txt

echo ���´��롭��
call svn revert -R "%cd%"
call svn up "%cd%"

rem echo ��ͬ������
rem call "%PythonExePath%" "%cd%\build\sync_signs.py"

echo �޸İ汾����.....
call "%PythonExePath%" E:/KINGSOFT_DUBA/Build/Build_Scripts/changever_ksmobile_cmlocker.py %module_name%
call "%PythonExePath%" "%cd%\build\sync_ver.py"

call "%PythonExePath%" "%cd%\build\changebuild0\before.py"
rem echo building normal
rem "%windir%\system32\cmd.exe" /c "%ANT_HOME%\bin\ant" clean
rem "%windir%\system32\cmd.exe" /c "%ANT_HOME%\bin\ant" cmrelease
rem del /f /a /q "%cd%\bin0"
rem rd /s /q "%cd%\bin0"
rem move /y "%cd%\bin" "%cd%\bin0"

echo building version1
call "%PythonExePath%" "%cd%\build\changever.py" %module_name% 1
"%windir%\system32\cmd.exe" /c "%ANT_HOME%\bin\ant" clean
"%windir%\system32\cmd.exe" /c "%ANT_HOME%\bin\ant" cmlocker-release
del /f /a /q "%cd%\bin1"
rd /s /q "%cd%\bin1"
move /y "%cd%\bin" "%cd%\bin1"

echo building version2
call "%PythonExePath%" "%cd%\build\changever.py" %module_name% 2
"%windir%\system32\cmd.exe" /c "%ANT_HOME%\bin\ant" clean
"%windir%\system32\cmd.exe" /c "%ANT_HOME%\bin\ant" cmlocker-beta
del /f /a /q "%cd%\bin2"
rd /s /q "%cd%\bin2"
move /y "%cd%\bin" "%cd%\bin2"
call "%PythonExePath%" "%cd%\build\changebuild0\after.py"

rem "%windir%\system32\xcopy.exe" "%cd%\bin0\*.*" "%cd%\product\bin0" /e /y /exclude:build\skip.txt
"%windir%\system32\xcopy.exe" "%cd%\bin1\*.*" "%cd%\product\bin1" /e /y /exclude:build\skip.txt
"%windir%\system32\xcopy.exe" "%cd%\bin2\*.*" "%cd%\product\bin2" /e /y /exclude:build\skip.txt

Md "%Resultpath%"

"%windir%\system32\xcopy.exe" "%cd%\product\*.*" /e/y "%Resultpath%"

call E:\KINGSOFT_DUBA\build\Build_Files\PutFileToFtp_ksmobile.build.cmd

if ERRORLEVEL 0	EXIT
