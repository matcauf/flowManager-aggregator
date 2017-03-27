/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.flowManager.impl.utils;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

public class NodeUtils {

    private static final int NODE_CONNECTOR_SIZE = 3;
    private static final int OPENFLOW_NODE_ID_INDEX = 1;
    private static final int OPENFLOW_PORT_INDEX = 2;
    private static final int OPENFLOW_PREFIX_INDEX = 0;
    private static final String OPENFLOW_NAME = "openflow";


    public static String extractNodeId(final String nodeConnectorId) {
        final List<String> split = validateNodeConnectorId(nodeConnectorId);
        return split.get(OPENFLOW_PREFIX_INDEX) + ":" + split.get(OPENFLOW_NODE_ID_INDEX);
    }

    private static List<String> validateNodeConnectorId(final String nodeConnectorId) {
        final List<String> split = Arrays.asList(nodeConnectorId.split(":"));
        Preconditions.checkState(split.size() == NODE_CONNECTOR_SIZE);
        return split;
    }

    public static String extractPort(final String nodeConnectorId) {
        final List<String> split = validateNodeConnectorId(nodeConnectorId);
        return split.get(OPENFLOW_PORT_INDEX);
    }

    public static boolean isOpenFlow(final String nodeID) {
        final List<String> split = Arrays.asList(nodeID.split(":"));
        return split.get(OPENFLOW_PREFIX_INDEX).equals(OPENFLOW_NAME);
    }
}
