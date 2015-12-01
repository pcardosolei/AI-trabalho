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
public class SensorVelocidade extends Agent {
        private static final long serialVersionUID = 1L; //Usamos isto para alguma coisa?
	private boolean sensorState = false; //depois meter a falso
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
		velocidade = Math.abs(new Random().nextInt() % 10) + 10;
            				
		try{ DFService.register(this, dfd );}
                    catch (FIPAException fe) { fe.printStackTrace(); }
		
		System.out.println("Agente "+this.getLocalName()+" a iniciar...");
		ParallelBehaviour parallel = new ParallelBehaviour(this,WHEN_ALL);
                parallel.addSubBehaviour(new ReceiveBehaviour());
                parallel.addSubBehaviour(new CalculaVelocidade(this,1000));
        
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
        
        public boolean isTravar(){
            return atravar;
        }
        
        public void setTravar(boolean atravar){
            this.atravar = atravar;
        }
        
      private class CalculaVelocidade extends TickerBehaviour
      {
          public CalculaVelocidade(Agent a, long timeout)
            {   
            super(a,timeout);
             }
        
            protected void onTick()
            {           
                if(atravar)
                    velocidade -= Math.abs(new Random().nextInt(15) + 5 );
                else if(!atravar)
                    velocidade += Math.abs(new Random().nextInt(16));
                if(velocidade < 0)
                    velocidade = 0;


                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setConversationId(""+System.currentTimeMillis());
                msg.setContent("velocidade "+velocidade);
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
                            switch(msg.getContent()){
                                case "shutdown":
                                    System.out.println("Sensor "+myAgent.getLocalName()+" a terminar...");
                                    setFinished(true);
                                    break;
                                case "online":                                 
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
                                    break;
                                case "offline":
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
                                    break;
                                case "value":
                                    if (isSensorState())
                                    {
                                            reply.setContent("velocidade "+velocidade);
                                            reply.setPerformative(ACLMessage.INFORM);
                                            myAgent.send(reply);
                                    }
                                    break;
                                case "travar":
                                    if(isSensorState()) 
                                    {
                                        setTravar(true);
                                        reply.setPerformative(ACLMessage.INFORM);
                                        myAgent.send(reply);
                                    }
                                    else
                                    {
                                       reply.setPerformative(ACLMessage.FAILURE);
                                       myAgent.send(reply); 
                                    }
                                    break;
                                case "descansar":
                                    if(isSensorState()){
                                        setTravar(false);
                                        reply.setPerformative(ACLMessage.INFORM);
                                        myAgent.send(reply); 
                                    }
                                    break;
                                }
                        }else
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


