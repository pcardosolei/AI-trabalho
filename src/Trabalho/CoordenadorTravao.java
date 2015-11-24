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
public class CoordenadorTravao extends Agent {
    
    private static final long serialVersionUID = 1L;
    private boolean sensorState = false;
    private boolean finished = false;     
    private static int distancia = 1; //considero apenas 1 distancia
    private static int velocidade = 1; //considero apenas 1 velocidade
    private static boolean atravar = false;
    
    @Override
    protected void setup(){
        super.setup();
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("coordenadortravao");
		dfd.addServices(sd);
            				
		try{ DFService.register(this, dfd );}
                    catch (FIPAException fe) { fe.printStackTrace(); }
		
		System.out.println("Agente "+this.getLocalName()+" a iniciar...");
		ParallelBehaviour parallel = new ParallelBehaviour(this,WHEN_ALL);
                parallel.addSubBehaviour(new ReceiveBehaviour());
                parallel.addSubBehaviour(new VerificaSeguranca(this,1000));
        
                this.addBehaviour(parallel);
    }
    
    @Override
    protected void takeDown(){
      	super.takeDown();
		
		 try { DFService.deregister(this); }
         catch (Exception e) {e.printStackTrace();}
		 
		 System.out.println("A remover registo de serviços...");
	 }
    
	public boolean isSensorState() {
		return sensorState;
	}

	public void setSensorState(boolean sensorState) {
		this.sensorState = sensorState;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}    
    
    private class ReceiveBehaviour extends CyclicBehaviour {
        
        @Override
        public void action() {
         ACLMessage msg=receive();
           if (msg != null)
           { 
               ACLMessage reply = msg.createReply();  
             if (msg.getPerformative() == ACLMessage.REQUEST)
             {
               
                
               if (msg.getContent().equals("shutdown"))
               {
                       System.out.println("Sensor "+myAgent.getLocalName()+" a terminar...");
                       setFinished(true);
                }
                if (msg.getContent().equals("online"))
                {
                    if (isSensorState())
                    {
                        reply.setPerformative(ACLMessage.FAILURE);
                        myAgent.send(reply);
                        }
                        else
                        {
                            System.out.println("Sensor "+myAgent.getLocalName()+" está agora online.");
                            reply.setPerformative(ACLMessage.CONFIRM);
                            myAgent.send(reply);
                            setSensorState(true);
                        }
               } else{
                
               String[] partes = msg.getContent().split(" ");
               switch(partes[0]){
                   case "distancia":
                       try{
                       distancia = Integer.parseInt(partes[1]);
                       }catch(Exception e){
                           
                       }
                       break;
                   case "velocidade":
                       try{
                       velocidade = Integer.parseInt(partes[1]);
                       break;
                       }catch(Exception e){
                           
                       }
                   case "online":
                       
                   default: 
                       break;            
                }
               
               reply.setPerformative(ACLMessage.CONFIRM);
               myAgent.send(reply);
                }
           } 
           else
            {                 
            reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            myAgent.send(reply);
            }
           
            if (isFinished())
            myAgent.doDelete(); 
           block();    
        }    
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
           if(!atravar){
            
            if(velocidade*1.1/distancia <0){
                 AID receiver = new AID();

                 ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                 msg.setContent("travar");
                 AID [] travoes = searchDF("travao");
                 for (AID travao : travoes) {
                    msg.addReceiver(travao);
                 }
                 myAgent.send(msg); 
                 atravar = true;
            }
           } else if(atravar){
              if(velocidade*1.1/distancia > 1){
                  AID receiver = new AID();
                  
                  ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                  msg.setContent("descansar");
                  AID [] travoes = searchDF("travao");
                  for(AID travao: travoes){
                      msg.addReceiver(travao);
                      
                  }
                  myAgent.send(msg);
                  atravar= false;
              } 
              
           }
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
