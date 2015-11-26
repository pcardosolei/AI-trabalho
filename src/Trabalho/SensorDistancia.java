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
import java.util.Random;

/**
 *
 * @author PauloCardoso
 */
public class SensorDistancia extends Agent {
    private static final long serialVersionUID = 1L;
	private boolean sensorState = true; //depois meter a falso;
	private boolean finished = false;
        private int distancia;
	private boolean atravar = false;
        
	@Override
	protected void takeDown() {
		super.takeDown();
		
		 try { DFService.deregister(this); }
         catch (Exception e) {e.printStackTrace();}
		 
		 System.out.println("A remover registo de serviços...");
	}
	
	@Override
	protected void setup() {
		super.setup();
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("distancia");
		dfd.addServices(sd);
		distancia = Math.abs(new Random().nextInt() % 100);
            				
		try{ DFService.register(this, dfd );}
                    catch (FIPAException fe) { fe.printStackTrace(); }
		
		System.out.println("Agente "+this.getLocalName()+" a iniciar...");
		ParallelBehaviour parallel = new ParallelBehaviour(this,WHEN_ALL);
                parallel.addSubBehaviour(new ReceiveBehaviour());
                parallel.addSubBehaviour(new CalculaDistancia(this,1000));
        
                this.addBehaviour(parallel);
        
	}
	
	public boolean isSensorState() {
		return sensorState;
	}

	public void setSensorState(boolean sensorState) {
		this.sensorState = sensorState;
	}
        
        public boolean isTravar(){
            return atravar;
        }
        
        public void setTravar(boolean atravar){
            this.atravar = atravar;
        }
        
	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
        
      private class CalculaDistancia extends TickerBehaviour
      {
      
          public CalculaDistancia(Agent a, long timeout)
        {   
            super(a,timeout);
         }
        
        protected void onTick()
        {   
            if(!atravar){
            distancia += new Random().nextInt(16) - 8;
            } else if(atravar){
                distancia +=  Math.abs(new Random().nextInt(10) + 5);
            }
            
            if(distancia < 0){
                distancia = 0;  //bateu de frente 
            }
            
           
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setConversationId(""+System.currentTimeMillis());
            msg.setContent("distancia "+distancia);
            AID [] distancia = searchDF("coordenadortravao");
            for(AID sensor : distancia){
                msg.addReceiver(sensor);
                }
            myAgent.send(msg);
            
                   
        }
    }

	private class ReceiveBehaviour extends CyclicBehaviour
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void action() 
		{
		    ACLMessage msg = receive();
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
                                }

                                if (msg.getContent().equals("offline"))
                                {
                                        if (isSensorState())
                                        {
                                                System.out.println("Sensor "+myAgent.getLocalName()+" está agora offline.");
                                                reply.setPerformative(ACLMessage.CONFIRM);
                                                myAgent.send(reply);
                                                setSensorState(false);
                                        }
                                        else
                                        {
                                                reply.setPerformative(ACLMessage.FAILURE);
                                                myAgent.send(reply);
                                        }

                                }
                                if (msg.getContent().equals("value"))
                                {
                                    if (isSensorState())
                                    {
            					reply.setContent("distancia "+distancia);
            					reply.setPerformative(ACLMessage.INFORM);
            					myAgent.send(reply);
                                	}   
                                }
                                if(msg.getContent().equals("travar"))
                                {
                                    if(isSensorState()) //ver se ja esta a travar
                                    {
                                        setTravar(true);
                                        System.out.println("Sensor "+myAgent.getLocalName()+" a travar");
                                        reply.setPerformative(ACLMessage.INFORM);
                                        myAgent.send(reply);
                                        
                                    }
                                    else
                                    {
                                       reply.setPerformative(ACLMessage.FAILURE);
                                       myAgent.send(reply); 
                                    }
                                }
                                if(msg.getContent().equals("descansar"))
                                {
                                    if(isSensorState()){
                                        setTravar(false);
                                        System.out.println("Sensor "+myAgent.getLocalName()+" parou de travar");
                                        reply.setPerformative(ACLMessage.INFORM);
                                        myAgent.send(reply);
                                        
                                    }
                                    else
                                    {
                                       reply.setPerformative(ACLMessage.FAILURE);
                                       myAgent.send(reply); 
                                    }
                                }
                        else
                        {
                                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                                myAgent.send(reply);
                        }
                    }

                    if (isFinished())
                        myAgent.doDelete();
                    block();
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
