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
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

/**
 *
 * @author PauloCardoso
 */
public class CoordenadorInterface extends Agent {
    
     
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
                             ArrayList<AID> agents = searchDFtypes("coordenadortravao distancia travao velocidade");
                                for(AID sensor : agents){
                                    resp.addReceiver(sensor);
                                }           
                                 myAgent.send(resp);
                                break;
                           }
                       case "offline":
                       {   
                           resp.setContent("offline");
                           ArrayList<AID> agents = searchDFtypes("coordenadortravao distancia travao velocidade combustivel");
                                for(AID sensor : agents){
                                    resp.addReceiver(sensor);
                                }         
                                myAgent.send(resp);
                                break;
                           }    
                       case "shutdown":
                       {
                           resp.setContent("shutdown");
                             ArrayList<AID> agents = searchDFtypes("coordenadortravao distancia travao velocidade combustivel");
                                for(AID sensor : agents){
                                    resp.addReceiver(sensor);
                                }         
                                myAgent.send(resp);
                                break;         
                       }
                       case "value":
                           resp.setContent("value");
                           ArrayList<AID> agents = searchDFtypes("distancia velocidade combustivel");
                                for(AID sensor : agents){
                                    resp.addReceiver(sensor);
                                }         
                           myAgent.send(resp);
                           break;
                   }
               }
               else if(msg.getPerformative() == ACLMessage.INFORM){
                   
                   AID receiver = new AID();
                   receiver.setLocalName("interface");
                   
                   ACLMessage resp = new ACLMessage(ACLMessage.INFORM);
                   resp.setConversationId(msg.getConversationId());
                   resp.setContent(msg.getContent());
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
    
     /*
      SÓ UM TIPO DE SENSOR
     */
    
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
        /*
        ---- MAIS DO QUE UM TIPO DE SENSOR
        */
        
          ArrayList<AID> searchDFtypes ( String service ){
                DFAgentDescription dfd = new DFAgentDescription();
   		ServiceDescription sd = new ServiceDescription();
                String[] services = service.split(" ");
                try
		{
                ArrayList<AID> agents = new ArrayList<>();
                for(int j=0;j<services.length;j++){
                    sd.setType(services[j]);
                    dfd.addServices(sd);
                    SearchConstraints ALL = new SearchConstraints();
                    ALL.setMaxResults(new Long(-1));
		
                    DFAgentDescription[] result = DFService.search(this, dfd, ALL);
                    for (int i=0; i<result.length; i++) 
                        agents.add(result[i].getName());
                }
            	return agents;
            } catch (FIPAException fe) { fe.printStackTrace(); }  
            return null;
        }
}




