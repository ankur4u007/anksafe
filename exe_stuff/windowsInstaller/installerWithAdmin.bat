@echo off
REG IMPORT %~dp0\ANKsafeReg.reg
md "C:\Program Files\ANKsafe"
copy %~dp0\ANKsafe.exe "C:\Program Files\ANKsafe\ANKsafe.exe"
pause