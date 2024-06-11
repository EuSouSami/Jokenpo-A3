# **Projeto criado por**
#### Alexsander Pacini Bittencourt | RA: 12523173616
#### Davi Jonas da Silva | RA: 12523173651
#### Leonardo Pedro dos Anjos | RA: 12523122745

# **Jogo Jokenpo em Socket** 🪨 📜 ✂️
Venha jogar o clássico Jokenpo "Pedra, Papel e Tesoura" utilizando comunicação via sockets para jogar em diferentes dispositivos na mesma rede.

### Dentro deste Sistema, você irá encontrar

## **Classe Servidor** 💻
O servidor é responsável por gerenciar as conexões dos clientes e coordenar o jogo. Ele aceita conexões de clientes e cria uma nova thread para cada cliente conectado. O servidor também recebe as escolhas dos jogadores, determina o resultado do jogo e envia as mensagens de volta aos clientes.

## **Classe Cliente** 🧑🏻
O cliente é responsável por se conectar ao servidor e interagir com o jogo. Ele solicita o nome do jogador, escolhe o modo de jogo (jogar contra o computador ou contra outro jogador) e faz suas escolhas (Pedra, Papel ou Tesoura). Após cada jogada, o cliente recebe o resultado do jogo e pode escolher jogar novamente ou encerrar o jogo.
