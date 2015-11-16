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
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author PauloCardoso
 */
public class Coordenador extends Agent {
    
     
    // é preciso adicionar serviços aqui como nos sensores?
     @Override
    protected void setup(){
        super.setup();
        System.out.println("Agente "+this.getLocalName()+" a iniciar...");      
        this.addBehaviour(new ReceiveBehaviour());
    }
    
    @Override
    protected void takeDown(){
        super.takeDown();
         try { DFService.deregister(this); }
         catch (Exception e) {e.printStackTrace();}
		 
        System.out.println(this.getLocalName()+" a morrer...");
    }
    
    
    
    private class ReceiveBehaviour extends CyclicBehaviour {
        @Override
        public void action(){
           ACLMessage msg=receive();
           if (msg != null)
           { 
               if(msg.getPerformative() == ACLMessage.REQUEST){
                                    
                   ACLMessage resp = new ACLMessage(ACLMessage.REQUEST);                   
                   resp.setConversationId(""+System.currentTimeMillis());
                   switch (msg.getContent()) {
                       case "online":
                           {
                             AID [] buyers = searchDF("sensor");
                                for (AID buyer : buyers) {
                                    resp.setContent("online");
                                    resp.addReceiver(buyer);
                                    myAgent.send(resp);
                                }
                                break;
                           }
                       case "offline":
                       {
                           AID [] buyers = searchDF("sensor");
                                for (AID buyer : buyers) {
                                    resp.setContent("offline");
                                    resp.addReceiver(buyer);
                                    myAgent.send(resp);
                                }
                                break;
                           }
                       case "value":
                       {
                           //perguntar a todos os sensores qual a temperatura
                           AID [] buyers = searchDF("sensor");
                                for (AID buyer : buyers) {
                                    resp.setContent("value");
                                    resp.addReceiver(buyer);
                                    myAgent.send(resp);
                                }
                               break;
                        }
                   }
               }
               else if(msg.getPerformative() == ACLMessage.INFORM){
                   
                   AID receiver = new AID();
                   receiver.setLocalName("interface");
                   
                   ACLMessage resp = new ACLMessage(ACLMessage.INFORM);
                   resp.setConversationId(msg.getConversationId());
                   resp.setContent(msg.getSender().getLocalName() + " " + msg.getContent());
                   resp.addReceiver(receiver);
                   myAgent.send(resp);
               }
               else
            	{       
                        ACLMessage reply = msg.createReply();
            		reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            		myAgent.send(reply);
            	}
            }
           block();    
        }
    }
    
    
	AID [] searchDF( String service )
//  ---------------------------------
	{
		DFAgentDescription dfd = new DFAgentDescription();
   		ServiceDescription sd = new ServiceDescription();
   		sd.setType( service );
		dfd.addServices(sd);
		
		SearchConstraints ALL = new SearchConstraints();
		ALL.setMaxResults(new Long(-1));

		try
		{
			DFAgentDescription[] result = DFService.search(this, dfd, ALL);
			AID[] agents = new AID[result.length];
			for (int i=0; i<result.length; i++) 
				agents[i] = result[i].getName() ;
			return agents;

		}
        catch (FIPAException fe) { fe.printStackTrace(); }
        
      	return null;
	}
}


    
    
//java -cp Desktop/jade/lib/jade.jar jade.Boot -gui

