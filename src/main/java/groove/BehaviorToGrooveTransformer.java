package groove;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.bpmn.*;
import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;
import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;
import behavior.petriNet.Place;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class BehaviorToGrooveTransformer {

    public static final String TOKEN_NODE_NAME = "Token";
    public static final String TOKEN_EDGE_NAME = "token";
    private static final String START_GST = "/start.gst";
    private static final String START = "start";
    private static final String START_NODE_ID = "n0";
    private static final String FINISHED_SUFFIX = "_finished";

    void generateGrooveGrammar(Behavior behavior, File targetFolder) {
        behavior.accept(new BehaviorVisitor() {
            @Override
            public void handle(FiniteStateMachine finiteStateMachine) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForFSM(finiteStateMachine, targetFolder);
            }

            @Override
            public void handle(PetriNet petriNet) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPN(petriNet, targetFolder);
            }

            @Override
            public void handle(BPMNProcessModel bpmnProcessModel) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForBPMNProcessModel(bpmnProcessModel, targetFolder);
            }
        });
    }

    private void generateGrooveGrammarForBPMNProcessModel(BPMNProcessModel bpmnProcessModel, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(bpmnProcessModel, grooveDir);

        // Generate start graph
        this.generateBPMNStartGraph(bpmnProcessModel, graphGrammarSubFolder);

        // Generate rules
        this.generateBPMNRules(bpmnProcessModel, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder);
    }

    private void generateBPMNRules(BPMNProcessModel bpmnProcessModel, File graphGrammarSubFolder) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();

        final Multimap<ParallelGateway, SequenceFlow> parallelGatewayOutgoing = ArrayListMultimap.create();
        final Multimap<ParallelGateway, SequenceFlow> parallelGatewayIncoming = ArrayListMultimap.create();

        bpmnProcessModel.getSequenceFlows().forEach(sequenceFlow -> {
            ruleGenerator.startRule(sequenceFlow.getName());

            sequenceFlow.getSource().accept(new ControlFlowNodeVisitor() {
                @Override
                public void handle(StartEvent startEventSource) {
                    sequenceFlow.getTarget().accept(new ControlFlowNodeVisitor() {
                        @Override
                        public void handle(StartEvent startEvent) {
                            throw new RuntimeException(
                                    String.format("There should be no sequence flow between start events! " +
                                                    "BPMN-Model: \"%s\", Sequence flow: \"%s\"",
                                            bpmnProcessModel,
                                            sequenceFlow));
                        }

                        @Override
                        public void handle(Activity activity) {
                            ruleGenerator.deleteNode(startEventSource.getName());
                            ruleGenerator.addNode(activity.getName());
                        }

                        @Override
                        public void handle(AlternativeGateway alternativeGateway) {
                            ruleGenerator.deleteNode(startEventSource.getName());
                            ruleGenerator.addNode(alternativeGateway.getName());
                        }

                        @Override
                        public void handle(ParallelGateway parallelGateway) {
                            ruleGenerator.deleteNode(startEventSource.getName());
                            ruleGenerator.addNode(startEventSource.getName() + FINISHED_SUFFIX);

                            parallelGatewayIncoming.put(parallelGateway, sequenceFlow);
                        }

                        @Override
                        public void handle(EndEvent endEvent) {
                            ruleGenerator.deleteNode(startEventSource.getName());
                            ruleGenerator.addNode(endEvent.getName());
                        }
                    });
                }

                @Override
                public void handle(Activity activitySource) {
                    sequenceFlow.getTarget().accept(new ControlFlowNodeVisitor() {
                        @Override
                        public void handle(StartEvent startEvent) {
                            throw new RuntimeException(
                                    String.format("There should be no sequence flow to a start event! " +
                                                    "BPMN-Model: \"%s\", Sequence flow: \"%s\"",
                                            bpmnProcessModel,
                                            sequenceFlow));
                        }

                        @Override
                        public void handle(Activity activityTarget) {
                            ruleGenerator.deleteNode(activitySource.getName());
                            ruleGenerator.addNode(activityTarget.getName());
                        }

                        @Override
                        public void handle(AlternativeGateway alternativeGateway) {
                            ruleGenerator.deleteNode(activitySource.getName());
                            ruleGenerator.addNode(alternativeGateway.getName());
                        }

                        @Override
                        public void handle(ParallelGateway parallelGateway) {
                            ruleGenerator.deleteNode(activitySource.getName());
                            ruleGenerator.addNode(activitySource.getName() + FINISHED_SUFFIX);

                            parallelGatewayIncoming.put(parallelGateway, sequenceFlow);
                        }

                        @Override
                        public void handle(EndEvent endEvent) {
                            ruleGenerator.deleteNode(activitySource.getName());
                            ruleGenerator.addNode(endEvent.getName());
                        }
                    });
                }

                @Override
                public void handle(AlternativeGateway alternativeGatewaySource) {
                    sequenceFlow.getTarget().accept(new ControlFlowNodeVisitor() {
                        @Override
                        public void handle(StartEvent startEvent) {
                            throw new RuntimeException(
                                    String.format("There should be no sequence flow to a start event! " +
                                                    "BPMN-Model: \"%s\", Sequence flow: \"%s\"",
                                            bpmnProcessModel,
                                            sequenceFlow));
                        }

                        @Override
                        public void handle(Activity activity) {
                            ruleGenerator.deleteNode(alternativeGatewaySource.getName());
                            ruleGenerator.addNode(activity.getName());
                        }

                        @Override
                        public void handle(AlternativeGateway alternativeGatewayTarget) {
                            ruleGenerator.deleteNode(alternativeGatewaySource.getName());
                            ruleGenerator.addNode(alternativeGatewayTarget.getName());
                        }

                        @Override
                        public void handle(ParallelGateway parallelGateway) {
                            ruleGenerator.deleteNode(alternativeGatewaySource.getName());
                            ruleGenerator.addNode(alternativeGatewaySource.getName() + FINISHED_SUFFIX);

                            parallelGatewayIncoming.put(parallelGateway, sequenceFlow);
                        }

                        @Override
                        public void handle(EndEvent endEvent) {
                            ruleGenerator.deleteNode(alternativeGatewaySource.getName());
                            ruleGenerator.addNode(endEvent.getName());
                        }
                    });
                }

                @Override
                public void handle(ParallelGateway parallelGatewaySource) {
                    throw new UnsupportedOperationException("TBD");
                }

                @Override
                public void handle(EndEvent endEvent) {
                    throw new RuntimeException(
                            String.format("An end event should never be source of a sequence flow! " +
                                            "BPMN-Model: \"%s\", Sequence flow: \"%s\"",
                                    bpmnProcessModel,
                                    sequenceFlow));
                }
            });
        ruleGenerator.generateRule();
        });

        // TODO: extra rules for parallel gateways

        ruleGenerator.writeRules(graphGrammarSubFolder);
    }

    private void generateBPMNStartGraph(BPMNProcessModel bpmnProcessModel, File graphGrammarSubFolder) {
        final String startEventName = bpmnProcessModel.getStartEvent().getName();

        createStartGraphWithOneNode(graphGrammarSubFolder, startEventName);

    }

    private void generateGrooveGrammarForPN(PetriNet petriNet, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(petriNet, grooveDir);

        // Generate start graph
        this.generatePNStartGraphFile(petriNet, graphGrammarSubFolder);

        // Generate rules
        this.generatePNRules(petriNet, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder);
    }

    private void generatePNStartGraphFile(PetriNet petriNet, File graphGrammarSubFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph(START, gxl);
        AtomicLong idCounter = new AtomicLong(-1);
        Map<String, String> nodeLabels = new HashMap<>();

        petriNet.getPlaces().forEach(place -> {
            Node placeNode = GrooveGxlHelper.createNodeWithName(
                    this.getNodeId(idCounter),
                    place.getName(),
                    graph);
            nodeLabels.put(placeNode.getId(), place.getName());
            // Create and link start tokens for each place.
            if (place.getStartTokenAmount() > 0) {
                for (int i = 0; i < place.getStartTokenAmount(); i++) {
                    Node tokenNode = GrooveGxlHelper.createNodeWithName(
                            this.getNodeId(idCounter),
                            TOKEN_NODE_NAME,
                            graph);
                    nodeLabels.put(tokenNode.getId(), TOKEN_NODE_NAME);
                    GrooveGxlHelper.createEdgeWithName(graph, placeNode, tokenNode, TOKEN_EDGE_NAME);
                }
            }
        });
        GrooveGxlHelper.layoutGraph(graph, nodeLabels);

        File startGraphFile = new File(graphGrammarSubFolder.getPath() + START_GST);
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private void generatePNRules(PetriNet petriNet, File subFolder) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        petriNet.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            transition.getIncomingEdges().forEach(weigthPlacePair -> {
                Place place = weigthPlacePair.getRight();
                Integer weight = weigthPlacePair.getLeft();
                GrooveNode placeNode = ruleGenerator.contextNode(place.getName());
                for (int i = 0; i < weight; i++) {
                    GrooveNode toBeDeletedTokenNode = ruleGenerator.deleteNode(TOKEN_NODE_NAME);
                    ruleGenerator.deleteEdge(TOKEN_EDGE_NAME, placeNode, toBeDeletedTokenNode);
                }
            });

            transition.getOutgoingEdges().forEach(weigthPlacePair -> {
                Place place = weigthPlacePair.getRight();
                Integer weight = weigthPlacePair.getLeft();
                GrooveNode placeNode = ruleGenerator.contextNode(place.getName());
                for (int i = 0; i < weight; i++) {
                    GrooveNode toBeAddedTokenNode = ruleGenerator.addNode(TOKEN_NODE_NAME);
                    ruleGenerator.addEdge(TOKEN_EDGE_NAME, placeNode, toBeAddedTokenNode);
                }
            });

            ruleGenerator.generateRule();
        });
        ruleGenerator.writeRules(subFolder);
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }

    private void generateGrooveGrammarForFSM(FiniteStateMachine finiteStateMachine, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(finiteStateMachine, grooveDir);

        this.generateFSMStartGraphFile(finiteStateMachine, graphGrammarSubFolder);

        this.generateFSMRules(finiteStateMachine, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder);
    }

    private File makeSubFolder(Behavior behavior, File grooveDir) {
        File graphGrammarSubFolder = new File(grooveDir + "/" + behavior.getName() + ".gps");
        graphGrammarSubFolder.mkdir();
        return graphGrammarSubFolder;
    }

    private void generateFSMRules(FiniteStateMachine finiteStateMachine, File subFolder) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            ruleGenerator.deleteNode(transition.getSource().getName());
            ruleGenerator.addNode(transition.getTarget().getName());

            ruleGenerator.generateRule();
        });
        ruleGenerator.writeRules(subFolder);
    }

    private void generatePropertiesFile(File subFolder) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String propertiesContent = String.format("# %s (Groove rule generator)\n" +
                        "location=%s\n" +
                        "startGraph=start\n" +
                        "grooveVersion=5.8.1\n" +
                        "grammarVersion=3.7",
                dtf.format(now),
                subFolder.getPath());
        File properties_file = new File(subFolder + "/" + "system.properties");
        try {
            FileUtils.writeStringToFile(properties_file, propertiesContent, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateFSMStartGraphFile(FiniteStateMachine finiteStateMachine, File targetFolder) {
        final String startStateName = finiteStateMachine.getStartState().getName();

        createStartGraphWithOneNode(targetFolder, startStateName);
    }

    private void createStartGraphWithOneNode(File targetFolder, String startNodeName) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph(START, gxl);
        GrooveGxlHelper.createNodeWithName(START_NODE_ID, startNodeName, graph);

        Map<String, String> nodeLabels = new HashMap<>();
        nodeLabels.put(START_NODE_ID, startNodeName);
        GrooveGxlHelper.layoutGraph(graph, nodeLabels);

        File startGraphFile = new File(targetFolder.getPath() + START_GST);
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }
}
