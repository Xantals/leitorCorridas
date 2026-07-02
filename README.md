# Uber Ride Reader — protótipo

Protótipo de app Android que lê notificações do **app Uber Driver** e tenta
extrair embarque/desembarque de uma solicitação de corrida.

## Como gerar o APK pelo GitHub (sem instalar Android Studio)

1. Crie um repositório novo no GitHub (pode ser privado).
2. Suba todo o conteúdo desta pasta para o repositório (incluindo a pasta
   `.github/workflows`, que costuma ficar oculta — confira se ela foi
   enviada).
3. Vá na aba **Actions** do repositório. O workflow "Build APK" deve rodar
   automaticamente após o push (leva alguns minutos).
4. Quando o build terminar (ícone verde ✅), clique nele e desça até
   **Artifacts** → baixe `uber-ride-reader-debug-apk` (vem como .zip,
   dentro tem o `app-debug.apk`).
5. Transfira o `app-debug.apk` para o celular (Google Drive, WhatsApp, cabo
   USB, o que for mais fácil) e toque nele para instalar — o Android vai
   pedir para permitir "instalar apps de fontes desconhecidas" na primeira
   vez.

Se o build falhar (ícone vermelho ❌), clique nele para ver o log de erro —
me manda a mensagem que aparecer que eu ajudo a corrigir.

## Como usar (rodando localmente pelo Android Studio)

1. Abra a pasta `UberRideReader` no Android Studio (crie um projeto novo
   "Empty Views Activity" em Kotlin e substitua os arquivos gerados pelos
   deste pacote, mantendo a mesma estrutura de pastas).
2. Rode o app num celular real com o app **Uber Driver** instalado
   (emulador não recebe notificações reais da Uber).
3. Toque em "Ativar acesso a notificações" — isso abre a tela do sistema
   Configurações > Apps > Acesso a notificações. Marque o app.
4. Deixe o app em segundo plano e aguarde uma notificação de corrida chegar.

## Passo importante: descobrir o formato real do texto

O texto exato que a Uber usa nas notificações **não é documentado** e muda
com frequência. O parser em `RideParser.kt` é só um ponto de partida.

Para calibrar:
1. Rode o app com o `Log.d(TAG, ...)` ativo (já está no código).
2. Com o celular conectado via USB, rode no terminal:
   ```
   adb logcat | grep UberNotifListener
   ```
3. Quando uma notificação de corrida chegar, veja no log o texto exato
   (`title` e `bigText`).
4. Ajuste as regex em `RideParser.kt` para bater com esse formato real.

Alternativa mais robusta que regex: em vez de tentar casar padrões de texto,
você pode simplesmente exibir o texto bruto da notificação (já faço isso na
tela, no campo "Texto original da notificação") e refinar a extração aos
poucos conforme for vendo casos reais.

## Limitações e avisos

- **Confirme o nome do pacote do app Uber Driver** no seu celular. Pode
  variar por região (`com.ubercab.driver` é o mais comum, mas verifique
  com `adb shell dumpsys notification` enquanto uma notificação chega).
- Esse tipo de app depende inteiramente do formato de notificação da Uber,
  que pode mudar sem aviso a qualquer atualização do app deles — não há
  API oficial pública para esses dados.
- Automatizar a leitura de dados de outro app pode esbarrar nos Termos de
  Uso da Uber (uso de dados/automação não documentada). Vale revisar os
  Termos de Serviço deles antes de usar isso em produção ou distribuir
  para terceiros — aqui é só um protótipo pessoal/educacional.
- A permissão de "Acesso a notificações" dá ao seu app visibilidade de
  *todas* as notificações do celular, não só da Uber — o filtro por pacote
  no código evita processar as outras, mas tecnicamente o Android concede
  acesso amplo.
