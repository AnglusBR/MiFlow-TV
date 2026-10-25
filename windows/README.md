# MiFlow Manager para Windows

Aplicativo nativo para localizar uma Mi Box na rede, conectar por ADB, instalar o MiFlow TV, instalar outros APKs, abrir a Google Play e restaurar o launcher original.

## Executar durante o desenvolvimento

```powershell
dotnet run --project .\windows\MiFlow.Manager\MiFlow.Manager.csproj
```

## Publicar

```powershell
dotnet publish .\windows\MiFlow.Manager\MiFlow.Manager.csproj -c Release -r win-x64 --self-contained true
```

O programa procura o `adb.exe` na pasta `platform-tools` ao lado do executável, nas variáveis `ANDROID_HOME`/`ANDROID_SDK_ROOT`, no Android SDK do usuário e no `PATH`.

Na primeira conexão, a Mi Box sempre exige que a chave RSA seja aceita na própria televisão. O gerenciador não tenta contornar essa proteção do Android.
