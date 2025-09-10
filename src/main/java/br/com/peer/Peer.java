package br.com.peer;

import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
    private String username;
    private ServerSocket serverSocket;
    private List<Socket> connections = new ArrayList<>();

    public Peer(String username, int port) {
        this.username = username;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Peer " + username + " está ouvindo na porta " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(this::listenForConnections).start();
        new Thread(this::listenForUserInput).start();
    }

    private void listenForConnections() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                connections.add(socket);
                new Thread(() -> handleConnection(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForUserInput() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String message = userInput.readLine();
                broadcastMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        for (Socket socket : connections) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(username + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            connections.add(socket);
            new Thread(() -> handleConnection(socket)).start();
            System.out.println("Conectado ao peer em " + host + ":" + port);
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao peer em " + host + ":" + port);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Solicita o nome do usuário
        System.out.print("Digite seu nome de usuário: ");
        String username = scanner.nextLine();

        // Solicita a porta
        System.out.print("Digite a porta para escutar: ");
        int port = scanner.nextInt();
        scanner.nextLine(); // Consumir a nova linha pendente

        // Inicia o peer
        Peer peer = new Peer(username, port);
        peer.start();

        // Pergunta se deseja conectar a outro peer
        System.out.print("Deseja conectar a outro peer? (s/n): ");
        String resposta = scanner.nextLine();

        if (resposta.equalsIgnoreCase("s")) {
            System.out.print("Digite o endereço do peer (host): ");
            String peerHost = scanner.nextLine();

            System.out.print("Digite a porta do peer: ");
            int peerPort = scanner.nextInt();
            scanner.nextLine(); // Consumir a nova linha pendente

            // Conecta ao peer
            peer.connectToPeer(peerHost, peerPort);
        }

        scanner.close();
    }
}