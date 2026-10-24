import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import java.util.Scanner;

public class CodebreakerAgent extends Agent {
    private int step = 0; // 0: Waiting for offer, 1: Waiting for READY, 2: Playing
    private int currentTurn = 1;
    private String lastMsg = "";
    private Scanner scanner = new Scanner(System.in);

    protected void setup() {
        System.out.println("Codebreaker-Agent " + getAID().getName() + " siap.");
        
        // Add the main cyclic behavior to handle all communication
        addBehaviour(new CodebreakerBehaviour(this));
    }

    class CodebreakerBehaviour extends CyclicBehaviour {
        
        public CodebreakerBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            ACLMessage msg = receive();
            
            if (msg != null) {
                String content = msg.getContent();
                
                // Prevent processing the exact same message twice (crucial because of Codemaker's TickerBehaviour)
                if (content.equals(lastMsg)) {
                    return; 
                }
                lastMsg = content;

                // STEP 0: Receive the invitation from Codemaker
                if (step == 0 && content.equals("Let's play Mastermind!")) {
                    System.out.println("\nCodemaker -> Codebreaker: " + content);
                    System.out.println("Menerima tawaran. Mengirim konfirmasi...");
                    
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("siap");
                    send(reply);
                    
                    step = 1; // Move to waiting for READY
                } 
                // STEP 1: Codemaker says the code is ready
                else if (step == 1 && content.equals("READY")) {
                    System.out.println("Codemaker -> Codebreaker: " + content);
                    System.out.println("Kode rahasia sudah dibuat! Anda punya 10 kesempatan.");
                    step = 2; // Move to playing
                    askForGuess(msg);
                } 
                // STEP 2: Receive feedback for the previous guess
                else if (step == 2) {
                    System.out.println("Codemaker -> Codebreaker (Feedback): " + content);
                    
                    String[] feedback = content.split(",");
                    int merah = Integer.parseInt(feedback[0]);
                    int putih = Integer.parseInt(feedback[1]);
                    
                    System.out.println("Hasil Tebakan: " + merah + " Merah (posisi & warna benar), " + putih + " Putih (warna benar, posisi salah).");
                    
                    if (merah == 4) {
                        System.out.println("\n[SELESAI] SELAMAT! Anda berhasil menebak kode rahasia dalam " + (currentTurn - 1) + " tebakan.");
                        myAgent.doDelete();
                    } else if (currentTurn > 10) {
                        System.out.println("\n[SELESAI] KESEMPATAN HABIS! Anda kalah.");
                        myAgent.doDelete();
                    } else {
                        askForGuess(msg);
                    }
                }
            } else {
                block();
            }
        }

        private void askForGuess(ACLMessage previousMsg) {
            System.out.println("\n--- Giliran Menebak ke-" + currentTurn + " ---");
            System.out.println("Masukkan 4 warna (pisahkan dengan koma).");
            System.out.println("Pilihan: Merah, Hijau, Biru, Kuning, Hitam, Putih");
            System.out.print("Tebakan Anda: ");
            
            String guess = scanner.nextLine();
            
            // Basic validation (just to ensure 4 items are entered)
            String[] guessArray = guess.split(",");
            if (guessArray.length != 4) {
                System.out.println("Format salah! Pastikan Anda memasukkan 4 warna yang dipisahkan koma.");
                askForGuess(previousMsg); // Retry
                return;
            }

            // Send guess to Codemaker
            ACLMessage reply = previousMsg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(guess);
            send(reply);
            
            System.out.println("Codebreaker -> Codemaker: " + guess);
            currentTurn++;
        }
    }
}