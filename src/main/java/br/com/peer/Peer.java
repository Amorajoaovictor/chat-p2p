package br.com.peer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Peer {
    private String username;
    private ServerSocket serverSocket;
    private List<PeerConnection> connections = new CopyOnWriteArrayList<>();
    private Map<Socket, PrintWriter> outputStreams = new ConcurrentHashMap<>();
    private volatile boolean running = true;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Inner class to represent a peer connection
    static class PeerConnection {
        Socket socket;
        String peerUsername;
        PrintWriter out;
        BufferedReader in;
        
        PeerConnection(Socket socket) {
            this.socket = socket;
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        boolean isConnected() {
            return socket != null && !socket.isClosed() && socket.isConnected();
        }
        
        void close() {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Peer(String username, int port) {
        this.username = username;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Peer " + username + " está ouvindo na porta " + port);
            
            // Add shutdown hook for graceful cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        } catch (IOException e) {
            System.err.println("Erro ao criar servidor na porta " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(this::listenForConnections).start();
        new Thread(this::listenForUserInput).start();
        
        System.out.println("Peer iniciado! Digite mensagens para enviar aos peers conectados.");
        System.out.println("Digite 'quit' para sair ou 'peers' para ver conexões ativas.");
    }

    private void listenForConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                PeerConnection peerConn = new PeerConnection(socket);
                connections.add(peerConn);
                
                System.out.println("[" + LocalDateTime.now().format(timeFormatter) + "] Nova conexão aceita de: " + 
                                 socket.getRemoteSocketAddress());
                
                // Start handshake to exchange usernames
                new Thread(() -> handleConnection(peerConn)).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }
    }

    private void handleConnection(PeerConnection peerConn) {
        try {
            // Send our username as handshake
            peerConn.out.println("HANDSHAKE:" + username);
            
            String message;
            while (running && peerConn.isConnected() && (message = peerConn.in.readLine()) != null) {
                if (message.startsWith("HANDSHAKE:")) {
                    // Receive peer's username
                    peerConn.peerUsername = message.substring(10);
                    System.out.println("[" + LocalDateTime.now().format(timeFormatter) + "] Conectado com peer: " + 
                                     peerConn.peerUsername);
                } else {
                    // Regular message
                    System.out.println("[" + LocalDateTime.now().format(timeFormatter) + "] " + message);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Conexão perdida com peer: " + e.getMessage());
            }
        } finally {
            // Clean up disconnected peer
            connections.remove(peerConn);
            peerConn.close();
            System.out.println("[" + LocalDateTime.now().format(timeFormatter) + "] Peer " + 
                             (peerConn.peerUsername != null ? peerConn.peerUsername : "desconhecido") + " desconectou");
        }
    }

    private void listenForUserInput() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            String message;
            while (running && (message = userInput.readLine()) != null) {
                if ("quit".equalsIgnoreCase(message.trim())) {
                    System.out.println("Encerrando...");
                    shutdown();
                    break;
                } else if ("peers".equalsIgnoreCase(message.trim())) {
                    showConnectedPeers();
                } else if (!message.trim().isEmpty()) {
                    broadcastMessage(message);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Erro ao ler entrada do usuário: " + e.getMessage());
            }
        }
    }

    private void broadcastMessage(String message) {
        if (connections.isEmpty()) {
            System.out.println("[" + LocalDateTime.now().format(timeFormatter) + "] Nenhum peer conectado para receber a mensagem.");
            return;
        }
        
        String formattedMessage = username + ": " + message;
        List<PeerConnection> toRemove = new ArrayList<>();
        
        for (PeerConnection peerConn : connections) {
            try {
                if (peerConn.isConnected()) {
                    peerConn.out.println(formattedMessage);
                    if (peerConn.out.checkError()) {
                        toRemove.add(peerConn);
                    }
                } else {
                    toRemove.add(peerConn);
                }
            } catch (Exception e) {
                System.err.println("Erro ao enviar mensagem para peer: " + e.getMessage());
                toRemove.add(peerConn);
            }
        }
        
        // Remove dead connections
        for (PeerConnection deadConn : toRemove) {
            connections.remove(deadConn);
            deadConn.close();
            System.out.println("Removida conexão morta com peer: " + 
                             (deadConn.peerUsername != null ? deadConn.peerUsername : "desconhecido"));
        }
        
        System.out.println("[" + LocalDateTime.now().format(timeFormatter) + "] Eu: " + message);
    }

    public void connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            PeerConnection peerConn = new PeerConnection(socket);
            connections.add(peerConn);
            
            // Send handshake
            peerConn.out.println("HANDSHAKE:" + username);
            
            new Thread(() -> handleConnection(peerConn)).start();
            System.out.println("Conectado ao peer em " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao peer em " + host + ":" + port + ": " + e.getMessage());
        }
    }
    
    private void showConnectedPeers() {
        if (connections.isEmpty()) {
            System.out.println("Nenhum peer conectado.");
        } else {
            System.out.println("Peers conectados (" + connections.size() + "):");
            for (int i = 0; i < connections.size(); i++) {
                PeerConnection conn = connections.get(i);
                String peerName = conn.peerUsername != null ? conn.peerUsername : "desconhecido";
                String status = conn.isConnected() ? "conectado" : "desconectado";
                System.out.println("  " + (i + 1) + ". " + peerName + " (" + status + ")");
            }
        }
    }
    
    public void shutdown() {
        running = false;
        
        // Close all peer connections
        for (PeerConnection conn : connections) {
            conn.close();
        }
        connections.clear();
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar servidor: " + e.getMessage());
        }
        
        System.out.println("Peer " + username + " encerrado.");
    }
    
    // Method to simulate sending a message (for testing purposes)
    public void simulateMessage(String message) {
        broadcastMessage(message);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Solicita o nome do usuário
            System.out.print("Digite seu nome de usuário: ");
            if (!scanner.hasNextLine()) {
                System.err.println("Entrada não disponível.");
                return;
            }
            String username = scanner.nextLine();

            // Solicita a porta
            System.out.print("Digite a porta para escutar: ");
            if (!scanner.hasNextInt()) {
                System.err.println("Porta inválida.");
                return;
            }
            int port = scanner.nextInt();
            scanner.nextLine(); // Consumir a nova linha pendente

            // Inicia o peer
            Peer peer = new Peer(username, port);
            peer.start();

            // Pergunta se deseja conectar a outro peer
            System.out.print("Deseja conectar a outro peer? (s/n): ");
            if (!scanner.hasNextLine()) {
                System.out.println("Continuando sem conectar a outros peers...");
            } else {
                String resposta = scanner.nextLine();

                if (resposta.equalsIgnoreCase("s")) {
                    System.out.print("Digite o endereço do peer (host): ");
                    if (scanner.hasNextLine()) {
                        String peerHost = scanner.nextLine();

                        System.out.print("Digite a porta do peer: ");
                        if (scanner.hasNextInt()) {
                            int peerPort = scanner.nextInt();
                            scanner.nextLine(); // Consumir a nova linha pendente

                            // Conecta ao peer
                            peer.connectToPeer(peerHost, peerPort);
                        }
                    }
                }
            }

            // Keep the main thread alive - the input handling is done in listenForUserInput thread
            try {
                // Wait for the peer to shutdown (when user types 'quit')
                while (peer.running) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            System.err.println("Erro durante execução: " + e.getMessage());
        } finally {
            scanner.close();
        }
        
        System.exit(0);
    }
}