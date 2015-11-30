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
 * @author Portatilcar
 */
public class SensorTemperatura extends Agent{
        private static final long serialVersionUID = 1L;
	private boolean sensorState = false; //depois meter a falso;
	private boolean finished = false;
        private float temperatura;
        
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
		sd.setType("temperatura");
		dfd.addServices(sd);
		temperatura = Math.abs(new Random().nextFloat() % 20);
            				
		try{ DFService.register(this, dfd );}
                    catch (FIPAException fe) { fe.printStackTrace(); }
		
		System.out.println("Agente "+this.getLocalName()+" a iniciar...");
		ParallelBehaviour parallel = new ParallelBehaviour(this,WHEN_ALL);
                parallel.addSubBehaviour(new ReceiveBehaviour());
                parallel.addSubBehaviour(new CalculaCombustivel(this,1000));
        
                this.addBehaviour(parallel);
        
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
        
      private class CalculaCombustivel extends TickerBehaviour
      {
      
          public CalculaCombustivel(Agent a, long timeout)
        {   
            super(a,timeout);
         }
        
        protected void onTick()
        {     
          temperatura -= 0.01;          
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
            					reply.setContent("combustivel "+temperatura);
            					reply.setPerformative(ACLMessage.INFORM);
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
