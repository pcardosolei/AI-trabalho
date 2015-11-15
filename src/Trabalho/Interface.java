/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Trabalho;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author PauloCardoso e Miguel Ribeiro
 */
public class Interface extends Agent {

    @Override
    protected void setup(){
        super.setup();
        System.out.println(this.getLocalName()+" a começar!");
        this.addBehaviour(new ReceiveBehaviour());
        this.addBehaviour(new NewSendMessage(this,50000));
    }
    
    @Override
    protected void takeDown(){
        super.takeDown();
        System.out.println(this.getLocalName()+" a morrer...");
    }
    
 
     private class NewSendMessage extends TickerBehaviour
    {
        String content;
        public NewSendMessage(Agent a, long timeout)
        {   
            super(a,timeout);
         }
        
        protected void onTick()
        {
            
            AID receiver = new AID();
            receiver.setLocalName("coordenador");
            
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setConversationId(""+System.currentTimeMillis());
            msg.addReceiver(receiver);
            msg.setContent("Qual é a temperatura da casa?");          
            myAgent.send(msg);
        }
    }
    
    
    
    private class ReceiveBehaviour extends CyclicBehaviour{
        @Override
        public void action(){
            ACLMessage msg=receive();
            if(msg!=null){
                System.out.println("Recebi uma mensagem de "+msg.getSender()+". Conteúdo: "+msg.getContent());
            }
        }
    }
}
