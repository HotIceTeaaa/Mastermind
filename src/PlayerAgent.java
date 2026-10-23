/*
javac -cp lib/jade.jar -d bin src/PlayerAgent.java 
*/

//biarin merah meski bukan dr src

import jade.core.Agent;
import jade.core.behaviours.*;

public class PlayerAgent extends Agent {
    protected void setup() {
        addBehaviour( new myBehaviour( this ) );
    }

    class myBehaviour extends SimpleBehaviour {
        private boolean finished = false;
        int iter = 0;
        
        public myBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            System.out.println(iter++);
            finished = iter == 100;
        }
        
        public boolean done() {
            return finished;
        }
    }
}