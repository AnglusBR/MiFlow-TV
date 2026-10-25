# MiFlow TV

Um launcher leve, bonito e feito para controle remoto, criado especialmente com a **Xiaomi Mi Box S MDZ-22-AB / Android TV 9** em mente.

> Projeto independente e não oficial. Mi, Mi Box e Xiaomi são marcas da Xiaomi Corporation. Nenhum código, firmware ou ativo proprietário da Xiaomi é distribuído aqui.

## O que já funciona

- visual original em laranja, com superfícies translúcidas e três temas;
- navegação completa por D-pad, com foco grande e animações rápidas;
- favoritos persistentes, ordenados automaticamente na primeira execução;
- gaveta com todos os apps e categorias: favoritos, streaming, jogos e utilitários;
- segure **OK** para favoritar, abrir informações ou desinstalar um app;
- atalhos para Wi-Fi, Bluetooth e Configurações;
- relógio e data em português;
- nenhuma propaganda, telemetria, conta ou permissão de internet;
- Java puro, sem AndroidX e com suporte desde Android 6 (API 23).

## Instalar na Mi Box

### 1. Ative o modo desenvolvedor

Na TV: **Configurações → Preferências do dispositivo → Sobre → Build**. Aperte OK sete vezes. Depois ative **Depuração USB** nas opções do desenvolvedor.

Descubra o IP em **Configurações → Rede e Internet**.

### 2. Compile e instale

No PowerShell, dentro desta pasta:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
adb connect IP_DA_MI_BOX:5555
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

Ou use o instalador assistido:

```powershell
.\tools\install.ps1 -Device IP_DA_MI_BOX:5555
```

A TV pedirá autorização para depuração. Aceite usando o controle.

### 3. Escolha o launcher

Depois da instalação, aperte **Home** e selecione **MiFlow TV → Sempre**. Se o Android TV não mostrar o seletor:

```powershell
adb shell pm disable-user --user 0 com.google.android.tvlauncher
adb shell input keyevent KEYCODE_HOME
```

O comando acima é reversível e foi escolhido para a Mi Box S com Android TV 9. Ele não deve ser usado às cegas em aparelhos Google TV mais novos.

## Voltar ao launcher original

```powershell
.\tools\restore.ps1 -Device IP_DA_MI_BOX:5555
```

Ou manualmente:

```powershell
adb shell pm enable com.google.android.tvlauncher
adb uninstall com.miflow.tv
```

## Desenvolvimento

Requisitos:

- Android Studio ou JDK 17;
- Android SDK Platform 35;
- Android build-tools;
- `adb` para instalar no aparelho.

Verificação completa:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

## Próximos passos

- editor visual da ordem dos favoritos;
- wallpapers próprios opcionais;
- modo infantil com PIN;
- backup/restauração das preferências;
- atualização automática via GitHub Releases, opcional e separada do launcher.

## Referências e licença

O projeto foi desenhado do zero depois de estudar ideias de interface e instalação de [mibox-launcher](https://github.com/sacha9214/mibox-launcher), [BoxClean](https://github.com/VachaganGrigoryan/BoxClean) e [Lucerra Launcher](https://github.com/itsrajarshi/lucerra-launcher). Nenhum código ou ativo desses projetos foi copiado para o MiFlow TV.

Código sob licença [MIT](LICENSE).
