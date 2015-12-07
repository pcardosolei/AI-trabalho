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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PauloCardoso
 */
public class CoordenadorInterface extends Agent {

    Map<String, AID> agents = new HashMap();

    // é preciso adicionar serviços aqui como nos sensores?
    @Override
    protected void setup() {
        super.setup();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("coordenador");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {}
        while (agents.size() < 5) {
            ArrayList<AID> list = searchDFtypes("distancia velocidade temperatura combustivel rotacoes");
            list.stream().filter((sensor) -> (!agents.containsValue(sensor))).forEach((sensor) -> {
                agents.put(sensor.getLocalName(), sensor);
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}
        }
        System.out.println("Agente " + this.getLocalName() + " a iniciar...");
        System.out.println("Agente " + this.getLocalName() + " está ligado a:");
        agents.keySet().stream().forEach((s) -> {
            System.out.print(s + " ");
        });
        System.out.println(" ");
        ParallelBehaviour parallel = new ParallelBehaviour(this, WHEN_ALL);
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new ReceiveBehaviour());
        parallel.addSubBehaviour(new UpdateSensors(this, 30000));
        this.addBehaviour(parallel);
    }

    @Override
    protected void takeDown() {
        super.takeDown();

        try {
            DFService.deregister(this);
        } catch (Exception e) {}
        this.doDelete();
        System.out.println("A remover registo de serviços...");
    }

    private class UpdateSensors extends TickerBehaviour {

        String content;
        AID receiver;

        public UpdateSensors(Agent a, long timeout) {
            super(a, timeout);
            receiver = new AID();
            receiver.setLocalName("coordenador");
        }

        @Override
        protected void onTick() {
            agents.clear();
            while (agents.size() < 5) {
                ArrayList<AID> list = searchDFtypes("distancia velocidade temperatura combustivel rotacoes");
                list.stream().filter((sensor) -> (!agents.containsValue(sensor))).forEach((sensor) -> {
                    agents.put(sensor.getLocalName(), sensor);
                });
            }
        }
    }

    private class ReceiveBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.REQUEST) {

                    ACLMessage resp = new ACLMessage(ACLMessage.REQUEST);
                    resp.setConversationId("" + System.currentTimeMillis());
                    String[] message = msg.getContent().split(" ");
                    agents.values().stream().forEach((sensor) -> {
                        resp.addReceiver(sensor);
                    });
                    switch (message[0]) {
                        case "online":
                            resp.setContent("online");
                            break;
                        case "offline":
                            resp.setContent("offline");
                            break;
                        case "shutdown":
                            resp.setContent("shutdown");
                            takeDown();
                            break;
                        case "value":
                            resp.setContent("value");
                            break;
                    }
                    myAgent.send(resp);
                } else if (msg.getPerformative() == ACLMessage.INFORM) {
                    String[] message = msg.getContent().split(" ");
                    AID iface = new AID();
                    iface.setLocalName("interface");
                    ACLMessage resp = new ACLMessage(ACLMessage.INFORM);
                    resp.setContent(msg.getContent());
                    resp.setConversationId(msg.getConversationId());
                    switch (message[0]) {
                        case "distancia":
                            resp.addReceiver(iface);
                            break;
                        case "velocidade":
                            resp.addReceiver(iface);
                            resp.addReceiver(agents.get("combustivel"));
                            resp.addReceiver(agents.get("distancia"));
                            break;
                        case "rotacoes":
                            resp.addReceiver(iface);
                            resp.addReceiver(agents.get("temperatura"));
                            break;
                        case "combustivel":
                            resp.addReceiver(iface);
                            break;
                        case "temperatura":
                            resp.addReceiver(iface);
                            break;
                        case "travar":
                            //System.out.println("A travar");
                            resp.addReceiver(agents.get("velocidade"));
                            resp.addReceiver(agents.get("rotacoes"));
                            break;
                        case "acelerar":
                            //System.out.println("A acelerar");
                            resp.addReceiver(agents.get("velocidade"));
                            resp.addReceiver(agents.get("rotacoes"));
                            break;
                        case "manter":
                            //System.out.println("A manter");
                            resp.addReceiver(agents.get("velocidade"));
                            resp.addReceiver(agents.get("rotacoes"));
                            break;
                        case "gearup":
                            //System.out.println("Gear up");
                            resp.addReceiver(agents.get("velocidade"));
                            resp.addReceiver(agents.get("rotacoes"));
                            break;
                        case "geardown":
                            //System.out.println("Gear down");
                            resp.addReceiver(agents.get("velocidade"));
                            resp.addReceiver(agents.get("rotacoes"));
                            break;
                    }
                    myAgent.send(resp);
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
            AID[] alist = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                alist[i] = result[i].getName();
            }
            return alist;

        } catch (FIPAException fe) {}
        return null;
    }
    /*
     ---- MAIS DO QUE UM TIPO DE SENSOR
     */

    ArrayList<AID> searchDFtypes(String service) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        String[] services = service.split(" ");
        try {
            ArrayList<AID> alist = new ArrayList<>();
            for (String service1 : services) {
                sd.setType(service1);
                dfd.addServices(sd);
                SearchConstraints ALL = new SearchConstraints();
                ALL.setMaxResults(new Long(-1));
                DFAgentDescription[] result = DFService.search(this, dfd, ALL);
                for (DFAgentDescription result1 : result) {
                    alist.add(result1.getName());
                }
            }
            return alist;
        } catch (FIPAException fe) {}
        return null;
    }
}
