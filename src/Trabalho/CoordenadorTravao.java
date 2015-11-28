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
import java.util.ArrayList;

/**
 *
 * @author PauloCardoso
 */
public class CoordenadorTravao extends Agent {
    
    private static final long serialVersionUID = 1L;
    private boolean sensorState = true; //depois meter a falso
    private boolean finished = false;     
    private static int distancia = 1; //distancia naquele segundo
    private static int velocidade = 1; //velocidade instantanea
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
    
        //falta o offline
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
           float criterio = (float)distancia/velocidade;
           if(!atravar){
            if(criterio < 1.5){
                 
                 ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                 msg.setContent("travar");
                 //pesquisa de todos os agents para travar
                 ArrayList<AID> agents = searchDFtypes("travao velocidade distancia"); 
                 for(AID sensor: agents){
                      msg.addReceiver(sensor);
                 }
                 myAgent.send(msg); 
                 atravar = true;
            }
           } else if(atravar){
              if(criterio > 2){
                  
                  ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                  msg.setContent("descansar");
                  ArrayList<AID> agents = searchDFtypes("travao velocidade distancia");   
                  for(AID sensor: agents){
                      msg.addReceiver(sensor);
                 }
                  myAgent.send(msg);
                  atravar= false;
              } 
              
           }
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
