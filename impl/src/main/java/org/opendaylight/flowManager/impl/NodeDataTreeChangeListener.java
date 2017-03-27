/*
 * Copyright © 2016 Inocybe, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.flowManager.impl;

import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.flowManager.impl.utils.*;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class NodeDataTreeChangeListener extends AbstractDataTreeChangeListener<Node> {
    public static final TopologyId OF_TOPOLOGYID = new TopologyId(new Uri("flow:1"));
    /**
     * Generate a Node InstanceIdentifier based on the flow:1 Network Topoology
     *
     * @return - A Node InstanceIdentifier
     */
    public static InstanceIdentifier<Node> getOFNodesTopologyPath() {
        return InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(OF_TOPOLOGYID))
                .child(Node.class)
                .build();
    }

    private static final Logger LOG = LoggerFactory.getLogger(NodeDataTreeChangeListener.class);
    private final ListenerRegistration<NodeDataTreeChangeListener> listener;

    public NodeDataTreeChangeListener(final DataBroker dataBroker) {
        super(dataBroker);
        final InstanceIdentifier<Node> NodeIid = this.getOFNodesTopologyPath();
        final DataTreeIdentifier<Node> dataTreeIid =
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, NodeIid);
        listener = dataBroker.registerDataTreeChangeListener(dataTreeIid, this);
        LOG.info("NodeDataTreeChangeListener created and registered");
    }

    @Override
    protected void add(final InstanceIdentifier<Node> identifier, final Node add) throws RuntimeException {
        LOG.info("added node {} ", add.getNodeId().getValue());
        if(NodeUtils.isOpenFlow(add.getNodeId().getValue())) {
            LOG.info("Is an openflow node, configure it:");
            add.getTerminationPoint().forEach(
                    terminationPoint -> {
                        this.addFlow(add, terminationPoint.getKey().getTpId().getValue());
                    }
            );
        }
    }

    @Override
    public void close() throws Exception {
        if (listener != null){
            listener.close();
        }
    }

    @Override
    protected void remove(final InstanceIdentifier<Node> identifier, final Node del) {

    }

    @Override
    protected void update(final InstanceIdentifier<Node> identifier, final Node original, final Node update) {

    }


    private void addFlow(final Node node, final String terminationPoint) {
        NodeBuilder nodeBuilder = FlowUtils.createNodeBuilder(node.getNodeId().getValue());
        FlowBuilder flowBuilder = allowAllCommunications(node, terminationPoint);
        writeFlow(flowBuilder, nodeBuilder);
    }

    private void writeFlow(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        LOG.info("writeFlow: flowBuilder: {}, nodeBuilder: {}", flowBuilder.build(), nodeBuilder.build());
        MdsalUtils.merge(this.dataBroker, LogicalDatastoreType.CONFIGURATION, FlowUtils.createNodePath(nodeBuilder),
                nodeBuilder.build());
        MdsalUtils.put(this.dataBroker, LogicalDatastoreType.CONFIGURATION, FlowUtils.createFlowPath(flowBuilder, nodeBuilder),
                flowBuilder.build());
    }

    /**
     * Allow all communications for a given terminationPoint with all others
     * @param node
     * @param terminationPoint
     * @return FlowBuilder
     */
    private FlowBuilder allowAllCommunications(final Node node, final String terminationPoint) {
        // set match
        MatchBuilder matchBuilder = new MatchBuilder();
        MatchUtils.createInPortMatch(matchBuilder,  terminationPoint);
        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();

        final AtomicInteger i = new AtomicInteger();
        // Set output action
        node.getTerminationPoint().forEach(
                (terminationPoint1) -> {
                    if(!terminationPoint.equals(terminationPoint1.getTpId().getValue())) {
                        OutputActionBuilder output = new OutputActionBuilder();
                        output.setOutputNodeConnector(new Uri(NodeUtils.extractPort(terminationPoint1.getTpId().getValue())));
                        output.setMaxLength(60);
                        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
                        ab.setOrder(i.get());
                        ab.setKey(new ActionKey(i.getAndIncrement()));
                        actionList.add(ab.build());
                    }
                }
        );

        // Create Apply Actions Instruction
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());


        FlowBuilder flowBuilder = new FlowBuilder();
        // Create Flow
        flowBuilder.setMatch(matchBuilder.build());
        String flowId = "L2_Rule_" + terminationPoint;
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setStrict(false);
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short)0);
        flowBuilder.setKey(key);
        flowBuilder.setPriority(0);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(1200);
        flowBuilder.setIdleTimeout(3400);
        flowBuilder.setCookie(new FlowCookie(BigInteger.valueOf(106)));
        flowBuilder.setCookieMask(new FlowCookie(BigInteger.valueOf(255)));
        flowBuilder.setInstructions(isb.setInstruction(instructions).build());

        return flowBuilder;
    }
}
