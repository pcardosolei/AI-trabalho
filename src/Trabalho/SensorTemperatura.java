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
public class SensorTemperatura extends Agent {

    private static final long serialVersionUID = 1L;
    private boolean sensorState = false; //depois meter a falso;
    private final Random r = new Random();
    private int temperatura = 80;
    //dados do ambiente
    private float rotacoes;

    @Override
    protected void takeDown() {
        super.takeDown();

        try {
            DFService.deregister(this);
        } catch (Exception e) {
        }
        this.doDelete();
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

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
        }

        System.out.println("Agente " + this.getLocalName() + " a iniciar...");
        ParallelBehaviour parallel = new ParallelBehaviour(this, WHEN_ALL);
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new CalculaTemperatura(this, 1000));

        this.addBehaviour(parallel);

    }

    public boolean isSensorState() {
        return sensorState;
    }

    public void setSensorState(boolean sensorState) {
        this.sensorState = sensorState;
    }

    public void setTemperatura(int t) {
        if (t <= 40) {
            temperatura = 40;
        } else if (t >= 120) {
            temperatura = 120;
        } else {
            temperatura = t;
        }
    }

    public void setRotacoes(float t) {
        if (t <= 0) {
            rotacoes = 0;
        } else if (t >= 6) {
            rotacoes = 6;
        } else {
            rotacoes = t;
        }
    }

    public int generateValue() {
        int random = r.nextInt(100);
        if (random <= 5) { //valor errado
            return -temperatura;
        } else if (random <= 15) { //valor desviado para baixo
            return (int) (temperatura - temperatura * (r.nextFloat() * 0.05f));
        } else if (random <= 25) { //valor desviado para cima
            return (int) (temperatura + temperatura * (r.nextFloat() * 0.05f));
        } else { //valor real
            return temperatura;
        }
    }

    private class CalculaTemperatura extends TickerBehaviour {

        public CalculaTemperatura(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        protected void onTick() {
            if (rotacoes < 1) {
                setTemperatura(temperatura - 1);
            } else if (rotacoes > 2) {
                setTemperatura(temperatura + 1);
            } else if (rotacoes > 5) {
                setTemperatura(temperatura + 3);
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
                            }
                            break;
                        case "value":
                            if (isSensorState()) {
                                reply.setContent("temperatura " + generateValue());
                                reply.setPerformative(ACLMessage.INFORM);
                            }
                            break;

                    }
                    myAgent.send(reply);
                } else if (msg.getPerformative() == ACLMessage.INFORM) {
                    String[] message = msg.getContent().split(" ");
                    if ("rotacoes".equals(message[0])) {
                        if (isSensorState()) {
                            float c = Float.parseFloat(message[1]);
                            if (c >= 0) {
                                setRotacoes(c);
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
