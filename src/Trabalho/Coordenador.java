/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Trabalho;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author PauloCardoso
 */
public class Coordenador extends Agent {
    
     
     @Override
    protected void setup(){
        super.setup();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( getAID() );     
        
        try {  
            DFService.register( this, dfd );  
        }
        catch (FIPAException fe) {
            fe.printStackTrace(); 
        }
        this.addBehaviour(new ReceiveBehaviour());
    }
    
    @Override
    protected void takeDown(){
        super.takeDown();
        System.out.println(this.getLocalName()+" a morrer...");
    }
    
    
    
    private class ReceiveBehaviour extends CyclicBehaviour {
        public void action(){
            ACLMessage msg=receive();
           if (msg != null)
           { 
               if(msg.getPerformative() == 16){
                    ACLMessage resp = msg.createReply();                   
                    resp.setConversationId(msg.getConversationId());      
                    //perguntar a todos os sensores qual a temperatura
                    
                    DFAgentDescription dfd = new DFAgentDescription();
                    ServiceDescription sd  = new ServiceDescription();
                    sd.setType( "sensor" );
                    dfd.addServices(sd);

                    DFAgentDescription[] result = DFService.search(this, dfd);

                    System.out.println(result.length + " results" );
                    if (result.length>0)
                        System.out.println(" " + result[0].getName() );

                    myAgent.send(resp);
               }
            }
           block();
                    
         
        }
    }  
}


    
    
//java -cp Desktop/jade/lib/jade.jar jade.Boot -gui

