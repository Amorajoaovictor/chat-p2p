package br.com.peer;

/**
 * Demonstration class to show how two peers can communicate
 * This creates two peer instances and shows them communicating
 */
public class PeerChatDemo {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Demonstração de Chat P2P ===");
            System.out.println("Criando dois peers para demonstrar comunicação bidirecional...\n");
            
            // Create first peer (Alice)
            Peer alice = new Peer("Alice", 8001);
            alice.start();
            
            // Wait a bit for Alice to start
            Thread.sleep(1000);
            
            // Create second peer (Bob)
            Peer bob = new Peer("Bob", 8002);
            bob.start();
            
            // Wait a bit for Bob to start
            Thread.sleep(1000);
            
            System.out.println("Conectando Bob com Alice...");
            // Connect Bob to Alice
            bob.connectToPeer("localhost", 8001);
            
            // Wait for connection to establish
            Thread.sleep(2000);
            
            System.out.println("\n=== Simulando troca de mensagens ===");
            
            // Simulate Alice sending a message
            System.out.println("\nAlice envia: 'Olá Bob!'");
            alice.simulateMessage("Olá Bob!");
            
            Thread.sleep(1000);
            
            // Simulate Bob responding
            System.out.println("\nBob responde: 'Oi Alice! Como vai?'");
            bob.simulateMessage("Oi Alice! Como vai?");
            
            Thread.sleep(1000);
            
            // Alice responds
            System.out.println("\nAlice responde: 'Estou bem! E você?'");
            alice.simulateMessage("Estou bem! E você?");
            
            Thread.sleep(1000);
            
            // Bob's final message
            System.out.println("\nBob responde: 'Também estou bem! Obrigado!'");
            bob.simulateMessage("Também estou bem! Obrigado!");
            
            Thread.sleep(2000);
            
            System.out.println("\n=== Demonstração concluída com sucesso! ===");
            System.out.println("Os dois peers conseguiram se comunicar bidirecionalmente.");
            
            // Cleanup
            alice.shutdown();
            bob.shutdown();
            
        } catch (Exception e) {
            System.err.println("Erro durante demonstração: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}