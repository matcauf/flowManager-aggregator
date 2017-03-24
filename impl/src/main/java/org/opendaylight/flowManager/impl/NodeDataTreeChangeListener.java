/*
 * Copyright © 2016 Inocybe, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.flowManager.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.flowManager.impl.utils.AbstractDataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeDataTreeChangeListener extends AbstractDataTreeChangeListener<Node> {

    private static final TopologyId OF_TOPOLOGYID = new TopologyId(new Uri("flow:1"));
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
        LOG.debug("added node {} ", add.getNodeId().getValue());
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
}
