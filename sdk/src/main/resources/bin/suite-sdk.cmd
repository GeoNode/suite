@echo off

:: Check for ant on the path
call ant -version >nul 2>nul || (
  echo Requires Apache Ant ^(see http://ant.apache.org/^)
  exit /b 1
)

set COMMAND=
set APP_PATH=
set ANT_ARGS=
set NAME=%~n0
:: default repeated in build.xml, but used here for success message
set LOCAL_PORT=9080 

:: Find the full path of SDK_HOME 
pushd "%~dp0.."
set SDK_HOME="%cd%"
popd

:: Determine if proper arguments have been supplied
if "x%~1"=="x" goto Usage
if "%~1"=="version" goto Version
if "%~1"=="--version" goto Version
if "%~1"=="create" goto Create
if "%~1"=="debug" goto Debug
if "%~1"=="package" goto Package
goto Usage

:lastarg
:: Figures out what the last and second to last arguments are
set "PREV_ARG=%LAST_ARG%"
set "LAST_ARG=%~1"
shift
if not "%~1"=="" goto lastarg
goto :eof

:argcount
:: Figures out how many args there are
set argC=0
for %%x in (%*) do Set /A argC+=1
goto :eof

:Version
set COMMAND="version"
goto Run


:Create
:: Create takes only two arguments, all required
if "x%~2"=="x" goto Usage
if "%~2"=="-h" goto UsageCreate
if "%~2"=="--help" goto UsageCreate
if "x%~3"=="x" goto UsageCreate
if "x%~4" NEQ "x" goto UsageCreate

:: Determine if there are "extra" flags
call :lastarg %*
:: If the second to last or last argument starts with a -, dump to usage
if "%PREV_ARG:~0,1%"=="-" (
  echo Invalid argument: %PREV_ARG%
  goto UsageCreate
)
if "%LAST_ARG:~0,1%"=="-" (
  echo Invalid argument: %LAST_ARG%
  goto UsageCreate
)

set COMMAND="%~1"
set APP_PATH="%~2"
set APP_TEMPLATE="%~3"
set ANT_ARGS=%ANT_ARGS% -Dapp.path=%APP_PATH% -Dapp.template=%APP_TEMPLATE%
goto Run


:Debug
if "x%~2"=="x" goto Usage
if "%~2"=="-h" goto UsageDebug
if "%~2"=="--help" goto UsageDebug

:: Determine if there are "extra" flags
call :lastarg %*
:: If the second to last or last argument starts with a -, dump to usage
if "%PREV_ARG:~0,1%"=="-" (
  echo Invalid argument: %PREV_ARG%
  goto UsageDebug
)
if "%LAST_ARG:~0,1%"=="-" (
  echo Invalid argument: %LAST_ARG%
  goto UsageDebug
)
set COMMAND="%~1"
shift

:: Must be an even number of arguments
:: (Two for each command flag, one for command, one for app-path)
set /a ARGCOUNT = 0
for %%a in (%*) do set /a ARGCOUNT += 1
set /a ARGODD = "%ARGCOUNT% %% 2"
if %ARGODD% == 1 goto Usage
goto DebugFlagLoop

:DebugFlagLoop
:: Sets all of the command flags (ant arguments)
:: And whatever remains should be the app-path

set ARG=%~1

:: Checking for a valid flag
rem TODO What about bad flags?
set flag=0
if "%~1"=="-l" set flag=l
if "%~1"=="--local-port" set flag=l
if "%~1"=="-g" set flag=g
if "%~1"=="--geoserver" set flag=g
if "%flag%"=="0" (
  :: Make sure we don't have an invalid flag
  if "%ARG:~0,1%"=="-" (
    echo Invalid Argument: %ARG%
    goto UsageDebug
  )
  :: Must be one arg remaining, otherwise fail
  if "%~1"=="%LAST_ARG%" (
    goto DebugPath
  ) else (
    goto UsageDebug
  )
)

if "%flag%"=="l" (
  if "x%~2"=="x" goto Usage
  set ANT_ARGS=%ANT_ARGS% -Dapp.port=%2
  set LOCAL_PORT=%2
  shift && shift
)

if "%flag%"=="g" (
  if "x%~2"=="x" goto Usage
  set ANT_ARGS=%ANT_ARGS% -Dapp.proxy.geoserver=%2
  shift && shift
)

:: Keep going until one arg left
goto DebugFlagLoop


:DebugPath
:: First argument is not a flag, so must assume that
:: it's the app-path.
set APP_PATH="%~1"
set ANT_ARGS=%ANT_ARGS% -Dapp.path=%APP_PATH%
goto Run


:Package
if "x%~2"=="x" goto Usage
if "%~2"=="-h" goto UsagePackage
if "%~2"=="--help" goto UsagePackage
if "x%~3"=="x" (
  :: build-path omitted, using current directory
  set WAR_PATH="%CD%"
) else (
  set WAR_PATH="%~3"
)
if "x%~4" NEQ "x" goto UsagePackage

set COMMAND="%~1"
set APP_PATH="%~2"
set ANT_ARGS=%ANT_ARGS% -Dapp.path=%APP_PATH% -Dapp.warpath=%WAR_PATH%
goto Run


:Usage
echo Usage: %NAME% ^<command^> ^<args^>
echo.
echo List of commands:
echo     create      Create a new application.
echo     debug       Run an existing application in debug mode.
echo     package     Create a WAR file.
echo.
echo See '%NAME% ^<command^> --help' for more detail on a specific command.
echo.
exit /b

:UsageCreate
echo Usage: %NAME% create ^<app-path^> ^<app-template^>
echo.
echo Create a new application. A new directory will be created using the ^<app-path^> 
echo argument (it must not already exist). Possible values for ^<app-template^> are:
echo.
echo     gxp        a template based on GXP, GeoExt and OpenLayers 2
echo     ol3view    a template based on OpenLayers 3 and bootstrap for viewing
echo     ol3edit    a template based on OpenLayers 3 and bootstrap for editing
echo.
exit /b

:UsageDebug
echo Usage: %NAME% debug [^<options^>] ^<app-path^>
echo.
echo Debug an existing application. The ^<app-path^> argument must be the path to an
echo existing application.
echo.
echo List of options:
echo.
echo     -l ^| --local-port   port    Port for the local debug server. Default is 
echo                                 9080.
echo.
echo     -g ^| --geoserver    url     URL for a remote GeoServer to proxy. The debug
echo                                 server will make the remote GeoServer available
echo                                 from the "/geoserver" path within the 
echo                                 application.
echo.
exit /b

:UsagePackage
echo Usage: %NAME% package ^<app-path^> ^<build-path^>
echo.
echo Package an existing application. The ^<app-path^> argument must be the path to an
echo existing application. The ^<build-path^> is the location where the WAR file package
echo will be created.
echo.
exit /b


:Run
:: Create log files (in case they don't already exist)
set LOG_DIR=%TEMP%\suite-sdk
set LOG_FILE=%LOG_DIR%\suite-sdk.log
set ANT_LOG=%LOG_DIR%\ant.log

mkdir "%LOG_DIR%" >nul 2>nul
del "%LOG_FILE%" >nul 2>nul
del "%ANT_LOG%" >nul 2>nul
type nul>"%LOG_FILE%"
type nul>"%ANT_LOG%"

if not exist "%LOG_FILE%" (
  set LOG_FILE=nul
)
if not exist "%ANT_LOG%" (
  set ANT_LOG=nul
)

:: Provide feedback that work is starting
if %COMMAND%=="create" (
  echo.
  echo Creating application ...
  echo.
)
if %COMMAND%=="debug" (
  echo.
  echo Starting debug server for application ^(use CTRL+C to stop^)
  echo.
)
if %COMMAND%=="package" (
  echo.
  echo Packaging application ^(this may take a few moments^) ...
  echo.
)

call ant -e -f %SDK_HOME%\build.xml -Dsdk.logfile="%LOG_FILE%" -Dsdk.home=%SDK_HOME% -Dbasedir=. %COMMAND% %ANT_ARGS% 2>>"%ANT_LOG%"

:: Handle results
IF %ERRORLEVEL% NEQ 0 (
  if %COMMAND%=="create" (
    echo.
    echo The '%NAME% create' command failed.
    echo.
    echo A common cause of this is the failure to create the provided directory:
    echo %APP_PATH%. Please ensure that the directory name is valid and that you
    echo have permission to create this directory.
    echo.
    echo Another common cause is that the ^<app-template^> value is invalid
    echo ^(should be one of: gxp, ol3view or ol3edit^). Option specified was: 
    echo %APP_TEMPLATE%
    echo.
    echo Please run '%NAME% create --help' for help on the usage.
    echo.
  )
  if %COMMAND%=="debug" (
    echo.
    echo The '%NAME% debug' command failed.
    echo.
    echo Two commmon causes of this are:
    echo * The directory provided did not contain a valid SDK application: %APP_PATH%
    echo * There was a conflict with the provided local port ^(-l^): %LOCAL_PORT%
    echo.
    echo Please run '%NAME% debug --help' for help on the usage.
    echo.
  )
  if %COMMAND%=="package" (
    echo.
    echo The '%NAME% package' command failed.
    echo.
    echo Two commmon causes of this are:
    echo * The ^<app-path^> directory does not contain a valid application: %APP_PATH%
    echo * The ^<build-path^> directory must be valid and writeable: %WAR_PATH%
    echo.
    echo Please run '%NAME% package --help' for help on the usage.
    echo.
  )
  echo See the logfile '%LOG_FILE%' for more detail on what went wrong.
  echo.
)
 

::Merge the two different log files at the end
copy /y "%LOG_FILE%"+"%ANT_LOG%" "%LOG_FILE%" >nul 2>nul
del "%ANT_LOG%" >nul 2>nul
