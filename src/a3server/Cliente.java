package a3server;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private String hostname;
    private int porta;

    public Cliente(String hostname, int porta) {
        this.hostname = hostname;
        this.porta = porta;
    }

    public void start() {
        System.out.println("Cliente iniciado. Conectando ao servidor...");

        try (Socket socket = new Socket(hostname, porta);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Digite seu nome: ");
            String nome = stdIn.readLine();
            out.println(nome);

            // Cria uma thread para ler e enviar mensagens do cliente para o servidor
            Thread inputThread = new Thread(() -> {
                try {
                    String userInput;
                    while ((userInput = stdIn.readLine()) != null) {
                        out.println(userInput);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputThread.start();

            // Lê e exibe as mensagens do servidor até que não haja mais mensagens disponíveis
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println("Servidor diz: " + serverResponse);
            }
        } catch (UnknownHostException e) {
            System.err.println("Host desconhecido: " + hostname);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite o endereço IP do servidor: ");
        String hostname = scanner.nextLine();

        System.out.print("Digite a porta do servidor: ");
        int porta = Integer.parseInt(scanner.nextLine());

        // Inicializa e inicia uma instância do cliente
        Cliente cliente = new Cliente(hostname, porta);
        cliente.start();
    }
}
