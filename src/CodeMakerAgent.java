import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

public class CodemakerAgent extends Agent {
    private String[] secretCode = new String[4];
    private final String[] availableColors = {"Merah", "Hijau", "Biru", "Kuning", "Hitam", "Putih"};
    private int currentRow = 0;
    private int step = 0;

    protected void setup() {
        System.out.println("Codemaker-Agent " + getAID().getName() + " siap.");

        generateSecretCode();
        
        // Menggunakan TickerBehaviour agar Codemaker terus menawarkan permainan 
        addBehaviour(new TickerBehaviour(this, 2000) {
            protected void onTick() {
                if (step == 0) {
                    ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
                    msgOut.setContent("Let's play Mastermind!");
                    msgOut.addReceiver(new AID("codebreaker", AID.ISLOCALNAME));
                    System.out.println("Codemaker -> Codebreaker: " + msgOut.getContent());
                    send(msgOut);
                }
            }
        });

        // Behavior untuk menerima jawaban dan mengevaluasi tebakan
        addBehaviour(new evaluatingBehaviour(this));
    }

    class evaluatingBehaviour extends CyclicBehaviour {
        public evaluatingBehaviour(Agent a) { super(a); }

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String content = msg.getContent();

                if (step == 0 && content.contains("siap")) { //CEK KONFIRMASI DARI CODEBREAKER bisa diubah nanti
                    System.out.println("Codebreaker menerima tawaran! Tebak KOdenya!");
                    step = 1; // Masuk ke tahap permainan utama
                    
                    // Beritahu Codebreaker bahwa kode sudah siap
                    ACLMessage readyMsg = msg.createReply();
                    readyMsg.setPerformative(ACLMessage.INFORM);
                    readyMsg.setContent("READY");
                    send(readyMsg);
                } else if (step == 1) {
                    currentRow++;
                    System.out.println("\n--- Turn Evaluasi ke-" + currentRow + " ---");
                    System.out.println("Tebakan Codebreaker: " + content);

                    String[] guess = content.split(",");
                    int[] result = evaluateGuess(secretCode, guess);
                    int merah = result[0];
                    int putih = result[1];

                    System.out.println("Hasil: Merah: " + merah + ", Putih: " + putih);

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(merah + "," + putih);
                    send(reply);

                    if (merah == 4) {
                        System.out.println("[SELESAI] Codebreaker BERHASIL menebak seluruh kode!");
                        printSecretCode();
                        myAgent.doDelete();
                    } else if (currentRow >= 10) {
                        System.out.println("[SELESAI] Kesempatan habis (10 turn). Codebreaker KALAH.");
                        printSecretCode();
                        myAgent.doDelete();
                    }
                }
            } else {
                block();
            }
        }
    }

    private void generateSecretCode() {
        for (int i = 0; i < 4; i++) {
            int index = (int) (Math.random() * availableColors.length);
            secretCode[i] = availableColors[index];
        }
        System.out.print("[DEBUG] Kode rahasia komputer: ");
        for (String c : secretCode) { System.out.print(c + " "); }
        System.out.println();
    }

    private int[] evaluateGuess(String[] secret, String[] guess) {
        int merah = 0, putih = 0;
        boolean[] secretChecked = new boolean[4];
        boolean[] guessChecked = new boolean[4];
        
        for (int i = 0; i < 4; i++) {
            if (guess[i].equals(secret[i])) {
                merah++;
                secretChecked[i] = true;
                guessChecked[i] = true;
            }
        }
        
        for (int i = 0; i < 4; i++) {
            if (!guessChecked[i]) {
                for (int j = 0; j < 4; j++) {
                    if (!secretChecked[j] && guess[i].equals(secret[j])) {
                        putih++;
                        secretChecked[j] = true;
                        break;
                    }
                }
            }
        }
        return new int[]{merah, putih};
    }

    private void printSecretCode() {
        System.out.print("Kode rahasia yang benar adalah: ");
        for (String c : secretCode) { System.out.print(c + " "); }
        System.out.println();
    }
}