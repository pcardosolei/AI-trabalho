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
public class SensorCombustivel extends Agent {

    private static final long serialVersionUID = 1L;
    private boolean sensorState = false;
    private float combustivel = 1;
    private final Random r = new Random();
    //dados do ambiente
    private int velocidade = 0;

    @Override
    protected void takeDown() {
        super.takeDown();

        try {
            DFService.deregister(this);
        } catch (Exception e) {
        }
        doDelete();
        System.out.println("A remover registo de serviços...");

    }

    @Override
    protected void setup() {
        super.setup();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("combustivel");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
        }

        System.out.println("Agente " + this.getLocalName() + " a iniciar...");
        ParallelBehaviour parallel = new ParallelBehaviour(this, WHEN_ALL);
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new CalculaCombustivel(this, 1000));

        this.addBehaviour(parallel);

    }

    public boolean isSensorState() {
        return sensorState;
    }

    public void setSensorState(boolean sensorState) {
        this.sensorState = sensorState;
    }

    public void setCombustivel(float c) {
        if (c <= 0) {
            combustivel = 0;
        } else if (c >= 1) {
            combustivel = 1;
        } else {
            combustivel = c;
        }
    }

    public void setVelocidade(int c) {
        if (c <= 0) {
            velocidade = 0;
        } else if (c >= 240) {
            velocidade = 240;
        } else {
            velocidade = c;
        }
    }

    public float generateValue() {
        int random = r.nextInt(100);
        if (random <= 5) { //valor errado
            return -combustivel;
        } else if (random <= 15) { //valor desviado para baixo
            return combustivel - combustivel * (r.nextFloat() * 0.1f);
        } else if (random <= 25) { //valor desviado para cima
            return combustivel + combustivel * (r.nextFloat() * 0.1f);
        } else { //valor real
            return combustivel;
        }
    }

    private class CalculaCombustivel extends TickerBehaviour {

        public CalculaCombustivel(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        protected void onTick() {
            if (velocidade <= 0) {

            } else if (velocidade <= 50) {
                setCombustivel(combustivel - 0.006f);
            } else if (velocidade <= 120) {
                setCombustivel(combustivel - 0.003f);
            } else {
                setCombustivel(combustivel - 0.005f);
            }
        }
    }

    private class ReceiveBehaviour extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    switch (msg.getContent()) {
                        case "shutdown":
                            System.out.println("Sensor " + myAgent.getLocalName() + " a terminar...");
                            takeDown();
                            break;
                        case "online":
                            if (isSensorState()) {
                                reply.setPerformative(ACLMessage.FAILURE);
                            } else {
                                System.out.println("Sensor " + myAgent.getLocalName() + " está agora online.");
                                reply.setPerformative(ACLMessage.CONFIRM);
                                setSensorState(true);
                            }
                            break;
                        case "offline":
                            if (isSensorState()) {
                                System.out.println("Sensor " + myAgent.getLocalName() + " está agora offline.");
                                reply.setPerformative(ACLMessage.CONFIRM);
                                setSensorState(false);
                            } else {
                                reply.setPerformative(ACLMessage.FAILURE);
                            }
                            break;
                        case "value":
                            if (isSensorState()) {
                                reply.setContent("combustivel " + generateValue());
                                reply.setPerformative(ACLMessage.INFORM);
                            }
                            break;
                    }
                    myAgent.send(reply);
                } else if (msg.getPerformative() == ACLMessage.INFORM) {
                    String[] message = msg.getContent().split(" ");
                    if ("velocidade".equals(message[0])) {
                        if (isSensorState()) {
                            int c = Integer.parseInt(message[1]);
                            if (c >= 0) {
                                setVelocidade(c);
                            }
                        }
                    }
                } else {
                    reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                    myAgent.send(reply);
                }
            }
            block();
        }
    }

    AID[] searchDF(String service) //  ---------------------------------
    {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
        dfd.addServices(sd);

        SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));

        try {
            DFAgentDescription[] result = DFService.search(this, dfd, ALL);
            AID[] agents = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                agents[i] = result[i].getName();
            }
            return agents;

        } catch (FIPAException fe) {
        }

        return null;
    }
}
