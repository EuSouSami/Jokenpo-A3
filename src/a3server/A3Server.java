package a3server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

public class A3Server {
    private static final int PORTA = 31000;
    private static final BlockingQueue<Escolha> escolhas = new ArrayBlockingQueue<>(2);
    private static final ConcurrentHashMap<Integer, PrintWriter> clientesWriters = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, String> nomesClientes = new ConcurrentHashMap<>();
    private static int clientCount = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor ouvindo na porta " + PORTA);

            while (true) {
                // Aceita conexões de clientes
                Socket clientSocket = serverSocket.accept();
                clientCount++;
                System.out.println("Cliente " + clientCount + " conectado: " + clientSocket.getInetAddress());

                // Cria e inicia uma thread para lidar com o cliente
                ClientHandler handler = new ClientHandler(clientSocket, escolhas, clientCount, clientesWriters, nomesClientes);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Escolha {
    int clientId;
    String escolha;

    public Escolha(int clientId, String escolha) {
        this.clientId = clientId;
        this.escolha = escolha;
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BlockingQueue<Escolha> escolhas;
    private int clientId;
    private ConcurrentHashMap<Integer, PrintWriter> clientesWriters;
    private ConcurrentHashMap<Integer, String> nomesClientes;

    public ClientHandler(Socket socket, BlockingQueue<Escolha> escolhas, int clientId, ConcurrentHashMap<Integer, PrintWriter> clientesWriters, ConcurrentHashMap<Integer, String> nomesClientes) {
        this.clientSocket = socket;
        this.escolhas = escolhas;
        this.clientId = clientId;
        this.clientesWriters = clientesWriters;
        this.nomesClientes = nomesClientes;
    }

    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Recebe e armazena o nome do cliente
            String nome = in.readLine();
            nomesClientes.put(clientId, nome);
            out.println("Bem-vindo, " + nome + "!");

            // Armazena o PrintWriter do cliente
            clientesWriters.put(clientId, out);

            boolean jogar = true;

            while (jogar) {
                out.println("----------------------------");
                out.println("|  Escolha o modo de Jogo   |");
                out.println("|  1 - VS Computador        |");
                out.println("|  2 - VS Player            |");
                out.println("|  0 - Encerrar             |");
                out.println("-----------------------------");
                String escolhaModo = in.readLine();
                int modo = Integer.parseInt(escolhaModo);

                if (modo == 0) {
                    jogar = false;
                    out.println("Jogo encerrado. Obrigado por jogar!");
                    clientesWriters.remove(clientId);
                    nomesClientes.remove(clientId);
                    clientSocket.close();
                    break;
                } else if (modo == 2) {
                    out.println("Escolha uma das opcoes abaixo:");
                    out.println("1 - Pedra");
                    out.println("2 - Papel");
                    out.println("3 - Tesoura");
                    out.println("0 - Encerrar");

                    // Recebe a escolha do cliente
                    String escolhaCliente = in.readLine();
                    if ("0".equals(escolhaCliente)) {
                        jogar = false;
                        out.println("Jogo encerrado. Obrigado por jogar!");
                        clientesWriters.remove(clientId);
                        nomesClientes.remove(clientId);
                        clientSocket.close();
                        break;
                    }
                    System.out.println("Escolha do Cliente " + nome + ": " + escolhaCliente);

                    // Armazena a escolha do cliente
                    escolhas.put(new Escolha(clientId, escolhaCliente));

                    // Envia uma mensagem ao cliente indicando que a escolha foi registrada
                    out.println("Escolha registrada. Aguardando o outro jogador...");

                    // Espera até que ambos os clientes tenham feito suas escolhas
                    if (escolhas.size() == 2) {
                        // Determina o resultado do jogo
                        Escolha escolha1 = escolhas.take();
                        Escolha escolha2 = escolhas.take();
                        String resultado = determinarResultado(escolha1, escolha2);

                        // Envia o resultado para ambos os clientes
                        enviarResultado(resultado, escolha1.clientId, escolha2.clientId);
                        out.println("Desejam jogar novamente?");
                            out.println("1 - Sim");
                            out.println("2 - Nao");
                            String resposta = in.readLine();
                            if ("2".equals(resposta)) {
                                jogar = false;
                        }
                    }
                } 
                else if (modo == 1) {
                    out.println("Escolha uma das opcoes abaixo:");
                    out.println("1 - Pedra");
                    out.println("2 - Papel");
                    out.println("3 - Tesoura");
                    out.println("0 - Encerrar");

                    // Recebe a escolha do cliente
                    String escolhaCliente = in.readLine();
                    if ("0".equals(escolhaCliente)) {
                        jogar = false;
                        out.println("Jogo encerrado. Obrigado por jogar!");
                        clientesWriters.remove(clientId);
                        nomesClientes.remove(clientId);
                        clientSocket.close();
                        break;
                    }
                    System.out.println("Escolha do cliente: " + escolhaCliente);

                    // Gera uma escolha aleatória para o servidor
                    Random random = new Random();
                    int escolhaServidor = random.nextInt(3) + 1; // Gera um número aleatório entre 1 e 3
                    System.out.println("Escolha do servidor: " + escolhaServidor);

                    // Envia a escolha do servidor de volta ao cliente
                    out.println("Você escolheu: " + escolhaCliente + ". Servidor escolheu: " + escolhaServidor);

                    // Determina o resultado do jogo
                    String resultado = determinarResultado(new Escolha(clientId, escolhaCliente), new Escolha(0, Integer.toString(escolhaServidor)));
                    out.println(resultado);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String determinarResultado(Escolha escolha1, Escolha escolha2) {
        int x = Integer.parseInt(escolha1.escolha);
        int y = Integer.parseInt(escolha2.escolha);

        if (x == y) {
            return "Empate";
        } else if (x == 1 && y == 2 || x == 2 && y == 3 || x == 3 && y == 1) {
            if (escolha2.clientId == 0) {
                return "Vitoria do Computador";
            } else {
                return "Vitoria de " + nomesClientes.get(escolha2.clientId);
            }
        } else {
            return "Vitoria de " + nomesClientes.get(escolha1.clientId);
        }
    }

    private void enviarResultado(String resultado, int clientId1, int clientId2) {
        clientesWriters.get(clientId1).println("Resultado do jogo: " + resultado);
        clientesWriters.get(clientId2).println("Resultado do jogo: " + resultado);
    }
}
