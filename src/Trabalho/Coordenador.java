/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Trabalho;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import static jade.core.behaviours.ParallelBehaviour.WHEN_ALL;
import jade.core.behaviours.TickerBehaviour;
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
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("coordenador");
		dfd.addServices(sd);
            				
		try{ DFService.register(this, dfd );}
                    catch (FIPAException fe) { fe.printStackTrace(); }
		
		System.out.println("Agente "+this.getLocalName()+" a iniciar...");
		ParallelBehaviour parallel = new ParallelBehaviour(this,WHEN_ALL);
                parallel.addSubBehaviour(new ReceiveBehaviour());     
                this.addBehaviour(parallel);
    }
    
    @Override
    protected void takeDown(){
      	super.takeDown();
		
		 try { DFService.deregister(this); }
         catch (Exception e) {e.printStackTrace();}
		 
		 System.out.println("A remover registo de serviços...");
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
                             resp.setContent("online");
                             AID [] buyers = searchDF("sensor");
                                for (AID buyer : buyers) {                                  
                                    resp.addReceiver(buyer);                                    
                                }
                                myAgent.send(resp);
                                break;
                           }
                       case "offline":
                       {   
                           resp.setContent("offline");
                           AID [] buyers = searchDF("sensor");
                                for (AID buyer : buyers) {
                                    resp.addReceiver(buyer);                                   
                                }
                                myAgent.send(resp);
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
    
     private class VerificaSeguranca extends TickerBehaviour
      {
      
          public VerificaSeguranca(Agent a, long timeout)
        {   
            super(a,timeout);
         }
        
        protected void onTick()
        {   
            AID receiver = new AID();
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);            
            msg.setConversationId(""+System.currentTimeMillis());      
            msg.setContent("value");          
            
            AID [] distancias = searchDF("distancia");
            for (AID distancia : distancias) {
                msg.addReceiver(distancia);
                }
                myAgent.send(msg); 
            
            
            
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

