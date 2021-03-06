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
    private int velocidade = 0;
    private final Random r = new Random();
    //dados do ambiente
    private boolean atravar = false;
    private boolean acelerar = false;
    private int gear;

    @Override
    protected void takeDown() {
        super.takeDown();

        try {
            DFService.deregister(this);
        } catch (Exception e) {}
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
        sd.setType("velocidade");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {}

        System.out.println("Agente " + this.getLocalName() + " a iniciar...");
        ParallelBehaviour parallel = new ParallelBehaviour(this, WHEN_ALL);
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new CalculaVelocidade(this, 1000));

        this.addBehaviour(parallel);

    }

    public boolean isSensorState() {
        return sensorState;
    }

    public void setSensorState(boolean sensorState) {
        this.sensorState = sensorState;
    }

    public void setTravar() {
        this.atravar = true;
        this.acelerar = false;
    }

    public void setAcelerar() {
        this.atravar = false;
        this.acelerar = true;
    }
    
    public void setManter() {
        this.atravar = false;
        this.acelerar = false;
    }

    public void gearUp() {
        if (gear < 5) {
            this.gear++;
        }
    }

    public void gearDown() {
        if (gear > 1) {
            this.gear--;
        }
    }

    public void setVelocidade(int v) {
        if (v <= 0) {
            velocidade = 0;
        } else if (v >= 240) {
            velocidade = 240;
        } else {
            velocidade = v;
        }
    }

    public int generateValue() {
        int random = r.nextInt(100);
        if (random <= 5) { //valor errado
            return -velocidade;
        } else if (random <= 15) { //valor desviado para baixo
            return (int) (velocidade - (velocidade * (r.nextFloat() * 0.1f)));
        } else if (random <= 25) { //valor desviado para cima
            return (int) (velocidade + (velocidade * (r.nextFloat() * 0.1f)));
        } else { //valor real
            return velocidade;
        }
    }

    private class CalculaVelocidade extends TickerBehaviour {

        public CalculaVelocidade(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        protected void onTick() {
            if (acelerar) {
                setVelocidade(velocidade + 4 + gear * 2);
            } else if (atravar) {
                setVelocidade(velocidade - 5 - gear * 3);
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
                                reply.setContent("velocidade " + generateValue());
                                reply.setPerformative(ACLMessage.INFORM);
                            }
                            break;
                    }
                    myAgent.send(reply);
                } else if (msg.getPerformative() == ACLMessage.INFORM) {
                    switch (msg.getContent()) {
                        case "travar":
                            if (isSensorState()) {
                                setTravar();
                            }
                            break;
                        case "acelerar":
                            if (isSensorState()) {
                                setAcelerar();
                            }
                            break;
                        case "manter":
                            if (isSensorState()) {
                                setManter();
                            }
                            break;
                        case "gearup":
                            if (isSensorState()) {
                                gearUp();
                            }
                            break;
                        case "geardown":
                            if (isSensorState()) {
                                gearDown();
                            }
                            break;
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

        } catch (FIPAException fe) {}

        return null;
    }
}
