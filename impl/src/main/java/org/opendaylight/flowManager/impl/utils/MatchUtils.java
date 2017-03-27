/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.flowManager.impl.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;

/**
 * Created by matthieu on 2017-03-24.
 */
public class MatchUtils {

    /**
     * Create Ingress Port Match openflowId, inPort
     * @param matchBuilder  MatchBuilder Object without a match yet
     * @param nodeId Openflow switch ID
     * @param inPort Openflow port number
     * @return matchBuilder MatchBuilder Object with a match
     */
    public static MatchBuilder createInPortMatch(final MatchBuilder matchBuilder,
                                                 final String nodeId, final String inPort) {
        final NodeConnectorId ncid = new NodeConnectorId("openflow:" + nodeId + ":" + inPort);
        matchBuilder.setInPort(NodeConnectorId.getDefaultInstance(ncid.getValue()));
        matchBuilder.setInPort(ncid);
        return matchBuilder;
    }

    /**
     * Create Ingress Port Match openflowId, inPort
     * @param matchBuilder  MatchBuilder Object without a match yet
     * @param nodeConnectorId Openflow switch ID
     * @return matchBuilder MatchBuilder Object with a match
     */
    public static MatchBuilder createInPortMatch(final MatchBuilder matchBuilder,
                                                 final String nodeConnectorId) {
        final NodeConnectorId ncid = new NodeConnectorId(nodeConnectorId);
        matchBuilder.setInPort(NodeConnectorId.getDefaultInstance(ncid.getValue()));
        matchBuilder.setInPort(ncid);
        return matchBuilder;
    }
}
