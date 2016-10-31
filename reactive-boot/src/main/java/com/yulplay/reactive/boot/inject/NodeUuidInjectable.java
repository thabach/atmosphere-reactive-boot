package com.yulplay.reactive.boot.inject;

import com.yulplay.reactive.boot.impl.NodeUuid;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;

import java.lang.reflect.Type;

@ApplicationScoped
public class NodeUuidInjectable implements Injectable<NodeUuid> {

    private final NodeUuid nodeUuid = new NodeUuid();

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && NodeUuid.class.isAssignableFrom((Class) t);
    }

    @Override
    public NodeUuid injectable(AtmosphereConfig config) {
        return nodeUuid;
    }

}

