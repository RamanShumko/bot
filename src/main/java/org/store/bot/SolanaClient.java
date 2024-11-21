package org.store.bot;

import lombok.Getter;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SolanaClient {
    private final RpcClient rpcClient = new RpcClient(Cluster.DEVNET);
}
