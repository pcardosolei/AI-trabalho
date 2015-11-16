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
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author PauloCardoso e Miguel Ribeiro
 */
public class Interface extends Agent {

    
    // é preciso adicionar serviços aqui como nos sensores?
    @Override
    protected void setup(){
        super.setup();
	System.out.println("Agente "+this.getLocalName()+" a iniciar...");
        this.addBehaviour(new ReceiveBehaviour());
        this.addBehaviour(new NewSendMessage(this,20000));
    }
    
    @Override
    protected void takeDown(){
        super.takeDown();
         try { DFService.deregister(this); }
         catch (Exception e) {e.printStackTrace();}
		 
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
                System.out.println("Conteúdo: "+msg.getContent());
            }
        }
    }
}
