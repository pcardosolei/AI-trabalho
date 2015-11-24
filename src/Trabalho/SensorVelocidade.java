/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Trabalho;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import static jade.core.behaviours.ParallelBehaviour.WHEN_ALL;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.Random;

/**
 *
 * @author PauloCardoso
 */
public class SensorVelocidade extends Agent {
     private static final long serialVersionUID = 1L;
	private boolean sensorState = false;
	private boolean finished = false;
        private int velocidade;
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
		sd.setType("velocidade");
		dfd.addServices(sd);
		velocidade = Math.abs(new Random().nextInt() % 100);
            				
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
            if(atravar)
                velocidade -= Math.abs(new Random().nextInt() % 10);
            else if(!atravar)
                velocidade += new Random().nextInt();
            if(velocidade < 0)
                velocidade = 0;
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
            					reply.setContent(""+velocidade);
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
}  


