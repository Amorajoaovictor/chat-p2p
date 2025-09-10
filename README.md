# Chat P2P

Um sistema de chat peer-to-peer em Java que permite a comunicação direta entre dois ou mais usuários.

## Características

- **Comunicação bidirecional** - Ambos os peers podem enviar e receber mensagens
- **Handshake automático** - Os peers trocam identificação automaticamente
- **Timestamps** - Todas as mensagens incluem horário de envio
- **Gerenciamento robusto de conexões** - Detecção e limpeza automática de conexões mortas
- **Interface amigável** - Comandos simples e feedback claro

## Como usar

### Executar o chat interativo

```bash
./gradlew run
```

Siga as instruções:
1. Digite seu nome de usuário
2. Digite a porta para escutar (ex: 8001)
3. Escolha se deseja conectar a outro peer
4. Se sim, forneça o endereço e porta do outro peer

### Executar demonstração

Para ver uma demonstração automática de dois peers conversando:

```bash
./gradlew runDemo
```

### Comandos durante o chat

- Digite qualquer mensagem para enviar aos peers conectados
- `peers` - mostra peers atualmente conectados
- `quit` - encerra o aplicativo

## Exemplo de uso

### Terminal 1 (Alice):
```
Digite seu nome de usuário: Alice
Digite a porta para escutar: 8001
Deseja conectar a outro peer? (s/n): n
Peer Alice está ouvindo na porta 8001
Peer iniciado! Digite mensagens para enviar aos peers conectados.
```

### Terminal 2 (Bob):
```
Digite seu nome de usuário: Bob
Digite a porta para escutar: 8002
Deseja conectar a outro peer? (s/n): s
Digite o endereço do peer (host): localhost
Digite a porta do peer: 8001
Conectado ao peer em localhost:8001
```

### Chat em ação:
```
[10:30:15] Conectado com peer: Bob
Alice> Olá Bob!
[10:30:20] Bob: Oi Alice! Como vai?
Alice> Estou bem, obrigada!
```

## Arquitetura

O sistema utiliza:
- **ServerSocket** para aceitar conexões de outros peers
- **Socket** para conectar a outros peers
- **Threads** para gerenciar múltiplas conexões simultaneamente
- **Protocolo de handshake** para identificação de peers
- **Gerenciamento thread-safe** de conexões

## Melhorias implementadas

- ✅ Comunicação bidirecional confiável
- ✅ Protocolo de identificação de peers
- ✅ Detecção de conexões mortas
- ✅ Formatação de mensagens com timestamps
- ✅ Tratamento robusto de erros
- ✅ Limpeza adequada de recursos
- ✅ Interface de usuário melhorada