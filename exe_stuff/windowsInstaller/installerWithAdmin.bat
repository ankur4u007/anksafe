@echo off

Regedit /s registrys\reg1.reg
Regedit /s registrys\reg2.reg
Regedit /s registrys\reg3.reg
Regedit /s registrys\reg4.reg
Regedit /s registrys\reg5.reg
Regedit /s registrys\reg6.reg
setx ANKsafeDir "C:\Program Files\ANKsafe"

md "C:\Program Files\ANKsafe"
copy %~dp0\ANKsafe.exe "C:\Program Files\ANKsafe\ANKsafe.exe"

pause