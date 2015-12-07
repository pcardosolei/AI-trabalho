/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Trabalho;

import gui.Crash;
import gui.mainWindow;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import static jade.core.behaviours.ParallelBehaviour.WHEN_ALL;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PauloCardoso e Miguel Ribeiro
 */
public class Interface extends Agent {

    private mainWindow janela;

    //Car state
    private boolean ligado;
    private int velocidade = 0;
    private float rotacoes = 0.0f;
    private int distancia = 30;
    private int temperatura = 80;
    private float combustivel = 1;
    private int gear = 1;
    int score = 0;
    boolean ended = false;
    boolean crashed = false;
    boolean breaking = false;
    boolean accelerating = false;

    @Override
    protected void setup() {

        super.setup();
        try {
            janela = new mainWindow(this, combustivel, distancia, rotacoes, velocidade, temperatura, gear);
        } catch (IOException ex) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
        }
        getJanela().setVisible(true);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("interface");
        dfd.addServices(sd);
        ParallelBehaviour parallel = new ParallelBehaviour(this, WHEN_ALL);
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new UpdateValues(this, 500));

        this.addBehaviour(parallel);
        System.out.println("Agente " + this.getLocalName() + " a iniciar...");

    }

    @Override
    protected void takeDown() {
        super.takeDown();
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        this.doDelete();
        System.out.println(this.getLocalName() + " a morrer...");
    }

    public void start() {
        ligado = true;
        sendMessage("online", ACLMessage.REQUEST);
    }

    public void shutdown() {
        sendMessage("shutdown", ACLMessage.REQUEST);
    }

    public mainWindow getJanela() {
        return janela;
    }

    public boolean isLigado() {
        return ligado;
    }

    public void setBreaking() {
        this.breaking = true;
        this.accelerating = false;
        sendMessage("travar", ACLMessage.INFORM);
    }

    public void setAccelerating() {
        this.accelerating = true;
        this.breaking = false;
        sendMessage("acelerar", ACLMessage.INFORM);
    }

    public void setMaintain() {
        this.accelerating = false;
        this.breaking = false;
        sendMessage("manter", ACLMessage.INFORM);
    }

    public int getVelocidade() {
        return velocidade;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public float getRotacoes() {
        return rotacoes;
    }

    public int getDistancia() {
        return distancia;
    }

    public int getTemperatura() {
        return temperatura;
    }

    public float getCombustivel() {
        return combustivel;
    }

    public int getGear() {
        return gear;
    }

    void setVelocidade(int d) {
        if (d <= 0) {
            velocidade = 0;
        } else if (d >= 240) {
            velocidade = 240;
        } else {
            velocidade = d;
        }
    }

    void setRotacoes(float d) {
        if (d <= 0) {
            rotacoes = 0;
        } else if (d >= 6) {
            rotacoes = 6;
            crashed = true;
        } else {
            rotacoes = d;
        }
    }

    void setCombustivel(float d) {
        if (d <= 0) {
            crashed = true;
            combustivel = 0;
        } else if (d >= 1) {
            combustivel = 1;
        } else {
            combustivel = d;
        }
    }

    void setTemperatura(int d) {
        if (d >= 120) {
            crashed = true;
            temperatura = 120;
        } else if (d <= 40) {
            temperatura = 40;
        } else {
            temperatura = d;
        }
    }

    void setDistancia(int d) {
        if (d <= 0) {
            crashed = true;
            distancia = 0;
        } else {
            distancia = d;
        }
    }

    public void increaseGear() {
        if (rotacoes < 1) {
            if (gear == 1) {
                gear++;
                sendMessage("gearup", ACLMessage.INFORM);
            }
        } else if (gear >= 5) {
            gear = 5;
        } else {
            gear++;
            sendMessage("gearup", ACLMessage.INFORM);
        }
    }

    public void lowerGear() {
        if (rotacoes > 5) {
            sendMessage("geardown", ACLMessage.INFORM);
            gear = 1;
            crashed = true;
        } else if (gear <= 1) {
            gear = 1;
        } else {
            gear--;
            sendMessage("geardown", ACLMessage.INFORM);
        }
    }

    public void sendMessage(String c, int type) {
        if (ligado) {
            AID receiver = new AID();
            receiver.setLocalName("coordenador");

            ACLMessage msg = new ACLMessage(type);
            msg.setConversationId("" + System.currentTimeMillis());
            msg.addReceiver(receiver);
            msg.setContent(c);
            this.send(msg);
        }
    }

    public void updateScore(){
        if (distancia <= 10) {
            score += 10;
        } else if (distancia <= 20) {
            score += 8;
        } else if (distancia <= 40) {
            score += 6;
        } else if (distancia <= 70) {
            score += 4;
        } else if (distancia <= 100) {
            score += 3;
        } else if (distancia <= 150) {
            score += 2;
        } else {
            score += 1;
        }
    }
    
    private class UpdateValues extends TickerBehaviour {

        AID receiver;

        public UpdateValues(Agent a, long timeout) {
            super(a, timeout);
            receiver = new AID();
            receiver.setLocalName("coordenador");
        }

        @Override
        protected void onTick() {
            if (ligado) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setConversationId("" + System.currentTimeMillis());
                msg.addReceiver(receiver);
                msg.setContent("value");
                myAgent.send(msg);
                updateScore();
                if (!crashed) {
                    janela.updateCar(combustivel, distancia, rotacoes, velocidade, temperatura, gear);
                } else {
                    if (!ended) {
                        janela.dispose();
                        Crash c = new Crash(score);
                        ended = true;
                    }
                    sendMessage("shutdown", ACLMessage.REQUEST);
                    shutdown();
                }
            }
        }
    }

    private class ReceiveBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                String[] message = msg.getContent().split(" ");
                switch (message[0]) {
                    case "velocidade":
                        int v = Integer.parseInt(message[1]);
                        //System.out.println("velocidade: "+v);
                        if (v >= 0) {
                            setVelocidade(v);
                        }
                        break;
                    case "temperatura":
                        int t = Integer.parseInt(message[1]);
                        //System.out.println("temperatura: "+t);
                        if (t >= 0) {
                            setTemperatura(t);
                        }
                        break;
                    case "combustivel":
                        float c = Float.parseFloat(message[1]);
                        //System.out.println("combustivel: "+c);
                        if (c >= 0) {
                            setCombustivel(c);
                        }
                        break;
                    case "distancia":
                        int d = Integer.parseInt(message[1]);
                        //System.out.println("distancia: "+d);
                        if (d >= 0) {
                            setDistancia(d);
                        }
                        break;
                    case "rotacoes":
                        float r = Float.parseFloat(message[1]);
                        //System.out.println("rotacoes: "+r);
                        if (r >= 0) {
                            setRotacoes(r);
                        }
                        break;
                }
            }
            block();
        }
    }
}
